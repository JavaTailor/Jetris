package codegen.providers;

import codegen.Generator;
import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.operands.OperandGenerator;
import codegen.operators.Operator;
import config.FuzzingConfig;
import config.FuzzingRandom;
import fj.test.Gen;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.parser.node.AEmptyMethodBody;
import utils.ClassUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodProvider {

    /**
     * add a new method to class.
     * random select parameters' type and return value type.
     * just contains a return statement.
     * @param clazz add a method to clazz
     * @return a method contains a return statement
     */
    public static SootMethod createNewMethod(ClassInfo clazz) {

        // generator parameter
        List<Type> paras = new ArrayList<>();
        for (int i = 0; i < FuzzingRandom.nextChoice(FuzzingConfig.MAX_FUNS_PARAMS + 1); i++) {
            Type type = TypeProvider.anyType();
            if (FuzzingRandom.randomUpTo(FuzzingConfig.PROB_ARRAY_VALUE)) {
                type = ArrayType.v(type, FuzzingRandom.nextChoice(FuzzingConfig.MAX_ARRAY_DIM) + 1);
            }
            paras.add(type);
        }

        // generator return type
        Type retType;
        if (FuzzingRandom.randomUpTo(FuzzingConfig.PROB_VOID_METHOD)) {
            retType = VoidType.v();
        } else {
            retType = TypeProvider.anyType();
            if (FuzzingRandom.randomUpTo(FuzzingConfig.PROB_ARRAY_VALUE)) {
                retType = ArrayType.v(retType, FuzzingRandom.nextChoice(FuzzingConfig.MAX_ARRAY_DIM) + 1);
            }
        }

        return initializeNewMethod(clazz, NameProvider.genFuncName(), paras, retType, ModifierProvider.nextMethodModifier());
    }

    public static SootMethod createNewMethodWithType(ClassInfo clazz, ArrayList<Type> refTypes) {

        Type type;
        if (refTypes.size() > 0) {
            type = refTypes.get(FuzzingRandom.nextChoice(refTypes.size()));
        } else {
            type = TypeProvider.anyType();
        }
        // generator parameter
        List<Type> paras = new ArrayList<>();
        for (int i = 0; i < FuzzingRandom.nextChoice(FuzzingConfig.MAX_FUNS_PARAMS + 1); i++) {
            if (FuzzingRandom.randomUpTo(FuzzingConfig.PROB_ARRAY_VALUE)) {
                type = ArrayType.v(type, FuzzingRandom.nextChoice(FuzzingConfig.MAX_ARRAY_DIM) + 1);
            }
            paras.add(type);
        }

        // generator return type
        Type retType;
        if (FuzzingRandom.randomUpTo(FuzzingConfig.PROB_VOID_METHOD)) {
            retType = VoidType.v();
        } else {
            if (refTypes.size() > 0) {
                retType = refTypes.get(FuzzingRandom.nextChoice(refTypes.size()));
            } else {
                retType = TypeProvider.anyType();
            }
            if (FuzzingRandom.randomUpTo(FuzzingConfig.PROB_ARRAY_VALUE)) {
                retType = ArrayType.v(retType, FuzzingRandom.nextChoice(FuzzingConfig.MAX_ARRAY_DIM) + 1);
            }
        }

        return initializeNewMethod(clazz, NameProvider.genFuncName(), paras, retType, ModifierProvider.nextMethodModifier());
    }


    public static SootMethod initializeNewMethod(ClassInfo clazz, String methodName, List<Type> paras, Type retType, int modifier) {

        SootMethod method = new SootMethod(methodName, paras, retType);
        method.setModifiers(modifier);
        clazz.getSootClass().addMethod(method);
        clazz.addMethod(method);
        method.setActiveBody(Jimple.v().newBody(method));
        Body body = method.getActiveBody();

        if ((modifier & Modifier.STATIC) == 0)  {
            // add this pointer
            Local t = Jimple.v().newLocal(NameProvider.genVarName(), clazz.getSootClass().getType());
            Stmt stmt = Jimple.v().newIdentityStmt(t, Jimple.v().newThisRef(clazz.getSootClass().getType()));
            body.getLocals().add(t);
            body.getUnits().add(stmt);
            if (methodName.equals("<init>")) {
                body.getUnits().add((Unit) Jimple.v().newSpecialInvokeExpr(t, method.makeRef()));
            }
        } else {
            // update static methods
            StaticMethodProvider.staticMethods.add(method);
        }

        // generate identityStmt
        for (int i = 0; i < method.getParameterCount(); i++) {
            Local local = Jimple.v().newLocal(NameProvider.genVarName(), paras.get(i));
            Stmt stmt = Jimple.v().newIdentityStmt(local , Jimple.v().newParameterRef(paras.get(i), i));
            body.getUnits().add(stmt);
            body.getLocals().add(local);
        }
        // generator body (return stmt)
        Stmt retStmt;
        Local ret = Jimple.v().newLocal(NameProvider.genVarName(), retType);
        if (retType instanceof ArrayType) {

            if (((ArrayType) retType).numDimensions == 1) {

                int size = FuzzingRandom.nextChoice(FuzzingConfig.MAX_ARRAY_SIZE_PERDIM + 1);
                NewArrayExpr expr = Jimple.v().newNewArrayExpr(((ArrayType) retType).baseType, IntConstant.v(size));
                AssignStmt assign = Jimple.v().newAssignStmt(ret, expr);
                body.getUnits().add(assign);
                retStmt = Jimple.v().newReturnStmt(ret);
                body.getLocals().add(ret);
                body.getUnits().add(retStmt);
            } else {

                int length = 1;
                int dimensions = ((ArrayType) retType).numDimensions;
                int maxArrayLength = FuzzingConfig.MAX_ARRAY_LENGTH;
                List<Value> arraySize = new ArrayList<>();
                for (int i = 0; i < dimensions; i++) {
                    int maxsize = maxArrayLength / length;
                    maxsize = maxsize <= 20 ? maxsize : 20;
                    int size = FuzzingRandom.nextChoice(maxsize) + 1;
                    length = size * length;
                    arraySize.add(IntConstant.v(size));
                }
                NewMultiArrayExpr expr = Jimple.v().newNewMultiArrayExpr((ArrayType) retType, arraySize);
                AssignStmt assign = Jimple.v().newAssignStmt(ret, expr);
                body.getUnits().add(assign);
                retStmt = Jimple.v().newReturnStmt(ret);
                body.getLocals().add(ret);
                body.getUnits().add(retStmt);
            }
        } else if (retType instanceof VoidType) {

            retStmt = Jimple.v().newReturnVoidStmt();
            body.getUnits().add(retStmt);
        } else {

            if (retType instanceof RefType) {
                retStmt = Jimple.v().newReturnStmt(NullConstant.v());
            } else {
                retStmt = Jimple.v().newReturnStmt(PrimitiveValueProvider.next(retType));
            }
            body.getUnits().add(retStmt);
            BasicBlock block = Generator.nextVariable(clazz, method.getSignature(), retType);
            if (!block.getLocalVars().isEmpty()) {
                body.getUnits().removeLast();
                for (Local var : block.getLocalVars()) {
                    if (var.getType().equals(retType)) {
                        retStmt = Jimple.v().newReturnStmt(var);
                    }
                }
                body.getLocals().addAll(block.getLocalVars());
                body.getUnits().addAll(block.getStmts());
                body.getUnits().add(retStmt);
            }
        }

        // generate code body for newly generated function
        if (FuzzingRandom.randomUpTo(FuzzingConfig.PROB_INITIALIZE_METHOD)) {

            Operator defaultSeq = FuzzingRandom.randomUpTo(FuzzingConfig.PROB_INTERNAL_OPERATOR_GROUP);
            BasicBlock defaultBlock = defaultSeq.nextBlock(clazz, method.getSignature(), new ArrayList<>(Arrays.asList(retStmt)));
            body.getLocals().addAll(defaultBlock.getLocalVars());
            body.getUnits().insertBefore(defaultBlock.getStmts(), retStmt);
        }
        return method;
    }
}
