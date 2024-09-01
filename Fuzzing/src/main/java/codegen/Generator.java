package codegen;

import codegen.blocks.*;
import codegen.operands.OperandGenerator;
import codegen.operators.*;
import config.FuzzingRandom;
import soot.Local;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Stmt;
import config.FuzzingConfig;

import java.util.ArrayList;
import java.util.List;

import static codegen.providers.ElementsProvider.getTarget;
import static codegen.providers.ElementsProvider.getTargets;

public class Generator {

    public static ArrayList<Operator> GENERATORS = new ArrayList<>();

    static {
        GENERATORS.add(ApiOperator.getInstance());
        GENERATORS.add(ArrayOperator.getInstance());
        GENERATORS.add(ArithOperator.getInstance());
        GENERATORS.add(IfOperator.getInstance());
        GENERATORS.add(SwitchOperator.getInstance());
        GENERATORS.add(LoopOperator.getInstance());
        GENERATORS.add(TrapOperator.getInstance());
        GENERATORS.add(OperandGenerator.getInstance());
        GENERATORS.add(FuncOperator.getInstance());
    }

    public static BasicBlock nextBlockSingleTest(ClassInfo clazz, String methodSign) {
        return ApiOperator.getInstance().nextBlock(clazz, methodSign, getTargets(clazz, methodSign));
    }

    public static BasicBlock nextBlockTest(ClassInfo clazz, String methodSign) {
        //TODO random select operators
        return GENERATORS.get(FuzzingRandom.nextChoice(GENERATORS.size())).nextBlock(clazz, methodSign, getTargets(clazz, methodSign));
    }

    // 这个函数会破坏嵌套关系，只能用于生成最外层BasicBlock
    public static BasicBlock nextBlock(ClassInfo clazz, String methodSign) {
        Operator operator = FuzzingRandom.randomUpTo(FuzzingConfig.PROB_OPERATOR_GROUP);
        return operator.nextBlock(clazz, methodSign, getTargets(clazz, methodSign));
    }

    public static BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {
        //TODO random select operators，先再GENERATORS中逐步测试
        Operator operator = FuzzingRandom.randomUpTo(FuzzingConfig.PROB_OPERATOR_GROUP);
        return operator.nextBlock(clazz, methodSign, targets);
    }

    public static StmtBlock nextVariable(ClassInfo clazz, String methodSign, Type type) {
        return OperandGenerator.getInstance().nextVariable(clazz, methodSign, type, getTarget(clazz, methodSign));
    }

    public static StmtBlock nextVariable(ClassInfo clazz, String methodSign, Type type, Stmt target) {
        return OperandGenerator.getInstance().nextVariable(clazz, methodSign, type, target);
    }

    public static StmtBlock funcInvocation(ClassInfo clazz, String methodSign, Local invoker, SootMethod invokeFunc) {
        return OperandGenerator.getInstance().funcInvocation(clazz, methodSign, invoker, invokeFunc, getTarget(clazz, methodSign));
    }

    public static StmtBlock funcInvocation(ClassInfo clazz, String methodSign, Local invoker, SootMethod invokeFunc, Stmt target) {
        return OperandGenerator.getInstance().funcInvocation(clazz, methodSign, invoker, invokeFunc, target);
    }
}
