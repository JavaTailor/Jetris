package codegen.providers;

import codegen.blocks.*;
import config.FuzzingRandom;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.*;
import soot.util.Chain;
import soot.util.Numberable;
import config.FuzzingConfig;
import utils.InitDataFlowAnalysis;

import java.util.*;
import java.util.stream.Collectors;

import static utils.InsertionHelper.getNextUnit;

public class ElementsProvider {

    /**
     * @param clazz
     * @param methodSign
     * @return
     */
    public static Stmt getTarget(ClassInfo clazz, String methodSign) {

        SootMethod sootMethod = clazz.getMethodMaps().get(methodSign);
        Body sootMethodBody = sootMethod.retrieveActiveBody();

        List<Unit> validUnits = new ArrayList<>();
        for (Unit unit : sootMethodBody.getUnits()) {
            if (! (unit instanceof IdentityStmt) && unit instanceof Stmt && !(unit instanceof GotoStmt)){
                validUnits.add(unit);
            }
        }
        return (Stmt)validUnits.get(FuzzingRandom.nextChoice(validUnits.size()));
    }

    /**
     * @param clazz
     * @param methodSign
     * @return
     */
    public static List<Stmt> getTargets(ClassInfo clazz, String methodSign) {

        SootMethod sootMethod = clazz.getMethodMaps().get(methodSign);
        Body sootMethodBody = sootMethod.retrieveActiveBody();

        LoopNestTree loopNestTree = new LoopNestTree(sootMethodBody);
        List<Loop> loops = new ArrayList<>();
        //⚠️ 根据作用域选择可用loops
        if (loopNestTree.hasNestedLoops()) {

            for (Loop loop : loopNestTree) {
                if (loop.getHead() instanceof JIfStmt && loop.getBackJumpStmt() instanceof GotoStmt) {
                    loops.add(loop);
                }
            }
        }

        if (loops.size() > 0 && FuzzingRandom.flipCoin()) {

            //01 嵌套已有循环
            Loop loop = loops.get(FuzzingRandom.nextChoice(loops.size()));
            List<Stmt> loopStmts = loop.getLoopStatements();
            loopStmts.remove(loop.getHead());
            loopStmts.remove(loop.getBackJumpStmt());

            Set<Stmt> loopSet = new HashSet<>(loopStmts);
            ArrayList<Stmt> seq = new ArrayList<>();
            for (Unit unit: sootMethodBody.getUnits()) {
                if (loopSet.contains(unit))
                    seq.add((Stmt) unit);
            }
            return seq;
        } else {

            List<List<Stmt>> seqs = getSequentialStmts(sootMethod);
            if (seqs.size() > 0) {
                return seqs.get(FuzzingRandom.nextChoice(seqs.size()));
            } else {
                ArrayList<Stmt> res = new ArrayList<>();
                res.add(getTarget(clazz, methodSign));
                return res;
            }
        }
    }

    /**
     * @param clazz
     * @param methodSign
     * @return
     */
    public static List<List<Stmt>> getAllTargets(ClassInfo clazz, String methodSign) {

        List<List<Stmt>> allTargets = new ArrayList<>();

        SootMethod sootMethod = clazz.getMethodMaps().get(methodSign);
        Body sootMethodBody = sootMethod.retrieveActiveBody();
        LoopNestTree loopNestTree = new LoopNestTree(sootMethodBody);
        List<Loop> loops = new ArrayList<>();
        //⚠️ 根据作用域选择可用loops
        if (loopNestTree.hasNestedLoops()) {
            for (Loop loop : loopNestTree) {
                if (loop.getHead() instanceof JIfStmt && loop.getBackJumpStmt() instanceof GotoStmt) {
                    loops.add(loop);
                }
            }
        }
        //01 add all loops
        if (loops.size() > 0) {

            for (Loop loop : loops) {

                List<Stmt> loopStmts = loop.getLoopStatements();
                loopStmts.remove(loop.getHead());
                loopStmts.remove(loop.getBackJumpStmt());

                Set<Stmt> loopSet = new HashSet<>(loopStmts);
                ArrayList<Stmt> seq = new ArrayList<>();
                for (Unit unit: sootMethodBody.getUnits()) {
                    if (loopSet.contains(unit))
                        seq.add((Stmt) unit);
                }
                allTargets.add(seq);
            }
        }
        //02 add all sequential stmts
        List<List<Stmt>> seqs = getSequentialStmts(sootMethod);
        if (seqs.size() > 0) {
            allTargets.addAll(seqs);
        }
        return allTargets;
    }

    /**
     * TODO state2
     * 给定某一函数体中的一个位置，返回此位置前已经初始化的变量（Local）
     * @param clazz 类
     * @param methodSign 方法
     * @param target 插入点
     * @return
     */
    public static ArrayList<Local> getAvailableVars(ClassInfo clazz, String methodSign, Unit target) {
        SootMethod sootMethod = clazz.getMethodMaps().get(methodSign);
        Body body = sootMethod.retrieveActiveBody();
        InitDataFlowAnalysis analysis = new InitDataFlowAnalysis(new CompleteUnitGraph(body));
        return new ArrayList<>(analysis.getInLocal(target).toList());
    }

    public static HashMap<String, ArrayInfo> getAvailableArrayMaps(ClassInfo clazz, String methodSign, Unit target) {

        SootMethod sootMethod = clazz.getMethodMaps().get(methodSign);
        Body body = sootMethod.retrieveActiveBody();
        InitDataFlowAnalysis analysis = new InitDataFlowAnalysis(new TrapUnitGraph(body));
        HashMap<String, ArrayInfo> arrayMaps = new HashMap<>();

        for (Local local: analysis.getInLocal(target)) {
            if (local.getType() instanceof ArrayType) {
                Unit unit = analysis.getDefUnit(local);
                if (unit == null) {
                    continue;
                }
                Value rightOperand = ((JAssignStmt) unit).getRightOpBox().getValue();
                if (rightOperand instanceof JNewArrayExpr) {

                    ArrayInfo arrayInfo = new ArrayInfo(local, ((ArrayType)local.getType()).numDimensions, ((JNewArrayExpr) rightOperand).getSize(), methodSign);
                    arrayMaps.put(local.getName(), arrayInfo);
                } else if (rightOperand instanceof JNewMultiArrayExpr) {
                    ArrayInfo arrayInfo = new ArrayInfo(local, ((ArrayType)local.getType()).numDimensions, ((JNewMultiArrayExpr) rightOperand).getSizes(), methodSign);
                    arrayMaps.put(local.getName(), arrayInfo);
                }
            }
        }
        return arrayMaps;
    }

    public static List<Numberable> getAvailableValues(ClassInfo clazz, String methodSign, Unit targetUnit) {

        List<Numberable> candidateVars = new ArrayList<>();
        //通过flag判断是否使用局部变量
        if (targetUnit != null) {
            candidateVars.addAll(getAvailableVars(clazz, methodSign, targetUnit));
        }
        //获取所有field
        if (clazz.getMethodMaps().get(methodSign).isStatic()) {

            candidateVars.addAll(clazz.getSootClass()
                    .getFields()
                    .stream()
                    .filter(field -> (field.isStatic()
                            && !field.getDeclaration().contains("final"))).collect(Collectors.toList()));
        } else {
            candidateVars.addAll(clazz.getSootClass()
                    .getFields()
                    .stream()
                    .filter(field -> (!field.getDeclaration().contains("final"))).collect(Collectors.toList()));
        }
        Body methodbody = clazz.getMethodMaps().get(methodSign).retrieveActiveBody();
        LoopNestTree loops = new LoopNestTree(methodbody);
        for (Loop loop : loops) {
            if (loop.getHead() instanceof JIfStmt) {
                for (ValueBox useBox : ((JIfStmt) loop.getHead()).getCondition().getUseBoxes()) {
                    if (useBox.getValue() instanceof JimpleLocal) {
                        candidateVars.remove(useBox.getValue());
                    }
                }
            } else if (loop.getBackJumpStmt() instanceof JIfStmt) {
                for (ValueBox useBox : ((JIfStmt) loop.getBackJumpStmt()).getCondition().getUseBoxes()) {
                    if (useBox.getValue() instanceof JimpleLocal) {
                        candidateVars.remove(useBox.getValue());
                    }
                }
            } else { // do nothing
                if (loop.getHead() instanceof JAssignStmt) {
                    Unit nextUnit = getNextUnit(methodbody, loop.getHead());
                    if (nextUnit instanceof JIfStmt) {
                        for (ValueBox useBox : ((JIfStmt)nextUnit).getCondition().getUseBoxes()) {
                            if (useBox.getValue() instanceof JimpleLocal) {
                                candidateVars.remove(useBox.getValue());
                            }
                        }
                    }
                }
            }
        }
        return candidateVars;
    }

    /**
     * 获取method中除了 LOOP/IF/SWITCH/TRAP之外的顺序语句
     * @param sootMethod
     */
    public static List<List<Stmt>> getSequentialStmts(SootMethod sootMethod) {

        Body methodBody = sootMethod.retrieveActiveBody();

        Chain<Unit> methodUnits = methodBody.getUnits();

        List<List<Stmt>> seqStmts = new ArrayList<>();
        BlockGraph blockGraph = new ZonedBlockGraph(methodBody);

        List<Unit> redirectStmts = new ArrayList<>();
        for (Block block : blockGraph.getBlocks()) {

            Iterator<Unit> blockUnits = methodUnits.iterator(block.getHead(), block.getTail());
            ArrayList<Stmt> stmts = new ArrayList<>();
            boolean throwFlag = false;
            while (blockUnits.hasNext()) {

                Unit stmt = blockUnits.next();
                if (stmt instanceof IfStmt
                        || stmt instanceof SwitchStmt
                        || stmt instanceof Trap) {
                    break;
                } else {

                    if (stmt instanceof JThrowStmt) {
                        throwFlag = true;
                        break;
                    }
                    if (!(stmt instanceof IdentityStmt
                            || stmt instanceof GotoStmt
                            || stmt instanceof ReturnStmt
                            || stmt instanceof ReturnVoidStmt)) {
                        if (stmt.getBoxesPointingToThis().size() <= 0) {
                            stmts.add((Stmt) stmt);
                        } else {

                            if (stmt instanceof JAssignStmt) {
                                if (((JAssignStmt) stmt).getRightOp() instanceof JNewExpr) {
                                    redirectStmts.add(stmt);
                                }
                            }
                        }
                    }
                }
            }
            if (throwFlag) {
                stmts = removeThrowStmt(stmts);
            }
            if (stmts.size() > 0) {
                seqStmts.add(stmts);
            }
        }
        for (Unit redirectStmt : redirectStmts) {
            NopStmt nop = Jimple.v().newNopStmt();
            methodBody.getUnits().insertBeforeNoRedirect(nop, redirectStmt);
            redirectStmt.redirectJumpsToThisTo(nop);
        }
        return seqStmts;
    }

    public static ArrayList<Stmt> removeThrowStmt(ArrayList<Stmt> stmts) {

//        GuaranteedDefs
        if (stmts.size() <= 3) {
            stmts.clear();
        } else {
            int index = -1;
            for (Unit stmt : stmts) {
                if (stmt instanceof JAssignStmt && ((JAssignStmt) stmt).getRightOpBox().getValue() instanceof JNewExpr) {
                    JNewExpr value = (JNewExpr) ((JAssignStmt) stmt).getRightOpBox().getValue();
                    if (value.getType() instanceof RefType) {
                        if(((RefType) value.getType()).getSootClass().hasSuperclass()) {
                            if (((RefType) value.getType()).getSootClass().getSuperclass().getName().equals("java.lang.Exception")) {
                                index = stmts.indexOf(stmt);
                                break;
                            }
                        }
                    }
                } else if (stmt instanceof JInvokeStmt) {
                    ValueBox invokeBox = ((JInvokeStmt) stmt).getInvokeExprBox();
                    if (invokeBox instanceof JSpecialInvokeExpr) {
                        SootMethod method = ((JSpecialInvokeExpr) invokeBox).getMethod();
                        if (method.getDeclaringClass().getSuperclass().getName().equals("java.lang.Exception")) {
                            index = stmts.indexOf(stmt);
                            break;
                        }
                    }
                } else {
                    //do nothing
                }
            }
            if (index != -1) {
                ArrayList<Unit> tmp = new ArrayList<>();
                for (int i = 0; i < stmts.size(); i++) {
                    if (i >= index) {
                        tmp.add(stmts.get(i));
                    }
                }
                stmts.removeAll(tmp);
            }
        }
        return stmts;
    }
}
