package codegen;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.operands.OperandGenerator;
import codegen.operators.*;
import config.FuzzingConfig;
import config.FuzzingRandom;
import flowinfo.AbstractNode;
import flowinfo.NodeSequence;

import java.util.ArrayList;

import static codegen.providers.ElementsProvider.getTargets;

public class FlowGuidedGenerator {

    public static ArrayList<Operator> seqOperators = new ArrayList<>();
    public static Operator apiOperator = ApiOperator.getInstance();
    public static Operator arrayOperator = ArrayOperator.getInstance();
    public static Operator arithOperator = ArithOperator.getInstance();
    public static Operator funcOperator = FuncOperator.getInstance();
    public static Operator operandGenerator = OperandGenerator.getInstance();
    public static Operator ifOperator = IfOperator.getInstance();
    public static Operator switchOperator = SwitchOperator.getInstance();
    public static Operator loopOperator = LoopOperator.getInstance();
    public static Operator trapOperator = TrapOperator.getInstance();

    static {
        seqOperators.add(apiOperator);
        seqOperators.add(arrayOperator);
        seqOperators.add(arithOperator);
        seqOperators.add(funcOperator);
        seqOperators.add(operandGenerator);
    }

    public static BasicBlock nextBlock(ClassInfo clazz, String methodSign) {
        Operator operator = FuzzingRandom.randomUpTo(FuzzingConfig.PROB_OPERATOR_GROUP);
        return operator.nextBlock(clazz, methodSign, getTargets(clazz, methodSign));
    }
    public static BasicBlock nextBlockInstanceTest(ClassInfo clazz, String methodSign, NodeSequence nodeSequence) {

        for (AbstractNode node : nodeSequence.getNodes()) {

            Operator operator = mapNodeTypeToOperator(node);
        }
        Operator operator = FuzzingRandom.randomUpTo(FuzzingConfig.PROB_OPERATOR_GROUP);
        return operator.nextBlock(clazz, methodSign, getTargets(clazz, methodSign));
    }

    public static Operator mapNodeTypeToOperator(AbstractNode node) {

//        COND, LOOP, SWITCH, SEQ, TRAP
        switch (node.type) {
            case COND:
                return ifOperator;
            case SWITCH:
                return switchOperator;
            case LOOP:
                return loopOperator;
            case TRAP:
                return trapOperator;
            case SEQ:
                int opIndex = FuzzingRandom.nextChoice(seqOperators.size());
                return seqOperators.get(opIndex);
            default:
                throw new RuntimeException("Unknown node type");
        }
    }
}
