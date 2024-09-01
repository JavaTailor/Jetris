package codegen.operators;

import codegen.Generator;
import codegen.blocks.*;
import codegen.operands.OperandGenerator;
import codegen.providers.ElementsProvider;
import codegen.providers.NameProvider;
import codegen.providers.OperatorProvider;
import codegen.providers.TypeProvider;
import config.FuzzingRandom;
import flowinfo.AbstractNode;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.util.Numberable;
import config.FuzzingConfig;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Array 操作：
 * （1）创建 ：类型：原始类型/引用类型； 维度：一维数组/高维数组
 * （2）赋值 ：
 * （3）访问
 * （4）覆盖
 */
public class ArrayOperator extends Generic {

    protected static ArrayOperator aop;
    public static int MAX_ARRAY_ASSIGNMENT = 100;

    public static ArrayOperator getInstance() {
        if (aop == null) {
            aop = new ArrayOperator();
        }
        return aop;
    }

    /**
     * 创建新的数组相关的操作
     * @param clazz
     * @param methodSign
     * @return
     */
    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {

        StmtBlock block = new StmtBlock();
        if (targets == null || targets.size() < 1) {
            return block;
        }

        Stmt targetStmt = targets.get(FuzzingRandom.nextChoice(targets.size()));
        HashMap<String, ArrayInfo> candidateArrays = getAvailableArrays(clazz, methodSign, targetStmt);

        for (int count = 0; count < FuzzingConfig.MAX_BLOCK_INST && block.getStmts().size() < FuzzingConfig.MAX_BLOCK_INST; count++) {
            //可能无法生成正确的block，限制最多迭代次数
            if (FuzzingRandom.randomUpTo(FuzzingConfig.PROB_REUSE_VAR) && candidateArrays.size() > 0) {

                HashMap<Type, ArrayList<ArrayInfo>> classifiedArray = classifyArrayByBaseType(candidateArrays);

                ArrayList<Type> list = new ArrayList<>();
                list.addAll(classifiedArray.keySet());
                Type type = list.get(FuzzingRandom.nextChoice(list.size()));

                if (FuzzingRandom.randomUpTo(FuzzingConfig.PROB_NEW_ARRAY)) {

                    // create new array for uninitialized array
                    if (type instanceof RefType) {
                        if (type.toString().equals("UnInitialized")) {
                            block = initUninitializedArray(clazz, methodSign, classifiedArray, type, targets.get(0));
                        }
                    }
//                    if (type instanceof PrimType) {

                        //reuse existing array i.e., array arithmetic
                        //如果是原始类型，则创建相应的减价乘除操作
                        List<ArrayInfo> candidates = new ArrayList<>();
                        candidates.addAll(classifiedArray.get(type).stream().filter(arrayInfo -> arrayInfo.isInitialized()).collect(Collectors.toList()));
//                        for (Type type1 : classifiedArray.keySet()) {
//                            candidates.addAll(classifiedArray.get(type1).stream().filter(arrayInfo -> arrayInfo.isInitialized()).collect(Collectors.toList()));
//                        }
                        if (candidates.size() > 0) {
                            int ssize = FuzzingRandom.nextChoice(1, candidates.size());
                            ArrayList<ArrayInfo> computes = new ArrayList<>();
                            for (int i = 0; i < ssize; i++) {
                                ArrayInfo operand = candidates.get(FuzzingRandom.nextChoice(ssize));
                                //添加使用到的局部变量，用于后面验证作用域
                                if (operand.getArray() instanceof Local &&
                                        ((ArrayType)((Local) operand.getArray()).getType()).baseType instanceof PrimType) {
                                    block.addReusedVar((Local) operand.getArray());
                                    computes.add(operand);
                                }
                            }
                            StmtBlock arithBlock = arrayValueArithmetic(clazz, methodSign, type, computes);
                            block.addAllLocalVars(arithBlock.getLocalVars());
                            block.addAllReusedVars(arithBlock.getReusedVars());
                            block.addAllStmts(arithBlock.getStmts());
                        }
//                    }
//                    else {
//                        throw new RuntimeException("UNKNOWN Type: " + type);
//                    }

                    // assign array value
                    // 随机选择一个已有的数组，并对该数组进行随机的赋值
                    StmtBlock assignValueBlock = assignArrayValueRandomly(clazz, methodSign, classifiedArray, type, targets.get(0));
                    block.addAllLocalVars(assignValueBlock.getLocalVars());
                    block.addAllReusedVars(assignValueBlock.getReusedVars());
                    block.addAllStmts(assignValueBlock.getStmts());
                } else {

                    // override existing array
                    // 重写已有的数组
                    StmtBlock rewriteBlock = rewriteExistingArrayRandomly(clazz, methodSign, classifiedArray, type, targets.get(0));
                    block.addAllLocalVars(rewriteBlock.getLocalVars());
                    block.addAllReusedVars(rewriteBlock.getReusedVars());
                    block.addAllStmts(rewriteBlock.getStmts());
                }
            } else {
                //create new array
                StmtBlock newArrayBlock = Generator.nextVariable(clazz, methodSign, TypeProvider.arrayType, targets.get(0));
                block.addAllLocalVars(newArrayBlock.getLocalVars());
                block.addAllReusedVars(newArrayBlock.getReusedVars());
                block.addAllStmts(newArrayBlock.getStmts());
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

        Stmt targetStmt = targets.get(FuzzingRandom.nextChoice(targets.size()));
        HashMap<String, ArrayInfo> candidateArrays = getAvailableArrays(clazz, methodSign, targetStmt);

        for (int count = 0; count < FuzzingConfig.MAX_BLOCK_INST && block.getStmts().size() < FuzzingConfig.MAX_BLOCK_INST; count++) {
            //可能无法生成正确的block，限制最多迭代次数
            if (FuzzingRandom.randomUpTo(FuzzingConfig.PROB_REUSE_VAR) && candidateArrays.size() > 0) {

                HashMap<Type, ArrayList<ArrayInfo>> classifiedArray = classifyArrayByBaseType(candidateArrays);

                ArrayList<Type> list = new ArrayList<>();
                list.addAll(classifiedArray.keySet());
                Type type = list.get(FuzzingRandom.nextChoice(list.size()));

                if (FuzzingRandom.randomUpTo(FuzzingConfig.PROB_NEW_ARRAY)) {

                    // create new array for uninitialized array
                    if (type instanceof RefType) {
                        if (type.toString().equals("UnInitialized")) {
                            block = initUninitializedArray(clazz, methodSign, classifiedArray, type, targets.get(0));
                        }
                    }
//                    if (type instanceof PrimType) {

                    //reuse existing array i.e., array arithmetic
                    //如果是原始类型，则创建相应的减价乘除操作
                    List<ArrayInfo> candidates = new ArrayList<>();
                    candidates.addAll(classifiedArray.get(type).stream().filter(arrayInfo -> arrayInfo.isInitialized()).collect(Collectors.toList()));
//                        for (Type type1 : classifiedArray.keySet()) {
//                            candidates.addAll(classifiedArray.get(type1).stream().filter(arrayInfo -> arrayInfo.isInitialized()).collect(Collectors.toList()));
//                        }
                    if (candidates.size() > 0) {
                        int ssize = FuzzingRandom.nextChoice(1, candidates.size());
                        ArrayList<ArrayInfo> computes = new ArrayList<>();
                        for (int i = 0; i < ssize; i++) {
                            ArrayInfo operand = candidates.get(FuzzingRandom.nextChoice(ssize));
                            //添加使用到的局部变量，用于后面验证作用域
                            if (operand.getArray() instanceof Local &&
                                    ((ArrayType)((Local) operand.getArray()).getType()).baseType instanceof PrimType) {
                                block.addReusedVar((Local) operand.getArray());
                                computes.add(operand);
                            }
                        }
                        StmtBlock arithBlock = arrayValueArithmetic(clazz, methodSign, type, computes);
                        block.addAllLocalVars(arithBlock.getLocalVars());
                        block.addAllReusedVars(arithBlock.getReusedVars());
                        block.addAllStmts(arithBlock.getStmts());
                    }
//                    }
//                    else {
//                        throw new RuntimeException("UNKNOWN Type: " + type);
//                    }

                    // assign array value
                    // 随机选择一个已有的数组，并对该数组进行随机的赋值
                    StmtBlock assignValueBlock = assignArrayValueRandomly(clazz, methodSign, classifiedArray, type, targets.get(0));
                    block.addAllLocalVars(assignValueBlock.getLocalVars());
                    block.addAllReusedVars(assignValueBlock.getReusedVars());
                    block.addAllStmts(assignValueBlock.getStmts());
                } else {

                    // override existing array
                    // 重写已有的数组
                    StmtBlock rewriteBlock = rewriteExistingArrayRandomly(clazz, methodSign, classifiedArray, type, targets.get(0));
                    block.addAllLocalVars(rewriteBlock.getLocalVars());
                    block.addAllReusedVars(rewriteBlock.getReusedVars());
                    block.addAllStmts(rewriteBlock.getStmts());
                }
            } else {
                //create new array
                StmtBlock newArrayBlock = Generator.nextVariable(clazz, methodSign, TypeProvider.arrayType, targets.get(0));
                block.addAllLocalVars(newArrayBlock.getLocalVars());
                block.addAllReusedVars(newArrayBlock.getReusedVars());
                block.addAllStmts(newArrayBlock.getStmts());
            }
        }
        block.setInserationTarget(targetStmt);
        insertGotoStmt(clazz, methodSign, block);
        return block;
    }

    /**
     * 对未初始化的数组变量进行初始化
     * @param clazz
     * @param methodSign
     * @param classifiedArray
     * @param type
     * @return
     */
    public StmtBlock initUninitializedArray(ClassInfo clazz, String methodSign, HashMap<Type, ArrayList<ArrayInfo>> classifiedArray, Type type, Stmt targetUnit) {

        StmtBlock block = new StmtBlock();
        ArrayList<ArrayInfo> candidates = classifiedArray.get(type);
        ArrayInfo target = candidates.get(FuzzingRandom.nextChoice(candidates.size()));

        //变量要区分 Local 和 SootField
        if (target.getArray() instanceof Local) {

            block = Generator.nextVariable(clazz, methodSign, ((Local) target.getArray()).getType(), targetUnit);
            Local var = block.getLocalVars().get(0);
            for (Local v: block.getLocalVars())
                if (v.getType().equals(((Local) target.getArray()).getType()))
                    var = v;
            ArrayList<Value> arrSize = new ArrayList<>();
            for (Stmt stmt : block.getStmts()) {
                if (stmt instanceof JAssignStmt) {
                    if (((JAssignStmt) stmt).getRightOpBox().getValue() instanceof JNewArrayExpr) {
                        arrSize.add(((JNewArrayExpr) ((JAssignStmt) stmt).getRightOpBox().getValue()).getSize());
                    }
                }
            }
            JAssignStmt assignStmt = (JAssignStmt) Jimple.v().newAssignStmt((Local)target.getArray(), var);
            block.addStmt(assignStmt);
            target.setDimension(((ArrayType)var.getType()).numDimensions);
            target.setArraySize(arrSize);
        } else if (target.getArray() instanceof SootField) {

            block = Generator.nextVariable(clazz, methodSign, ((SootField) target.getArray()).getType(), targetUnit);
            Local var = block.getLocalVars().get(0);
            for (Local v: block.getLocalVars())
                if (v.getType().equals(((SootField) target.getArray()).getType()))
                    var = v;
            ArrayList<Value> arrSize = new ArrayList<>();
            for (Stmt stmt : block.getStmts()) {
                if (stmt instanceof JAssignStmt) {
                    if (((JAssignStmt) stmt).getRightOpBox().getValue() instanceof JNewArrayExpr) {
                        arrSize.add(((JNewArrayExpr) ((JAssignStmt) stmt).getRightOpBox().getValue()).getSize());
                    }
                }
            }
            target.setDimension(((ArrayType)var).numDimensions);
            target.setArraySize(arrSize);
            // SootField 中要区分是否为静态变量，构建fieldRef有差别
            if (((SootField) target.getArray()).isStatic()) {

                StaticFieldRef staticFieldRef = Jimple.v().newStaticFieldRef(((SootField) target.getArray()).makeRef());
                JAssignStmt assignStmt = (JAssignStmt) Jimple.v().newAssignStmt(staticFieldRef, var);
                block.addStmt(assignStmt);
            } else {

                InstanceFieldRef instanceFieldRef = Jimple.v().newInstanceFieldRef(clazz.getThisRef(methodSign), ((SootField) target.getArray()).makeRef());
                JAssignStmt assignStmt = (JAssignStmt) Jimple.v().newAssignStmt(instanceFieldRef, var);
                block.addStmt(assignStmt);
            }
        }
        target.setInitialized(true);
        return block;
    }

    /**
     * 为传入的所有数组进行 算术操作
     * operands 为运算数，运算符则在操作过程中随机生成
     * @param clazz
     * @param methodSign
     * @param type
     * @param operands
     * @return
     */
    public StmtBlock arrayValueArithmetic(ClassInfo clazz, String methodSign, Type type, ArrayList<ArrayInfo> operands) {

        StmtBlock block = new StmtBlock();
        if (operands.size() == 0) {
            return block;
        }
        if (operands.size() == 1) {
            //仅有一个操作数，直接将其赋值给一个局部变量之后，返回
            splitArrayAccess(clazz, methodSign, block, operands.get(0), type);
            return block;
        } else {

            Local newLocal = Jimple.v().newLocal(NameProvider.genVarName(), type);
            block.addLocalVar(newLocal);

            JArrayRef arrayRef = splitArrayAccess(clazz, methodSign, block, operands.get(0), type);
            JAssignStmt assign = (JAssignStmt) Jimple.v().newAssignStmt(newLocal, arrayRef);
            block.addStmt(assign);

            for (int i = 1; i < operands.size(); i++) {

                arrayRef = splitArrayAccess(clazz, methodSign, block, operands.get(i), type);
                Local tmpLocal = Jimple.v().newLocal(NameProvider.genVarName(), type);
                block.addLocalVar(tmpLocal);

                JAssignStmt assign2 = (JAssignStmt) Jimple.v().newAssignStmt(tmpLocal, arrayRef);
                block.addStmt(assign2);
                //随机生成一个操作符
                Expr operator = OperatorProvider.anyOperator(type);
                if (operator == null){
                    continue;
                }
                Object expr = OperatorProvider.createOperatorFormulaStmt(operator, newLocal, tmpLocal);

                if (expr != null) {

                    JAssignStmt assignToLocal = (JAssignStmt) Jimple.v().newAssignStmt(newLocal, (Value) expr);
                    block.addStmt(assignToLocal);
                    if (FuzzingRandom.flipCoin()) {
                        JAssignStmt assignToArray = (JAssignStmt) Jimple.v().newAssignStmt(arrayRef, newLocal);
                        block.addStmt(assignToArray);
                    }
                }
            }
        }
        return block;
    }

    /**
     * 对于多维数组，需要对数组进行拆分，e.g., 若对a[1][2]进行访问，则需要： (1) int[] b = a[1]; int c = b[2];
     * @param clazz
     * @param methodSign
     * @param block
     * @param operand
     * @param type
     * @return
     */
    public JArrayRef splitArrayAccess(ClassInfo clazz, String methodSign, BasicBlock block, ArrayInfo operand, Type type) {

        while (operand.getDimension() != 1) {

            int index = FuzzingRandom.nextChoice(operand.getArraySize().get(0).hashCode());

            JArrayRef arrayRef = (JArrayRef) Jimple.v().newArrayRef((Value) operand.getArray(), IntConstant.v(index));
            Local localRef = Jimple.v().newLocal(NameProvider.genVarName(), arrayRef.getType());
            block.addLocalVar(localRef);
            JAssignStmt assign = (JAssignStmt) Jimple.v().newAssignStmt(localRef, arrayRef);
            block.addStmt(assign);

            List<Value> arraySize = new ArrayList<>();
            for (int i = 1; i < operand.getArraySize().size(); i++) {
                arraySize.add(operand.getArraySize().get(i));
            }
            operand = new ArrayInfo(localRef, ((ArrayType)localRef.getType()).numDimensions, arraySize, operand.getMethodRef());
        }

        int index = FuzzingRandom.nextChoice(operand.getArraySize().get(0).hashCode());

        JArrayRef arrayRef = null;
        if (operand.getArray() instanceof Local) {
            arrayRef = (JArrayRef) Jimple.v().newArrayRef((Local) operand.getArray(), IntConstant.v(index));
        } else if (operand.getArray() instanceof SootField) {

            Local fieldLocal = Jimple.v().newLocal(NameProvider.genVarName(), ((SootField) operand.getArray()).getType());
            block.addLocalVar(fieldLocal);

            if (((SootField) operand.getArray()).isStatic()) {

                //static array
                StaticFieldRef staticFieldRef = Jimple.v().newStaticFieldRef(((SootField) operand.getArray()).makeRef());
                JAssignStmt assignStmt = (JAssignStmt) Jimple.v().newAssignStmt(fieldLocal, staticFieldRef);
                block.addStmt(assignStmt);
            } else {

                //instance array
                InstanceFieldRef instanceFieldRef = Jimple.v().newInstanceFieldRef(clazz.getThisRef(methodSign), ((SootField) operand.getArray()).makeRef());
                JAssignStmt assignStmt = (JAssignStmt) Jimple.v().newAssignStmt(fieldLocal, instanceFieldRef);
                block.addStmt(assignStmt);
            }

            arrayRef = (JArrayRef) Jimple.v().newArrayRef(fieldLocal, IntConstant.v(index));
        } else {
            //TODO
            System.out.println("Pay Attention Here!");
        }
        return arrayRef;
    }

    /**
     * 随机对已有数组的索引进行赋值
     * @param clazz
     * @param methodSign
     * @param classifiedArray
     * @param type
     * @param targetUnit
     * @return
     */
    public StmtBlock assignArrayValueRandomly(ClassInfo clazz, String methodSign, HashMap<Type, ArrayList<ArrayInfo>> classifiedArray, Type type, Stmt targetUnit) {

        StmtBlock block = new StmtBlock();
        ArrayList<ArrayInfo> candidates = classifiedArray.get(type);
        ArrayInfo target = candidates.get(FuzzingRandom.nextChoice(candidates.size()));

        if (target.getDimension() == 1) {

            int arraySize = target.getArraySize().get(0).hashCode();
            if (arraySize > MAX_ARRAY_ASSIGNMENT) {
                arraySize = MAX_ARRAY_ASSIGNMENT;
            }
            int round = FuzzingRandom.nextChoice(arraySize);
            if (target.getArray() instanceof Local) {

                for (int i = 0; i < round; i++) {
                    int index = FuzzingRandom.nextChoice(arraySize);
                    AssignStmt assign = Jimple.v().newAssignStmt(Jimple.v().newArrayRef((Value) target.getArray(), IntConstant.v(index)),
                            OperandGenerator.getInstance().nextValue(clazz, methodSign, block, ((ArrayType)((Local) target.getArray()).getType()).baseType, targetUnit));
                    block.addStmt(assign);
                }
                block.addReusedVar((Local) target.getArray());
            } else {
                //TODO: 数组类型为 sootfield
            }
        }
        return block;
    }

    /**
     * 随机重写已有的数组
     * @param clazz
     * @param methodSign
     * @param classifiedArray
     * @param type
     * @return
     */
    public StmtBlock rewriteExistingArrayRandomly(ClassInfo clazz, String methodSign, HashMap<Type, ArrayList<ArrayInfo>> classifiedArray, Type type, Stmt targetUnit) {

        StmtBlock block = new StmtBlock();
        ArrayList<ArrayInfo> candidates = classifiedArray.get(type);
        ArrayInfo target = candidates.get(FuzzingRandom.nextChoice(candidates.size()));
        if (target.getArray() instanceof Local) {
            block.addReusedVar((Local) target.getArray());
        }
        Type ttype = target.getArray() instanceof Local ? ((Local) target.getArray()).getType() : ((SootField) target.getArray()).getType();
        StmtBlock tblock = Generator.nextVariable(clazz, methodSign, ttype, targetUnit);
        block.addAllLocalVars(tblock.getLocalVars());
        block.addAllReusedVars(tblock.getReusedVars());
        block.addAllStmts(tblock.getStmts());

        Local tlocal = tblock.getLocalVars().get(0);
        for (Local var : tblock.getLocalVars()) {
            if (var.getType().equals(ttype)) {
                tlocal = var;
            }
        }
        if (target.getArray() instanceof Local) {
            JAssignStmt assignStmt = (JAssignStmt) Jimple.v().newAssignStmt((Value) target.getArray(), tlocal);
            block.addStmt(assignStmt);
        } else if (target.getArray() instanceof SootField){

            if (((SootField) target.getArray()).isStatic()) {
                //static array
                StaticFieldRef staticFieldRef = Jimple.v().newStaticFieldRef(((SootField) target.getArray()).makeRef());
                JAssignStmt assignStmt = (JAssignStmt) Jimple.v().newAssignStmt(staticFieldRef, tlocal);
                block.addStmt(assignStmt);
            } else {
                //Instance array
                InstanceFieldRef instanceFieldRef = Jimple.v().newInstanceFieldRef(clazz.getThisRef(methodSign), ((SootField) target.getArray()).makeRef());
                JAssignStmt assignStmt = (JAssignStmt) Jimple.v().newAssignStmt(instanceFieldRef, tlocal);
                block.addStmt(assignStmt);
            }
        } else {
            throw new RuntimeException("UNKNOWN ArrayType: " + target);
        }
        return block;
    }

    /**
     * 对数组按类型进行分类
     * @param arrayInfos
     * @return
     */
    public HashMap<Type, ArrayList<ArrayInfo>> classifyArrayByBaseType(HashMap<String, ArrayInfo> arrayInfos) {

        HashMap<Type, ArrayList<ArrayInfo>> maps = new HashMap<>();
        RefType unInitialized = RefType.v("UnInitialized");
        for (String s : arrayInfos.keySet()) {
            if (! arrayInfos.get(s).isInitialized()) {
                if (!maps.containsKey(unInitialized)) {
                    ArrayList<ArrayInfo> tmp = new ArrayList<>();
                    maps.put(unInitialized, tmp);
                }
                maps.get(unInitialized).add(arrayInfos.get(s));
            } else {

                Type baseType;
                if (arrayInfos.get(s).getArray() instanceof SootField) {

                    baseType = ((ArrayType)((SootField) arrayInfos.get(s).getArray()).getType()).baseType;
                } else if (arrayInfos.get(s).getArray() instanceof JimpleLocal) {
                    baseType = ((ArrayType)((JimpleLocal) arrayInfos.get(s).getArray()).getType()).baseType;
                } else {
                    throw new RuntimeException("UNKNOWN array type: " + arrayInfos.get(s));
                }
                if (! maps.containsKey(baseType)) {
                    ArrayList<ArrayInfo> tmp = new ArrayList<>();
                    maps.put(baseType, tmp);
                }
                maps.get(baseType).add(arrayInfos.get(s));
            }
        }
        return maps;
    }

    /**
     * 从 class 以及 对应的 method 中查找是否有数组类型的变量
     * @param clazz
     * @param methodSign
     * @return
     */
    public HashMap<String, ArrayInfo> getAvailableArrays(ClassInfo clazz, String methodSign, Unit targetUnit) {

        HashMap<String, ArrayInfo> arrayMaps = new HashMap<>();
        if (clazz.getSootClass().getFields().size() > 0) {

            if (clazz.getMethodMaps().get(methodSign).isStatic()) {
                // static initializer
                SootMethod clinit = clazz.getStaticInitializer();
                if (clinit != null) traverseMethodUnitsToFindArray(arrayMaps, clinit);
            } else {
                // default initializer
                SootMethod init = clazz.getDefaultInitializer();
                if (init != null) traverseMethodUnitsToFindArray(arrayMaps, init);
            }
        }
        if (targetUnit != null) {
            arrayMaps.putAll(ElementsProvider.getAvailableArrayMaps(clazz, methodSign, targetUnit));
            //TODO ZYQ: ERROR ? below adds all local variables to arrayMaps
//            for (Local local : clazz.getMethodMaps().get(methodSign).getActiveBody().getLocals()) {
//                if (local.getType() instanceof ArrayType && !arrayMaps.keySet().contains(local.getName())) {
//                    ArrayInfo uninitialized = new ArrayInfo(local);
//                    arrayMaps.put(local.getName(), uninitialized);
//                }
//            }
        }
        return arrayMaps;
    }

    /**
     * 遍历 函数内的 units 来查找数组类型的变量，并在遍历的过程中分析数组的初始化信息
     * @param arrayMaps
     * @param method
     * @return
     */
    public HashMap<String, ArrayInfo> traverseMethodUnitsToFindArray(HashMap<String, ArrayInfo> arrayMaps, SootMethod method) {

        boolean initflag = method.isConstructor() || method.isStaticInitializer();
        HashMap<String, ArrayInfo> cantAccessArrayMaps = new HashMap<>();
        HashMap<String, ArrayInfo> unInitializedMap = new HashMap<>();
        if (method == null) {
            return arrayMaps;
        }
        Body body = method.retrieveActiveBody();
        for (Unit unit : body.getUnits()) {

            if (unit instanceof JAssignStmt) {
                Value rightOperand = ((JAssignStmt) unit).getRightOpBox().getValue();
                if (rightOperand instanceof JNewArrayExpr) {

                    // create new array
                    Local leftOperand = (Local) ((JAssignStmt) unit).getLeftOpBox().getValue();
                    ArrayInfo arrayInfo = new ArrayInfo(leftOperand, ((ArrayType)leftOperand.getType()).numDimensions, ((JNewArrayExpr) rightOperand).getSize(), method.getName());
                    if (initflag) {
                        cantAccessArrayMaps.put(leftOperand.getName(), arrayInfo);
                    } else {
                        arrayMaps.put(leftOperand.getName(), arrayInfo);
                    }
                } else if (rightOperand instanceof JNewMultiArrayExpr) {

                    // create new multi array
                    Local leftOperand = (Local) ((JAssignStmt) unit).getLeftOpBox().getValue();
                    ArrayInfo arrayInfo = new ArrayInfo(leftOperand, ((ArrayType)leftOperand.getType()).numDimensions, ((JNewMultiArrayExpr) rightOperand).getSizes(), method.getName());
                    if (initflag) {
                        cantAccessArrayMaps.put(leftOperand.getName(), arrayInfo);
                    } else {
                        arrayMaps.put(leftOperand.getName(), arrayInfo);
                    }
                } else if (rightOperand.getType() instanceof ArrayType) {

                    //assign local array to array
                    Value leftOperand = ((JAssignStmt) unit).getLeftOpBox().getValue();

                    if (leftOperand instanceof JimpleLocal) {

                        //r3 = int[][]; int[] r4 = r3[0]
                        if (rightOperand instanceof JArrayRef || rightOperand instanceof StaticFieldRef) {
                            continue;
                        }
                        ArrayInfo rightOperandRef = null;
                        if (rightOperand instanceof Local) {
                            rightOperandRef = arrayMaps.get(((Local)rightOperand).getName());
                        } else if (rightOperand instanceof StaticFieldRef) {
                            rightOperandRef = arrayMaps.get(((StaticFieldRef) rightOperand).getField().getName());
                        } else if (rightOperand instanceof JInstanceFieldRef) {
                            rightOperandRef = arrayMaps.get(((JInstanceFieldRef) rightOperand).getField().getName());
                        }
                        if (rightOperandRef == null) {
                            ArrayInfo arrayInfo = new ArrayInfo((Numberable) leftOperand);
                            if (rightOperand instanceof Local) {
                                unInitializedMap.put(((Local) rightOperand).getName(), arrayInfo);
                            } else if (rightOperand instanceof StaticFieldRef) {
                                unInitializedMap.put(((StaticFieldRef) rightOperand).getField().getName(), arrayInfo);
                            } else if (rightOperand instanceof JInstanceFieldRef) {
                                unInitializedMap.put(((JInstanceFieldRef) rightOperand).getField().getName(), arrayInfo);
                            }
                            continue;
                        }
                        ArrayInfo arrayInfo = new ArrayInfo((JimpleLocal)leftOperand, rightOperandRef.getDimension(), rightOperandRef.getArraySize(), method.getName());
                        arrayMaps.put(((JimpleLocal) leftOperand).getName(), arrayInfo);

                    } else if (leftOperand instanceof JInstanceFieldRef) {

                        //assign local array to instance array
                        //如果左操作数为 instance field， 则右操作数必为Local
                        SootFieldRef leftFieldRef = ((JInstanceFieldRef) leftOperand).getFieldRef();
                        ArrayInfo rightOperandRef = null;
                        if (initflag) {
                            rightOperandRef = cantAccessArrayMaps.get(((Local)rightOperand).getName());
                        } else {
                            rightOperandRef = arrayMaps.get(((Local)rightOperand).getName());
                        }
                        if (rightOperandRef == null) {
                            ArrayInfo arrayInfo = new ArrayInfo((Numberable) leftOperand);
                            unInitializedMap.put(((Local)rightOperand).getName(), arrayInfo);
                            continue;
                        }
                        ArrayInfo arrayInfo = new ArrayInfo(leftFieldRef.resolve(), rightOperandRef.getDimension(), rightOperandRef.getArraySize(), method.getDeclaringClass().getName());
                        arrayMaps.put(leftFieldRef.resolve().getName(), arrayInfo);
                    } else if (leftOperand instanceof StaticFieldRef){

                        //assign local array to static array
                        //如果左操作数为 static field， 则右操作数必为Local
                        StaticFieldRef leftFieldRef = (StaticFieldRef) leftOperand;
                        ArrayInfo rightOperandRef = null;
                        if (initflag) {
                            rightOperandRef = cantAccessArrayMaps.get(((Local)rightOperand).getName());
                        } else {
                            rightOperandRef = arrayMaps.get(((Local)rightOperand).getName());
                        }
                        if (rightOperandRef == null) {
                            ArrayInfo arrayInfo = new ArrayInfo((Numberable) leftOperand);
                            unInitializedMap.put(((Local)rightOperand).getName(), arrayInfo);
                            continue;
                        }
                        ArrayInfo arrayInfo = new ArrayInfo(leftFieldRef.getField(), rightOperandRef.getDimension(), rightOperandRef.getArraySize(), method.getDeclaringClass().getName());
                        arrayMaps.put(leftFieldRef.getField().getName(), arrayInfo);
                    } else {
                        //do nothing
                        //JArrayRef $r9[0] = r2; multiple array
                    }
                } else {
                    //do nothing
                    //$r1[1] = 2 //赋值语句
                    //$i1 = r2[0] //多维数组赋值
                }
            }
        }
        // update uninitialized array map
        // ⚠️：unInitializedMap 的 key 为 right operand 的 Name， Value 为 left operand 的 Value(SootField/Local)
        for (String s : unInitializedMap.keySet()) {

            ArrayInfo arrayInfo = unInitializedMap.get(s);
            if (arrayMaps.get(s) != null) {
                arrayInfo.setDimension(arrayMaps.get(s).getDimension());
                arrayInfo.setArraySize(arrayMaps.get(s).getArraySize());
                arrayInfo.setInitialized(arrayMaps.get(s).isInitialized());
            }
            if (arrayInfo.getArray() instanceof SootField) {
                arrayMaps.put(((SootField) arrayInfo.getArray()).getName(), arrayInfo);
            } else {
                arrayMaps.put(((Local)arrayInfo.getArray()).getName(), arrayInfo);
            }
        }
        return arrayMaps;
    }
}
