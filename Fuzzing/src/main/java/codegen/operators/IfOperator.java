package codegen.operators;

import codegen.blocks.*;
import codegen.providers.NameProvider;
import codegen.providers.OperatorProvider;
import codegen.providers.PrimitiveValueProvider;
import config.FuzzingRandom;
import flowinfo.AbstractNode;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.util.Numberable;
import config.FuzzingConfig;

import java.util.ArrayList;
import java.util.List;

// 可用变量分析
import static codegen.providers.ElementsProvider.getAvailableValues;

public class IfOperator extends Generic {

    protected static IfOperator iop;

    public static IfOperator getInstance() {
        if (iop == null) {
            iop = new IfOperator();
        }
        return iop;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {

        CondBlock block = new CondBlock();
        block.setGotoTarget(Jimple.v().newNopStmt());

        //01 获取已有变量，包括 field 以及 local
        List<Numberable> candidates = new ArrayList<>();
        if (targets.size() > 0) {
            candidates = filterValidVars(getAvailableValues(clazz, methodSign, targets.get(0)));
        }
        Object condition;
        IfStmt headStmt;

        if (candidates.size() > 0 && FuzzingRandom.flipCoin(FuzzingConfig.PROB_REUSE_VAR)) {
            //create condition
            condition = reuseSeedVariable(clazz, methodSign, block, candidates);
        } else {
            //create condition
            condition = createNewVariable(block);
        }
        headStmt = Jimple.v().newIfStmt((Value) condition, block.getGotoTarget());
        block.setHeadStmt(headStmt);

        // create body
        // 由于if语句的跳转跳转到target的后一句
        if (FuzzingRandom.flipCoin(FuzzingConfig.PROB_REUSE_INST)) {
            block.setContents(targets);
        } else {
            //
            ArrayList<Stmt> target = new ArrayList<>();
            target.add(targets.get(0));
            block.setContents(target);

            //create internal body
            Operator defaultSeq = FuzzingRandom.randomUpTo(FuzzingConfig.PROB_INTERNAL_OPERATOR_GROUP);
            BasicBlock defaultBlock = defaultSeq.nextBlock(clazz, methodSign, targets);
            block.addAllLocalVars(defaultBlock.getLocalVars());
            block.addAllStmts(defaultBlock.getStmts());
        }
        insertReturnStmt(clazz, methodSign, block, targets.get(0));
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
                if (((Local) value).getType() instanceof PrimType || ((Local) value).getType() instanceof RefType) {
                    vars.add(value);
                }
            } else if (value instanceof SootField) {
                if (((SootField) value).getType() instanceof PrimType || ((SootField) value).getType() instanceof RefType) {
                    vars.add(value);
                }
            }
        }
        return vars;
    }

    /**
     *
     * @param clazz
     * @param methodSign
     * @param block
     * @param candidates
     * @return
     */
    public Object reuseSeedVariable(ClassInfo clazz, String methodSign, CondBlock block, List<Numberable> candidates) {

        Object condition;

        Numberable condVar = candidates.get(FuzzingRandom.nextChoice(candidates.size()));
        Local localCond;
        if (condVar instanceof Local) {
            localCond = (Local) condVar;
            //⚠️ 标记重用的局部变量
            block.setReuseVar(localCond);
        } else if (condVar instanceof SootField) {

            localCond = Jimple.v().newLocal(NameProvider.genVarName(), ((SootField)condVar).getType());
            block.addLocalVar(localCond);
            if (((SootField)condVar).isStatic()) {
                block.addInitStmt(Jimple.v().newAssignStmt(localCond, Jimple.v().newStaticFieldRef(((SootField)condVar).makeRef())));
            } else {
                block.addInitStmt(Jimple.v().newAssignStmt(localCond, Jimple.v().newInstanceFieldRef(clazz.getThisRef(methodSign), ((SootField) condVar).makeRef())));
            }
        } else {
            throw new RuntimeException("Something wrong here: IfOperator - nextBlock");
        }

        //以一定的概率去使用已定义变量内的引用类型变量。
        if (FuzzingRandom.flipCoin(FuzzingConfig.PROB_REUSE_REF_VAR)) {

            if (localCond instanceof RefType) {
                ArrayList<SootField> subFields = new ArrayList<>();
                for (SootField field: ((RefType) localCond.getType()).getSootClass().getFields()) {
                    if (field.isPublic()) {
                        subFields.add(field);
                    }
                }
                if (subFields.size() > 0) {
                    SootField subField = subFields.get(FuzzingRandom.nextChoice(subFields.size()));
                    Local subLocal = Jimple.v().newLocal(NameProvider.genVarName(), subField.getType());
                    block.addLocalVar(subLocal);
                    if (subField.isStatic()) {
                        block.addInitStmt(Jimple.v().newAssignStmt(subLocal, Jimple.v().newStaticFieldRef(subField.makeRef())));
                    } else {
                        block.addInitStmt(Jimple.v().newAssignStmt(subLocal, Jimple.v().newInstanceFieldRef(localCond, subField.makeRef())));
                    }
                    localCond = subLocal;
                }
            }
        }

        if (localCond.getType() instanceof PrimType) {
            //01 prim
            Expr relationOperator = FuzzingRandom.randomUpTo(FuzzingConfig.PROB_RELATION_GROUP);
            condition = OperatorProvider.createOperatorFormulaStmt(relationOperator, localCond, PrimitiveValueProvider.next(localCond.getType()));
        } else if (localCond.getType() instanceof RefType) {
            //02 reference
            condition = Jimple.v().newEqExpr(localCond, NullConstant.v());
        } else {
            throw new RuntimeException("This should not happen: IfOperator - nextBlock: " + localCond.getType());
        }
        return condition;
    }

    /**
     *
     * @param block
     * @return
     */
    public Object createNewVariable(CondBlock block) {

        //03 new
        Local condVar = Jimple.v().newLocal(NameProvider.genVarName(), IntType.v());
        block.addLocalVar(condVar);

        JAssignStmt initStmt;
        if (FuzzingRandom.flipCoin()) {
            initStmt = (JAssignStmt) Jimple.v().newAssignStmt(condVar, IntConstant.v(0));
        } else {
            initStmt = (JAssignStmt) Jimple.v().newAssignStmt(condVar, IntConstant.v(1));
        }
        block.addInitStmt(initStmt);

        return Jimple.v().newEqExpr(condVar, IntConstant.v(0));
    }
}