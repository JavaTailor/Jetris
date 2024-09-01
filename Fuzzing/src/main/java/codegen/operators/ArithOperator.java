package codegen.operators;

import codegen.Generator;
import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.blocks.StmtBlock;
import codegen.providers.NameProvider;
import codegen.providers.OperatorProvider;
import codegen.providers.PrimitiveValueProvider;
import codegen.providers.TypeProvider;
import config.FuzzingRandom;
import flowinfo.AbstractNode;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.util.Numberable;
import config.FuzzingConfig;


import java.util.*;

import static codegen.providers.ElementsProvider.getAvailableValues;

public class ArithOperator extends Generic{

    protected static ArithOperator aop;

    public static ArithOperator getInstance() {
        if (aop == null) {
            aop = new ArithOperator();
        }
        return aop;
    }

    /**
     * TODO 运行时会报错，但可以正确反编译。
     * Error: A JNI error has occurred, please check your installation and try again
     * Exception in thread "main" java.lang.VerifyError: Bad type on operand stack
     * 生成若干运算语句。 由于Jimple是三地址码，右侧只能是一个表达式。
     * 随机选择一个局部变量作为左值。
     * 随机选择两个全局变量，局部变量，随机的常数作为表达式的操作数。（~是一个）
     * @param clazz
     * @param methodSign
     * @return
     */
    @Override
    public StmtBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {

        StmtBlock block = new StmtBlock();
        if (targets == null || targets.size() < 1) {
            return block;
        }
        //TODO
        //Stmt targetStmt = targets.get(FuzzingConfig.nextChoice(targets.size()));
        Stmt targetStmt = targets.get(0);
        List<Numberable> candidateVars = filterValidVars(getAvailableValues(clazz, methodSign, targetStmt));

        if (candidateVars.size() <= 0) {
            for (int i = 1; i <= 5; i++) {
                Type type = TypeProvider.anyPrimType();
                BasicBlock b = Generator.nextVariable(clazz, methodSign, type, targets.get(0));
                candidateVars.add(b.getLocalVars().get(0));
                block.addAllStmts(b.getStmts());
                block.addAllLocalVars(b.getLocalVars());
            }
        }
        for (int cnt = 0; cnt <= FuzzingConfig.MAX_BLOCK_INST && block.getStmts().size() < FuzzingConfig.MAX_BLOCK_INST; cnt++) {

            if (candidateVars.size() > 0) {

                HashMap<Type, ArrayList<Numberable>> variableMaps = classifyVariablesByType(candidateVars);
                ArrayList<Type> list = new ArrayList<>();
                list.addAll(variableMaps.keySet());
                Type type = list.get(FuzzingRandom.nextChoice(list.size()));
                ArrayList<Numberable> typeVars = variableMaps.get(type);
                int operandSize = FuzzingRandom.nextChoice(typeVars.size());
                Set<Numberable> computes = new HashSet<>();
                for (int i = 0; i <= operandSize; i++) {
                    Numberable operand = typeVars.get(FuzzingRandom.nextChoice(operandSize));
                    //添加使用到的局部变量，用于后面验证作用域
                    if (operand instanceof Local) {
                        block.addReusedVar((Local) operand);
                    }
                    computes.add(operand);
                }
                StmtBlock arithBlock = primVarArithmetic(clazz, methodSign, computes, type);
                block.addAllLocalVars(arithBlock.getLocalVars());
                block.addAllStmts(arithBlock.getStmts());
            }
        }
        block.setInserationTarget(targetStmt);
        insertGotoStmt(clazz, methodSign, block);
        return block;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets, AbstractNode nodeTemp) {
        StmtBlock block = new StmtBlock();
        if (targets == null || targets.size() < 1) {
            return block;
        }
        //TODO
        //Stmt targetStmt = targets.get(FuzzingConfig.nextChoice(targets.size()));
        Stmt targetStmt = targets.get(0);
        List<Numberable> candidateVars = filterValidVars(getAvailableValues(clazz, methodSign, targetStmt));

        if (candidateVars.size() <= 0) {

            HashSet<Type> defSet = nodeTemp.getProperty().getDefSet();
            for (Type type : defSet) {
                if (type instanceof PrimType) {
                    BasicBlock b = Generator.nextVariable(clazz, methodSign, type, targets.get(0));
                    candidateVars.add(b.getLocalVars().get(0));
                    block.addAllStmts(b.getStmts());
                    block.addAllLocalVars(b.getLocalVars());
                }
            }
            while (candidateVars.size() < 5) {
                Type type = TypeProvider.anyPrimType();
                BasicBlock b = Generator.nextVariable(clazz, methodSign, type, targets.get(0));
                candidateVars.add(b.getLocalVars().get(0));
                block.addAllStmts(b.getStmts());
                block.addAllLocalVars(b.getLocalVars());
            }
        }
        for (int cnt = 0; cnt <= FuzzingConfig.MAX_BLOCK_INST && block.getStmts().size() < FuzzingConfig.MAX_BLOCK_INST; cnt++) {

            if (candidateVars.size() > 0) {

                HashMap<Type, ArrayList<Numberable>> variableMaps = classifyVariablesByType(candidateVars);
                ArrayList<Type> list = new ArrayList<>();
                list.addAll(variableMaps.keySet());
                Type type = list.get(FuzzingRandom.nextChoice(list.size()));
                ArrayList<Numberable> typeVars = variableMaps.get(type);
                int operandSize = FuzzingRandom.nextChoice(typeVars.size());
                Set<Numberable> computes = new HashSet<>();
                for (int i = 0; i <= operandSize; i++) {
                    Numberable operand = typeVars.get(FuzzingRandom.nextChoice(operandSize));
                    //添加使用到的局部变量，用于后面验证作用域
                    if (operand instanceof Local) {
                        block.addReusedVar((Local) operand);
                    }
                    computes.add(operand);
                }
                StmtBlock arithBlock = primVarArithmetic(clazz, methodSign, computes, type, nodeTemp);
                block.addAllLocalVars(arithBlock.getLocalVars());
                block.addAllStmts(arithBlock.getStmts());
            }
        }
        block.setInserationTarget(targetStmt);
        insertGotoStmt(clazz, methodSign, block);
        return block;
    }

    public HashMap<Type, ArrayList<Numberable>> classifyVariablesByType(List<Numberable> candidates) {

        HashMap<Type, ArrayList<Numberable>> maps = new HashMap<>();
        for (Numberable candidate : candidates) {
            Type type = candidate instanceof Local ? ((Local)candidate).getType() : ((SootField)candidate).getType();
            if (maps.keySet().contains(type)) {
                maps.get(type).add(candidate);
            } else {
                ArrayList<Numberable> tmp = new ArrayList<>();
                tmp.add(candidate);
                maps.put(type, tmp);
            }
        }
        return maps;
    }

    public StmtBlock primVarArithmetic(ClassInfo clazz, String methodSign, Set<Numberable> computes, Type type) {

        StmtBlock block = new StmtBlock();
        Iterator iterator = computes.iterator();
        if (computes.size() == 0) return block;
        if (computes.size() == 1) {

            Numberable operand = (Numberable) iterator.next();
            Expr operator = OperatorProvider.anyOperator(type);

            if (operand instanceof Local) {

                Object expr = operator instanceof DivExpr || operator instanceof JRemExpr ?
                        OperatorProvider.createOperatorFormulaStmt(operator, (Value) operand, PrimitiveValueProvider.nextNonZero(type)) :
                        OperatorProvider.createOperatorFormulaStmt(operator, (Value) operand, PrimitiveValueProvider.next(type));
                if (expr != null) {
                    JAssignStmt assignToLocal = (JAssignStmt) Jimple.v().newAssignStmt((Value) operand, (Value) expr);
                    block.addStmt(assignToLocal);
                }
            } else {

                Local localOperand = Jimple.v().newLocal(NameProvider.genVarName(), type);
                block.addLocalVar(localOperand);
                Object expr = operator instanceof DivExpr || operator instanceof JRemExpr ?
                        OperatorProvider.createOperatorFormulaStmt(operator, localOperand, PrimitiveValueProvider.nextNonZero(type)) :
                        OperatorProvider.createOperatorFormulaStmt(operator, localOperand, PrimitiveValueProvider.next(type));

                if (((SootField)operand).isStatic()) {
                    block.addStmt(Jimple.v().newAssignStmt(localOperand, Jimple.v().newStaticFieldRef(((SootField)operand).makeRef())));
                    if (expr != null) {
                        block.addStmt(Jimple.v().newAssignStmt(localOperand, (Value) expr));
                        block.addStmt(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(((SootField)operand).makeRef()), localOperand));
                    }
                } else {
                    block.addStmt(Jimple.v().newAssignStmt(localOperand, Jimple.v().newInstanceFieldRef(clazz.getThisRef(methodSign), ((SootField) operand).makeRef())));
                    if (expr != null) {
                        block.addStmt(Jimple.v().newAssignStmt(localOperand, (Value) expr));
                        block.addStmt(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(clazz.getThisRef(methodSign), ((SootField) operand).makeRef()), localOperand));
                    }
                }
            }
        } else {

            Local localOperand = Jimple.v().newLocal(NameProvider.genVarName(), type);
            block.addLocalVar(localOperand);
            block.addStmt(Jimple.v().newAssignStmt(localOperand, PrimitiveValueProvider.next(type)));

            while (iterator.hasNext()) {

                Numberable operand = (Numberable) iterator.next();
                Expr operator = OperatorProvider.anyOperator(type);
                if (operand instanceof Local) {
                    Object expr = OperatorProvider.createOperatorFormulaStmt(operator, localOperand, (Value) operand);
                    if (expr != null) {
                        block.addStmt(Jimple.v().newAssignStmt(localOperand, (Value) expr));
                    }
                } else {

                    Local subLocal = Jimple.v().newLocal(NameProvider.genVarName(), type);
                    block.addLocalVar(subLocal);
                    if (((SootField)operand).isStatic()) {
                        block.addStmt(Jimple.v().newAssignStmt(subLocal, Jimple.v().newStaticFieldRef(((SootField)operand).makeRef())));
                    } else {
                        block.addStmt(Jimple.v().newAssignStmt(subLocal, Jimple.v().newInstanceFieldRef(clazz.getThisRef(methodSign), ((SootField) operand).makeRef())));
                    }
                    Object expr = OperatorProvider.createOperatorFormulaStmt(operator, localOperand, subLocal);
                    if (expr != null) {
                        block.addStmt(Jimple.v().newAssignStmt(localOperand, subLocal));
                    }
                }
            }
        }
        return block;
    }

    private StmtBlock primVarArithmetic(ClassInfo clazz, String methodSign, Set<Numberable> computes, Type type, AbstractNode nodeTemp) {

        StmtBlock block = new StmtBlock();
        Iterator iterator = computes.iterator();

        HashMap<Type, Set<Expr>> useSet = nodeTemp.getProperty().getUseSet();

        if (computes.size() == 0) return block;
        if (computes.size() == 1) {

            Numberable operand = (Numberable) iterator.next();
            Expr operator  = OperatorProvider.anyOperator(type);
            if (useSet.containsKey(type)) {
                if (useSet.get(type).size() > 0) {
                    Set<Expr> operators = useSet.get(type);
                    operator = (Expr) operators.toArray()[FuzzingRandom.nextChoice(operators.size())];
                }
            }

            if (operand instanceof Local) {

                Object expr = operator instanceof DivExpr || operator instanceof JRemExpr ?
                        OperatorProvider.createOperatorFormulaStmt(operator, (Value) operand, PrimitiveValueProvider.nextNonZero(type)) :
                        OperatorProvider.createOperatorFormulaStmt(operator, (Value) operand, PrimitiveValueProvider.next(type));
                if (expr != null) {
                    JAssignStmt assignToLocal = (JAssignStmt) Jimple.v().newAssignStmt((Value) operand, (Value) expr);
                    block.addStmt(assignToLocal);
                }
            } else {

                Local localOperand = Jimple.v().newLocal(NameProvider.genVarName(), type);
                block.addLocalVar(localOperand);
                Object expr = operator instanceof DivExpr || operator instanceof JRemExpr ?
                        OperatorProvider.createOperatorFormulaStmt(operator, localOperand, PrimitiveValueProvider.nextNonZero(type)) :
                        OperatorProvider.createOperatorFormulaStmt(operator, localOperand, PrimitiveValueProvider.next(type));

                if (((SootField)operand).isStatic()) {
                    block.addStmt(Jimple.v().newAssignStmt(localOperand, Jimple.v().newStaticFieldRef(((SootField)operand).makeRef())));
                    if (expr != null) {
                        block.addStmt(Jimple.v().newAssignStmt(localOperand, (Value) expr));
                        block.addStmt(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(((SootField)operand).makeRef()), localOperand));
                    }
                } else {
                    block.addStmt(Jimple.v().newAssignStmt(localOperand, Jimple.v().newInstanceFieldRef(clazz.getThisRef(methodSign), ((SootField) operand).makeRef())));
                    if (expr != null) {
                        block.addStmt(Jimple.v().newAssignStmt(localOperand, (Value) expr));
                        block.addStmt(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(clazz.getThisRef(methodSign), ((SootField) operand).makeRef()), localOperand));
                    }
                }
            }
        } else {

            Local localOperand = Jimple.v().newLocal(NameProvider.genVarName(), type);
            block.addLocalVar(localOperand);
            block.addStmt(Jimple.v().newAssignStmt(localOperand, PrimitiveValueProvider.next(type)));

            while (iterator.hasNext()) {

                Numberable operand = (Numberable) iterator.next();

                Expr operator = OperatorProvider.anyOperator(type);
                if (useSet.containsKey(type)) {
                    if (useSet.get(type).size() > 0) {
                        Set<Expr> operators = useSet.get(type);
                        operator = (Expr) operators.toArray()[FuzzingRandom.nextChoice(operators.size())];
                    }
                }

                if (operand instanceof Local) {
                    Object expr = OperatorProvider.createOperatorFormulaStmt(operator, localOperand, (Value) operand);
                    if (expr != null) {
                        block.addStmt(Jimple.v().newAssignStmt(localOperand, (Value) expr));
                    }
                } else {

                    Local subLocal = Jimple.v().newLocal(NameProvider.genVarName(), type);
                    block.addLocalVar(subLocal);
                    if (((SootField)operand).isStatic()) {
                        block.addStmt(Jimple.v().newAssignStmt(subLocal, Jimple.v().newStaticFieldRef(((SootField)operand).makeRef())));
                    } else {
                        block.addStmt(Jimple.v().newAssignStmt(subLocal, Jimple.v().newInstanceFieldRef(clazz.getThisRef(methodSign), ((SootField) operand).makeRef())));
                    }
                    Object expr = OperatorProvider.createOperatorFormulaStmt(operator, localOperand, subLocal);
                    if (expr != null) {
                        block.addStmt(Jimple.v().newAssignStmt(localOperand, subLocal));
                    }
                }
            }
        }
        return block;
    }
}
