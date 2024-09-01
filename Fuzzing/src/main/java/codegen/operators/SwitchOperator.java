package codegen.operators;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.blocks.SwitchBlock;
import codegen.providers.NameProvider;
import config.FuzzingRandom;
import flowinfo.AbstractNode;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JLookupSwitchStmt;
import soot.util.Numberable;
import config.FuzzingConfig;

import java.util.ArrayList;
import java.util.List;

import static codegen.providers.ElementsProvider.getAvailableValues;

public class SwitchOperator extends Generic {

    protected static SwitchOperator sop;

    public static SwitchOperator getInstance() {
        if (sop == null) {
            sop = new SwitchOperator();
        }
        return sop;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {

        SwitchBlock block = new SwitchBlock();
        block.setContents(targets);
        //01 获取已有变量，包括 field 以及 local （仅获取原始类型）
        List<Numberable> candidates = filterValidVars(getAvailableValues(clazz, methodSign, targets.get(0)));

        Local localCond;
        List<IntConstant> lookUpValues = new ArrayList<>();
        List<Stmt> labels = new ArrayList<>(); // 占位符
        int caseSize = FuzzingRandom.nextChoice(FuzzingConfig.MAX_SWITCH_CASES);
        if (candidates.size() > 0 && FuzzingRandom.flipCoin()) {

            Numberable condVar = candidates.get(FuzzingRandom.nextChoice(candidates.size()));
            if (condVar instanceof Local) {
                localCond = (Local) condVar;
            } else if (condVar instanceof SootField) {

                localCond = Jimple.v().newLocal(NameProvider.genVarName(), ((SootField)condVar).getType());
                block.addLocalVar(localCond);

                if (((SootField)condVar).isStatic()) {
                    StaticFieldRef staticFieldRef = Jimple.v().newStaticFieldRef(((SootField)condVar).makeRef());
                    JAssignStmt assignStmt = (JAssignStmt) Jimple.v().newAssignStmt(localCond, staticFieldRef);
                    block.setInit(assignStmt);
                } else {
                    InstanceFieldRef instanceFieldRef = Jimple.v().newInstanceFieldRef(clazz.getThisRef(methodSign), ((SootField) condVar).makeRef());
                    JAssignStmt assignStmt = (JAssignStmt) Jimple.v().newAssignStmt(localCond, instanceFieldRef);
                    block.setInit(assignStmt);
                }
            } else {
                throw new RuntimeException("Something wrong here: IfOperator - nextBlock");
            }
        } else {

            //01 声明局部变量
            localCond = Jimple.v().newLocal(NameProvider.genVarName(), IntType.v());
            block.addLocalVar(localCond);

            //02 对局部变量进行赋值
            AssignStmt condVarAssignment = Jimple.v().newAssignStmt(localCond, IntConstant.v(FuzzingRandom.nextChoice(FuzzingConfig.MAX_SWITCH_CASES)));
//            block.addStmt(condVarAssignment);
            block.setInit(condVarAssignment);
        }

        Stmt defaultLabel = Jimple.v().newNopStmt();
        for (int i = 0; i < caseSize; i++) {
            lookUpValues.add(IntConstant.v(i));

            Operator defaultSeq = FuzzingRandom.randomUpTo(FuzzingConfig.PROB_INTERNAL_OPERATOR_GROUP);
            BasicBlock defaultBlock = defaultSeq.nextBlock(clazz, methodSign, targets.subList(0, 1));
            defaultBlock.getStmts().add(Jimple.v().newGotoStmt(defaultLabel));
            block.addAllLocalVars(defaultBlock.getLocalVars());
            block.addAllStmts(defaultBlock.getStmts());

            labels.add(defaultBlock.getStmts().get(0));
        }
        block.addStmt(defaultLabel);
        //05 switch 语句
        JLookupSwitchStmt switchStmt = (JLookupSwitchStmt) Jimple.v().newLookupSwitchStmt(localCond, lookUpValues, labels, defaultLabel);
        //06 插入switch 语句
        block.setSwitchStmt(switchStmt);
        block.setInserationTarget(targets.get(0));
        return block;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets, AbstractNode nodeTemp) {
        return null;
    }

    /**
     * 可访问变量，包括local, instance field, static field
     * @param availableValues
     * @return
     */
    public List<Numberable> filterValidVars(List<Numberable> availableValues) {

        List<Numberable> vars = new ArrayList<>();
        for (Numberable value: availableValues) {
            if (value instanceof Local) {
                if (((Local) value).getType() instanceof IntType) {
                    vars.add(value);
                }
            } else if (value instanceof SootField) {
                if (((SootField) value).getType() instanceof IntType) {
                    vars.add(value);
                }
            }
        }
        return vars;
    }
}
