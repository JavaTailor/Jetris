package codegen.operators;

import codegen.Generator;
import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.providers.StaticMethodProvider;
import codegen.providers.TypeProvider;
import config.FuzzingRandom;
import flowinfo.AbstractMethodCall;
import flowinfo.AbstractNode;
import soot.util.Numberable;
import config.FuzzingConfig;
import codegen.blocks.StmtBlock;
import codegen.providers.NameProvider;
import soot.*;
import soot.jimple.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static codegen.providers.ElementsProvider.getAvailableValues;

public class ApiOperator extends Generic {

    protected static ApiOperator aop;

    public static ApiOperator getInstance() {
        if (aop == null) {
            aop = new ApiOperator();
        }
        return aop;
    }

    /**
     * 返回一个含有函数调用的block。如果生成失败会返回一个空block。
     * 首先随机产生静态函数调用和实例函数调用。
     * 如果是实例函数调用则以一定的概率去选择当前类中及函数中的变量
     * 或者生成一个新的变量，调用其实例函数。
     * 并如果函数有返回值，将其返回值声明成变量。
     * @param clazz block的声明类
     * @param methodSign block所在的函数
     * @return
     */
    @Override
    public StmtBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {

        StmtBlock block = new StmtBlock();
        //01 获取已有变量，包括 field 以及 local
        if (targets == null || targets.size() < 1) {
            return block;
        }
        Stmt targetStmt = targets.get(FuzzingRandom.nextChoice(targets.size()));
        List<Numberable> candidateVars = filterValidVars(getAvailableValues(clazz, methodSign, targetStmt));
        //自定义函数中不应该包含调用自定义函数，防止产生循环依赖或者无限递归
        for (int cnt = 0; cnt <= FuzzingConfig.MAX_METHOD_INVOCATION && block.getStmts().size() < FuzzingConfig.MAX_BLOCK_INST; cnt++) {

            Local localVar = null;
            SootMethod targetFunc = null;

            if (!clazz.getMethods().contains(clazz.getMethodMaps().get(methodSign))
                    && clazz.getStaticMethods().size() > 0
                    && FuzzingRandom.randomUpTo(FuzzingConfig.PROB_SELF_METHOD_INVOCATION)) {
                SootMethod method = clazz.getMethodMaps().get(methodSign);
                if (method.isStatic()) {
                    targetFunc = clazz.getStaticMethods().get(FuzzingRandom.nextChoice(clazz.getStaticMethods().size()));
                } else {
                    targetFunc = clazz.getMethods().get(FuzzingRandom.nextChoice(clazz.getMethods().size()));
                    localVar = clazz.getThisRef(methodSign);
                }
            } else if (!FuzzingRandom.randomUpTo(FuzzingConfig.PROB_STATIC_METHOD_INVOCATION)) {
                if (candidateVars.size() > 0 && FuzzingRandom.flipCoin()) {
                    //reuse defined variables
                    Numberable refVar = candidateVars.get(FuzzingRandom.nextChoice(candidateVars.size()));
                    if (refVar instanceof Local) {
                        localVar = (Local) refVar;
                        //⚠️ 标记重用的局部变量
                        block.addReusedVar(localVar);
                    } else if (refVar instanceof SootField) {
                        //assign field var to local var
                        localVar = Jimple.v().newLocal(NameProvider.genVarName(), ((SootField) refVar).getType());
                        block.addLocalVar(localVar);

                        if (((SootField) refVar).isStatic()) {
                            block.addStmt(Jimple.v().newAssignStmt(localVar, Jimple.v().newStaticFieldRef(((SootField) refVar).makeRef())));
                        } else {
                            block.addStmt(Jimple.v().newAssignStmt(localVar, Jimple.v().newInstanceFieldRef(clazz.getThisRef(methodSign), ((SootField) refVar).makeRef())));
                        }
                    } else {
                        throw new RuntimeException("Something wrong here: ApiOperator - nextBlock");
                    }

                    //以一定的概率去使用已定义变量内的引用类型变量。
                    if (FuzzingRandom.flipCoin()) {

                        ArrayList<SootField> subFields = new ArrayList<>();
                        for (SootField field : ((RefType) localVar.getType()).getSootClass().getFields()) {
                            if (field.isPublic() && !field.getDeclaration().contains("final") && field.getType() instanceof RefType) {
                                subFields.add(field);
                            }
                        }
                        if (subFields.size() > 0) {
                            SootField subField = subFields.get(FuzzingRandom.nextChoice(subFields.size()));
                            Local subLocal = Jimple.v().newLocal(NameProvider.genVarName(), subField.getType());
                            block.addLocalVar(subLocal);
                            if (subField.isStatic()) {
                                block.addStmt(Jimple.v().newAssignStmt(subLocal, Jimple.v().newStaticFieldRef(subField.makeRef())));
                            } else {
                                block.addStmt(Jimple.v().newAssignStmt(subLocal, Jimple.v().newInstanceFieldRef(localVar, subField.makeRef())));
                            }
                            localVar = subLocal;
                        }
                    }

                    ArrayList<SootMethod> availableMethods = new ArrayList<>();
                    for (SootMethod method : ((RefType) localVar.getType()).getSootClass().getMethods()) {
                        if (method.isPublic() && !method.isStatic() && method.isConcrete() && !method.isConstructor()) {
                            availableMethods.add(method);
                        }
                    }
                    if (availableMethods.isEmpty()) return new StmtBlock();
                    targetFunc = availableMethods.get(FuzzingRandom.nextChoice(availableMethods.size()));
                } else  {

                    //03 创建新的引用类型变量，并调用其方法
                    //create new Ref variables
                    Type refType = TypeProvider.anyRefType();
                    ArrayList<SootMethod> availableMethods = new ArrayList<>();
                    for (SootMethod method : ((RefType) refType).getSootClass().getMethods()) {
                        if (method.isPublic() && method.isConcrete() && !method.isConstructor()) {
                            availableMethods.add(method);
                        }
                    }
                    if (!availableMethods.isEmpty()) {
                        targetFunc = availableMethods.get(FuzzingRandom.nextChoice(availableMethods.size()));
                        if (!targetFunc.isStatic()) {

                            StmtBlock sub = Generator.nextVariable(clazz, methodSign, refType, targetStmt);
                            for (Local var : sub.getLocalVars()) {
                                if (var.getType() == refType) {
                                    localVar = var;
                                }
                            }
                            block.addAllLocalVars(sub.getLocalVars());
                            block.addAllReusedVars(sub.getReusedVars());
                            block.addAllStmts(sub.getStmts());
                        }
                    }
                }
            }
            if (targetFunc == null) {
                targetFunc = StaticMethodProvider.getStaticMethod();
            }

            //04 调用被选中的API
            // 返回值直接放到函数调用里，以StmtBlock的形式传回来
            StmtBlock invokeBlock = Generator.funcInvocation(clazz, methodSign, localVar, targetFunc, targetStmt);
            if (invokeBlock.getStmts().isEmpty()) return new StmtBlock();
            block.addAllStmts(invokeBlock.getStmts());
            block.addAllLocalVars(invokeBlock.getLocalVars());
            for (Local var : invokeBlock.getReusedVars()) {
                block.addReusedVar(var);
            }
        }
        block.setInserationTarget(targetStmt);
        insertGotoStmt(clazz, methodSign, block);
        return block;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets, AbstractNode nodeTemp) {

        StmtBlock block = new StmtBlock();
        //01 获取已有变量，包括 field 以及 local
        if (targets == null || targets.size() < 1) {
            return block;
        }
        Stmt targetStmt = targets.get(FuzzingRandom.nextChoice(targets.size()));
        List<Numberable> candidateVars = filterValidVars(getAvailableValues(clazz, methodSign, targetStmt));
        //自定义函数中不应该包含调用自定义函数，防止产生循环依赖或者无限递归
        for (int cnt = 0; cnt <= FuzzingConfig.MAX_METHOD_INVOCATION && block.getStmts().size() < FuzzingConfig.MAX_BLOCK_INST; cnt++) {

            Local localVar = null;
            SootMethod targetFunc = null;

            if (!clazz.getMethods().contains(clazz.getMethodMaps().get(methodSign))
                    && clazz.getStaticMethods().size() > 0
                    && FuzzingRandom.randomUpTo(FuzzingConfig.PROB_SELF_METHOD_INVOCATION)) {
                SootMethod method = clazz.getMethodMaps().get(methodSign);
                if (method.isStatic()) {
                    targetFunc = clazz.getStaticMethods().get(FuzzingRandom.nextChoice(clazz.getStaticMethods().size()));
                } else {
                    targetFunc = clazz.getMethods().get(FuzzingRandom.nextChoice(clazz.getMethods().size()));
                    localVar = clazz.getThisRef(methodSign);
                }
            } else if (!FuzzingRandom.randomUpTo(FuzzingConfig.PROB_STATIC_METHOD_INVOCATION)) {
                if (candidateVars.size() > 0 && FuzzingRandom.flipCoin()) {
                    //reuse defined variables
                    Numberable refVar = candidateVars.get(FuzzingRandom.nextChoice(candidateVars.size()));
                    if (refVar instanceof Local) {
                        localVar = (Local) refVar;
                        //⚠️ 标记重用的局部变量
                        block.addReusedVar(localVar);
                    } else if (refVar instanceof SootField) {
                        //assign field var to local var
                        localVar = Jimple.v().newLocal(NameProvider.genVarName(), ((SootField) refVar).getType());
                        block.addLocalVar(localVar);

                        if (((SootField) refVar).isStatic()) {
                            block.addStmt(Jimple.v().newAssignStmt(localVar, Jimple.v().newStaticFieldRef(((SootField) refVar).makeRef())));
                        } else {
                            block.addStmt(Jimple.v().newAssignStmt(localVar, Jimple.v().newInstanceFieldRef(clazz.getThisRef(methodSign), ((SootField) refVar).makeRef())));
                        }
                    } else {
                        throw new RuntimeException("Something wrong here: ApiOperator - nextBlock");
                    }

                    //以一定的概率去使用已定义变量内的引用类型变量。
                    if (FuzzingRandom.flipCoin()) {

                        ArrayList<SootField> subFields = new ArrayList<>();
                        for (SootField field : ((RefType) localVar.getType()).getSootClass().getFields()) {
                            if (field.isPublic() && !field.getDeclaration().contains("final") && field.getType() instanceof RefType) {
                                subFields.add(field);
                            }
                        }
                        if (subFields.size() > 0) {
                            SootField subField = subFields.get(FuzzingRandom.nextChoice(subFields.size()));
                            Local subLocal = Jimple.v().newLocal(NameProvider.genVarName(), subField.getType());
                            block.addLocalVar(subLocal);
                            if (subField.isStatic()) {
                                block.addStmt(Jimple.v().newAssignStmt(subLocal, Jimple.v().newStaticFieldRef(subField.makeRef())));
                            } else {
                                block.addStmt(Jimple.v().newAssignStmt(subLocal, Jimple.v().newInstanceFieldRef(localVar, subField.makeRef())));
                            }
                            localVar = subLocal;
                        }
                    }

                    ArrayList<SootMethod> availableMethods = new ArrayList<>();
                    for (SootMethod method : ((RefType) localVar.getType()).getSootClass().getMethods()) {
                        if (method.isPublic() && !method.isStatic() && method.isConcrete() && !method.isConstructor()) {
                            availableMethods.add(method);
                        }
                    }
                    if (availableMethods.isEmpty()) return new StmtBlock();
                    targetFunc = availableMethods.get(FuzzingRandom.nextChoice(availableMethods.size()));
                } else  {

                    //03 创建新的引用类型变量，并调用其方法
                    //create new Ref variables
                    Type refType = TypeProvider.anyRefType();
                    ArrayList<SootMethod> availableMethods = new ArrayList<>();
                    if (FuzzingRandom.flipCoin()) {

                        ArrayList<Type> candidateTypes = new ArrayList<>();
                        for (Type type : nodeTemp.getProperty().getDefSet()) {
                            if (type instanceof RefType) {
                                candidateTypes.add(type);
                            }
                        }
                        if (candidateTypes.size() > 0) {
                            refType = candidateTypes.get(FuzzingRandom.nextChoice(candidateTypes.size()));
                            ArrayList<SootMethod> allMethods = new ArrayList<>();
                            for (SootMethod method : ((RefType) refType).getSootClass().getMethods()) {
                                if (method.isPublic() && method.isConcrete() && !method.isConstructor()) {
                                    allMethods.add(method);
                                }
                            }
                            Set<Expr> useSet = nodeTemp.getProperty().getUseSet().get(refType);
                            for (SootMethod method : allMethods) {
                                for (Expr expr : useSet) {
                                    if (expr instanceof AbstractMethodCall) {
                                        if (((AbstractMethodCall) expr).getMethodName().equals(method.getName())) {
                                            availableMethods.add(method);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        for (SootMethod method : ((RefType) refType).getSootClass().getMethods()) {
                            if (method.isPublic() && method.isConcrete() && !method.isConstructor()) {
                                availableMethods.add(method);
                            }
                        }
                    }

                    if (!availableMethods.isEmpty()) {
                        targetFunc = availableMethods.get(FuzzingRandom.nextChoice(availableMethods.size()));
                        if (!targetFunc.isStatic()) {

                            StmtBlock sub = Generator.nextVariable(clazz, methodSign, refType, targetStmt);
                            for (Local var : sub.getLocalVars()) {
                                if (var.getType() == refType) {
                                    localVar = var;
                                }
                            }
                            block.addAllLocalVars(sub.getLocalVars());
                            block.addAllReusedVars(sub.getReusedVars());
                            block.addAllStmts(sub.getStmts());
                        }
                    }
                }
            }
            if (targetFunc == null) {
                targetFunc = StaticMethodProvider.getStaticMethod();
            }

            //04 调用被选中的API
            // 返回值直接放到函数调用里，以StmtBlock的形式传回来
            StmtBlock invokeBlock = Generator.funcInvocation(clazz, methodSign, localVar, targetFunc, targetStmt);
            if (invokeBlock.getStmts().isEmpty()) return new StmtBlock();
            block.addAllStmts(invokeBlock.getStmts());
            block.addAllLocalVars(invokeBlock.getLocalVars());
            for (Local var : invokeBlock.getReusedVars()) {
                block.addReusedVar(var);
            }
        }
        block.setInserationTarget(targetStmt);
        insertGotoStmt(clazz, methodSign, block);
        return block;
    }

    public List<Numberable> filterValidVars(List<Numberable> availableValues) {

        List<Numberable> vars = new ArrayList<>();
        for (Numberable value: availableValues) {
            if (value instanceof Local) {
                if (((Local) value).getType() instanceof RefType) {
                    vars.add(value);
                }
            } else if (value instanceof SootField) {
                if (((SootField) value).getType() instanceof RefType) {
                    vars.add(value);
                }
            }
        }
        return vars;
    }
}
