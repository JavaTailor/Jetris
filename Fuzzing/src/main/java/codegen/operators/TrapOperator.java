package codegen.operators;

import codegen.Generator;
import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.blocks.StmtBlock;
import codegen.blocks.TrapBlock;
import codegen.providers.*;
import config.FuzzingRandom;
import flowinfo.AbstractNode;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JAssignStmt;
import config.FuzzingConfig;
import soot.jimple.internal.JRemExpr;
import soot.util.Chain;
import soot.util.Numberable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static codegen.providers.ElementsProvider.getAvailableValues;

public class TrapOperator extends Generic {

    protected static TrapOperator top;
    protected String TrapCounts = "TRAPCOUNT";

    public static TrapOperator getInstance() {
        if (top == null) {
            top = new TrapOperator();
        }
        return top;
    }

    /**
     * try-catch 模版
      */
    @Override
    public TrapBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {

        TrapBlock block = new TrapBlock();
        // 被包裹代码块contents为targets
        block.setContents(targets);

        Type type = ExceptionProvider.anyException();
        //01 create throw block body
        StmtBlock throwBlock = throwException(clazz, methodSign, type);
        block.addAllLocalVars(throwBlock.getLocalVars());
        block.setThrowBlock(throwBlock.getStmts());
        //02 create catch block body
        StmtBlock catchBlock = handleExceptionCount(clazz, type);
//        StmtBlock catchBlock = handleException(type);
        block.addAllLocalVars(catchBlock.getLocalVars());
        block.setCatchBlock(catchBlock.getStmts());

        //03 此处为添加是否抛出异常的if控制语块，防止goto语句被优化掉

        Local conVar = Jimple.v().newLocal(NameProvider.genVarName(), IntType.v());
        block.addLocalVar(conVar);
        JAssignStmt initStmt = FuzzingRandom.flipCoin() ? (JAssignStmt)Jimple.v().newAssignStmt(conVar, IntConstant.v(0))
                : (JAssignStmt)Jimple.v().newAssignStmt(conVar, IntConstant.v(1));
        block.setInitCond(initStmt);
        EqExpr cond = Jimple.v().newEqExpr(conVar, IntConstant.v(1));
        // 需要重定向
        IfStmt headStmt = Jimple.v().newIfStmt(cond, Jimple.v().newNopStmt());
        block.setHeadCond(headStmt);

        // goto语句，这里应该直接goto到return
        GotoStmt gotoStmt = Jimple.v().newGotoStmt(block.getCatchBlock().get(0));
        block.setGotoStmt(gotoStmt);

        SootClass throwable = Scene.v().getSootClass(type.toString());
        Trap trap = Jimple.v().newTrap(throwable, block.getThrowBlock().get(0), gotoStmt, block.getCatchBlock().get(0));
        block.setTrap(trap);
        block.setInserationTarget(targets.get(0));
        return block;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets, AbstractNode nodeTemp) {
        return null;
    }

    public StmtBlock throwException(ClassInfo clazz, String methodSign, Type type) {

        StmtBlock block = new StmtBlock();
        //01 创建异常变量
        Local throwsException = Jimple.v().newLocal(NameProvider.genVarName(), type);
        block.addLocalVar(throwsException);
        //02 异常赋值
        Stmt assignException = Jimple.v().newAssignStmt(throwsException, Jimple.v().newNewExpr((RefType) type));
        block.addStmt(assignException);
        //03 实例化异常
        SootClass exceptionClass = ((RefType) type).getSootClass();
        ArrayList<SootMethod> constructors = new ArrayList<>();
        for (SootMethod method : exceptionClass.getMethods()) {
            if (method.isPublic() && method.isConstructor()) {
                constructors.add(method);
            }
        }
        StmtBlock funcBlock = Generator.funcInvocation(clazz, methodSign, throwsException, constructors.get(FuzzingRandom.nextChoice(constructors.size())), null);
        block.addAllLocalVars(funcBlock.getLocalVars());
        block.addAllStmts(funcBlock.getStmts());
        //04 抛出异常
        Stmt throwStmt = Jimple.v().newThrowStmt(throwsException);
        block.addStmt(throwStmt);
        return block;
    }

    // 减小io
    public StmtBlock handleException(Type type) {

        StmtBlock block = new StmtBlock();
        Local exception = Jimple.v().newLocal(ExceptionProvider.genHandleException(), type);
        block.addLocalVar(exception);
        Stmt handlerStmt = Jimple.v().newIdentityStmt(exception, Jimple.v().newCaughtExceptionRef());
        VirtualInvokeExpr printStackTraceExpr = Jimple.v().newVirtualInvokeExpr(
                exception,
                RefType.v("java.lang.Throwable").getSootClass().getMethod("printStackTrace", Collections.<Type>emptyList()).makeRef()
        );
        InvokeStmt printStackTraceStmt = Jimple.v().newInvokeStmt(printStackTraceExpr);
        block.addStmt(handlerStmt);
        block.addStmt(printStackTraceStmt);
        return block;
    }

    public StmtBlock handleExceptionCount(ClassInfo clazz, Type type) {

        StmtBlock block = new StmtBlock();
        Local exception = Jimple.v().newLocal(ExceptionProvider.genHandleException(), type);
        block.addLocalVar(exception);
        Stmt handlerStmt = Jimple.v().newIdentityStmt(exception, Jimple.v().newCaughtExceptionRef());
        block.addStmt(handlerStmt);

        List<SootField> fields = clazz.getSootClass().getFields().stream().filter(field -> field.getName().equals(TrapCounts)).collect(Collectors.toList());
        SootField fieldCount = null;
        if (fields.size() > 0) {
            fieldCount = fields.get(0);
        }
        if (fieldCount == null) {
            //create new field
            fieldCount = new SootField(TrapCounts, LongType.v(), Modifier.PUBLIC | Modifier.STATIC);
            clazz.getSootClass().addField(fieldCount);
            //init new field
            JAssignStmt init = (JAssignStmt) Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(fieldCount.makeRef()), LongConstant.v(0));
            SootMethod clinit = clazz.getStaticInitializer();
            Body body = clinit.retrieveActiveBody();
            body.getUnits().addFirst(init);
        }
        Local localCount = Jimple.v().newLocal(NameProvider.genVarName(), LongType.v());
        block.addLocalVar(localCount);

        block.addStmt(Jimple.v().newAssignStmt(localCount, Jimple.v().newStaticFieldRef(fieldCount.makeRef())));
        JAddExpr trapCountIncrease = (JAddExpr) Jimple.v().newAddExpr(localCount, LongConstant.v(1));
        Local tmp = Jimple.v().newLocal(NameProvider.genVarName(), LongType.v());
        block.addLocalVar(tmp);
        block.addStmt(Jimple.v().newAssignStmt(tmp, trapCountIncrease));
        block.addStmt(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(fieldCount.makeRef()), tmp));

        return block;
    }
}