package utils;

import codegen.Generator;
import codegen.blocks.*;
import codegen.operators.Operator;
import config.FuzzingConfig;
import config.FuzzingRandom;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JLookupSwitchStmt;
import soot.toolkits.graph.*;

import java.util.*;

public class InsertionHelper {

    /******************************************************************************************************
     *************************************** Insertion Functions  *****************************************
     ******************************************************************************************************/

    /**
     * insert a new seq block into the method body
     * @param sootMethod
     * @param currentBlock
     */
    public static void insertStmtBlock(ClassInfo clazz, SootMethod sootMethod, BasicBlock currentBlock) {
        if (currentBlock.getInserationTarget() == null) return;
        Body sootMethodBody = sootMethod.retrieveActiveBody();
        sootMethodBody.getLocals().addAll(currentBlock.getLocalVars());

        sootMethodBody.getUnits().insertBefore(currentBlock.getStmts(), currentBlock.getInserationTarget());
    }

    /**
     * @param clazz
     * @param sootMethod
     * @param currentBlock
     */
    public static void insertIfBlock(ClassInfo clazz, SootMethod sootMethod, BasicBlock currentBlock) {

        Body sootMethodBody = sootMethod.retrieveActiveBody();

        List<Stmt> seq = currentBlock.getContents();
        List<Stmt> def = getDefinitions(clazz, sootMethod, seq);

        sootMethodBody.getLocals().addAll(currentBlock.getLocalVars());
        if (seq.size() - def.size() > 0 && FuzzingRandom.flipCoin()) {
            // 移除原chain中的def语句
            for (Stmt stmt : def) {
                seq.remove(stmt);
                sootMethodBody.getUnits().remove(stmt);
            }
            insertIfStmts(sootMethodBody, currentBlock, seq, def);
        }

        int nested = FuzzingRandom.nextChoice(FuzzingConfig.MAX_NESTED_SIZE);
        for (int i = 0; i < nested; i++) {

            Operator internalOperator = FuzzingRandom.randomUpTo(FuzzingConfig.PROB_OPERATOR_GROUP);
            BasicBlock internalBlock = internalOperator.nextBlock(clazz, sootMethod.getSignature(), seq);
            sootMethodBody.getLocals().addAll(internalBlock.getLocalVars());
            insertIfBlockStmts(sootMethodBody, currentBlock, internalBlock);
        }
//            BasicBlock block = Generator.nextBlock(clazz, sootMethod.getSignature(), seq.subList(seq.indexOf(currentBlock.getTarget()), seq.size()));
//            insertIfBlockStmts_N(sootMethodBody, currentBlock);

    }

    public static void insertIfStmts(Body sootMethodBody, BasicBlock currentBlock, List<Stmt> seq, List<Stmt> def) {

        Unit insertionPoint = seq.get(0);
        Unit target = ((CondBlock)currentBlock).getGotoTarget();

        // 先将def插入after前
        for (Stmt stmt : def) {
            Unit newUnit = (Unit) stmt.clone();
            sootMethodBody.getUnits().insertBeforeNoRedirect(newUnit, insertionPoint);
        }

        IfStmt headStmt = (IfStmt) ((CondBlock) currentBlock).getHeadStmt();
        sootMethodBody.getUnits().insertBefore(currentBlock.getStmts(), insertionPoint);
        sootMethodBody.getUnits().insertBeforeNoRedirect(headStmt, insertionPoint);
        // Nop语句插入到seq后一个位置
        if (seq.size() == 1 && ( seq.get(0) instanceof ReturnStmt || seq.get(0) instanceof ReturnVoidStmt)) {
            sootMethodBody.getUnits().insertBeforeNoRedirect(target, seq.get(seq.size() - 1));
        } else {
            sootMethodBody.getUnits().insertAfter(target, seq.get(seq.size() - 1));
        }
    }

    public static void insertIfBlockStmts_N(Body sootMethodBody, BasicBlock block) {

        //01 设置插入点
        Unit insertionPoint = block.getInserationTarget();
        Unit outerTarget = ((CondBlock)block).getGotoTarget();
        //02 插入外部if
        IfStmt headStmt = (IfStmt) ((CondBlock) block).getHeadStmt();
        sootMethodBody.getUnits().insertBeforeNoRedirect(headStmt, insertionPoint);
        sootMethodBody.getUnits().insertBefore(block.getStmts(), insertionPoint);
        //与insertIfStmts不同，这里最后插入 nop 语句，且是 insertBefore
        sootMethodBody.getUnits().insertBefore(outerTarget, insertionPoint);
    }

    public static void insertIfBlockStmts(Body sootMethodBody, BasicBlock outer, BasicBlock inner) {

        //01 设置插入点
        Unit insertionPoint = outer.getInserationTarget();
        Unit outerTarget = ((CondBlock)outer).getGotoTarget();
        //02 插入外部if
        IfStmt headStmt = (IfStmt) ((CondBlock) outer).getHeadStmt();
        sootMethodBody.getUnits().insertBefore(outer.getStmts(), insertionPoint);
        sootMethodBody.getUnits().insertBeforeNoRedirect(headStmt, insertionPoint);
        //与insertIfStmts不同，这里最后插入 nop 语句，且是 insertBefore
        sootMethodBody.getUnits().insertBefore(outerTarget, insertionPoint);

        if (inner instanceof StmtBlock) {
            //03 插入新生成的 StmtBlock 中的语句
            sootMethodBody.getUnits().insertBefore(inner.getStmts(), outerTarget);
        } else if (inner instanceof CondBlock) {
            //03 - 1 更新 if 语句的 target
            Stmt subHeadStmt = ((CondBlock) inner).getHeadStmt();
            //03 - 2 插入 if condition
            sootMethodBody.getUnits().insertBefore(inner.getStmts(), outerTarget);
            //03 - 3 插入 if Stmt
            sootMethodBody.getUnits().insertBeforeNoRedirect(subHeadStmt, outerTarget);
            //03 - 4 插入 sub if target
            sootMethodBody.getUnits().insertBeforeNoRedirect(((CondBlock) inner).getGotoTarget(), outerTarget);
        } else if (inner instanceof LoopBlock) {
            //03 插入 block 语句
            //03 - 1 插入 sub block 的初始化语句
            sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) inner).getInitStmt(), outerTarget);
            //03 - 2 插入 head Stmt
            sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) inner).getHeadStmt(), outerTarget);
            //03 - 3 插入 step 更新语句
            sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) inner).getStepStmt(), outerTarget);
            //03 - 4 插入 goto 语句
            sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) inner).getBackJumpStmt(), outerTarget);
            //03 - 5 插入 sub loop target
            sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) inner).getGotoTarget(), outerTarget);
        } else if (inner instanceof SwitchBlock) {
            //03 - 1 插入可能的局部变量初始化语句
            sootMethodBody.getUnits().insertBefore(inner.getStmts(), outerTarget);
            sootMethodBody.getUnits().insertBeforeNoRedirect(((SwitchBlock) inner).getSwitchStmt(), outerTarget);
            Stmt subSwitchStmt = ((SwitchBlock) inner).getSwitchStmt();
            for (int i1 = 0; i1 < ((SwitchStmt) subSwitchStmt).getTargets().size(); i1++) {
                ((JLookupSwitchStmt) subSwitchStmt).setTarget(i1, outerTarget);
            }
            ((JLookupSwitchStmt) subSwitchStmt).setDefaultTarget(outerTarget);
        } else if (inner instanceof TrapBlock) {

            Unit innerHead = ((TrapBlock) inner).getInitCond();
            //03 - 1 调整goto跳转到target(内部trap外，if后)
            GotoStmt gotoStmt = ((TrapBlock) inner).getGotoStmt();
            gotoStmt.setTarget(outerTarget);
            //03 - 2
            Stmt innerHeadStmt = ((TrapBlock) inner).getHeadCond();
            ((IfStmt) innerHeadStmt).setTarget(gotoStmt);
            //03 - 3 insert if condition and if stmt
            sootMethodBody.getUnits().insertBeforeNoRedirect(((TrapBlock) inner).getInitCond(), outerTarget);
            sootMethodBody.getUnits().insertBeforeNoRedirect(innerHeadStmt, outerTarget);
            //03 - 4 插入try语句
            for (Stmt stmt : ((TrapBlock) inner).getThrowBlock()) {
                sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, outerTarget);
            }
            //03 - 5 插入goto语句
            sootMethodBody.getUnits().insertBeforeNoRedirect(gotoStmt, outerTarget);
            //03 - 6 插入catch语句
            for (Stmt stmt : ((TrapBlock) inner).getCatchBlock()) {
                sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, outerTarget);
            }
            //03 - 7 set trap
            Trap trap = ((TrapBlock) inner).getTrap();
            trap.setBeginUnit(innerHead);
            trap.setEndUnit(gotoStmt);
            //03 - 8 插入trap
            sootMethodBody.getTraps().add(((TrapBlock) inner).getTrap());
        }
    }

    /**
     * 插入 loop block
     * 01 - 获取当前函数中已经存在的 loop， 随机选择一个进行嵌套
     * 02 - 作为新的loop插入，为其创建新的循环体
     * @param clazz
     * @param sootMethod
     * @param currentBlock
     */
    public static void insertLoopBlock(ClassInfo clazz, SootMethod sootMethod, BasicBlock currentBlock) {

        Body sootMethodBody = sootMethod.retrieveActiveBody();

        List<Stmt> seq = ((LoopBlock)currentBlock).getContents();
        List<Stmt> def = getDefinitions(clazz, sootMethod, seq);

        sootMethodBody.getLocals().addAll(currentBlock.getLocalVars());
        if (seq.size() - def.size() > 0 && FuzzingRandom.flipCoin()) {

            // 移除原chain中的def语句
            for (Stmt stmt : def) {
                seq.remove(stmt);
                sootMethodBody.getUnits().remove(stmt);
            }
            insertLoopStmts(sootMethodBody, currentBlock, seq, def);
        } else {
            BasicBlock block = Generator.nextBlock(clazz, sootMethod.getSignature(), seq.subList(seq.indexOf(currentBlock.getInserationTarget()), seq.size()));
            sootMethodBody.getLocals().addAll(block.getLocalVars());
            insertLoopBlockStmts(sootMethodBody, currentBlock, block);
        }
    }

    /**
     * @param sootMethodBody
     * @param currentBlock
     * @param seq
     * @param def
     */
    public static void insertLoopStmts(Body sootMethodBody, BasicBlock currentBlock, List<Stmt> seq, List<Stmt> def) {

        Unit insertionPoint = seq.get(0);
        Unit target = ((LoopBlock)currentBlock).getGotoTarget();

        // 先将def插入after前
        for (Stmt stmt : def) {
            Unit newUnit = (Unit) stmt.clone();
            sootMethodBody.getUnits().insertBeforeNoRedirect(newUnit, insertionPoint);
        }

        //01 插入 loopIndex 的初始化语句
        sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) currentBlock).getInitStmt(), insertionPoint);
        //02 插入 if 语句
        sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) currentBlock).getHeadStmt(), insertionPoint);
        //03 中间是seqs， 将Loop结束的Nop语句插入到seq后一个位置
        if (seq.size() == 1 && ( seq.get(0) instanceof ReturnStmt || seq.get(0) instanceof ReturnVoidStmt)) {
            sootMethodBody.getUnits().insertBeforeNoRedirect(target, seq.get(seq.size() - 1));
        } else {
            sootMethodBody.getUnits().insertAfter(target, seq.get(seq.size() - 1));
        }
        //04 loopStep increase stmt
        sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) currentBlock).getStepStmt(), target);
        //05 insert backjump stmt
        sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) currentBlock).getBackJumpStmt(), target);
    }

    /**
     * @param sootMethodBody
     * @param outer
     * @param inner
     */
    public static void insertLoopBlockStmts(Body sootMethodBody, BasicBlock outer, BasicBlock inner) {

        Unit insertionPoint = outer.getInserationTarget();
        //⚠️ pay attention here, insert sub block before step increase stmt
        Unit outerTarget = ((LoopBlock)outer).getStepStmt();

        //01 插入 loopIndex 的初始化语句
        sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) outer).getInitStmt(), insertionPoint);
        //02 插入 loop - if 语句
        sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) outer).getHeadStmt(), insertionPoint);
        //04 insert step increase stmt
        sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) outer).getStepStmt(), insertionPoint);
        //05 insert back jump stmt
        sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) outer).getBackJumpStmt(), insertionPoint);
        //06 Loop结束的Nop语句插入到seq后一个位置
        sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) outer).getGotoTarget(), insertionPoint);

        //03 中间是seqs
        if (inner instanceof StmtBlock) {
            sootMethodBody.getUnits().insertBefore(inner.getStmts(), outerTarget);
        } else if (inner instanceof CondBlock) {
            //03 - 1 更新 if 语句的 target
            Stmt subHeadStmt = ((CondBlock) inner).getHeadStmt();
            //03 - 2 插入 if condition
            sootMethodBody.getUnits().insertBefore(inner.getStmts(), outerTarget);
            //03 - 3 插入 if Stmt
            sootMethodBody.getUnits().insertBeforeNoRedirect(subHeadStmt, outerTarget);
            //03 - 4 插入 sub if target
            sootMethodBody.getUnits().insertBeforeNoRedirect(((CondBlock) inner).getGotoTarget(), outerTarget);
        } else if (inner instanceof LoopBlock) {
            //03 插入 block 语句
            //03 - 1 插入 sub block 的初始化语句
            sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) inner).getInitStmt(), outerTarget);
            //03 - 2 插入 head Stmt
            sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) inner).getHeadStmt(), outerTarget);
            //03 - 3 插入 step 更新语句
            sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) inner).getStepStmt(), outerTarget);
            //03 - 4 插入 goto 语句
            sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) inner).getBackJumpStmt(), outerTarget);
            //03 - 5 插入 sub loop target
            sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) inner).getGotoTarget(), outerTarget);
        } else if (inner instanceof SwitchBlock) {
            //03 - 1 插入可能的局部变量初始化语句
            sootMethodBody.getUnits().insertBefore(inner.getStmts(), outerTarget);
            sootMethodBody.getUnits().insertBeforeNoRedirect(((SwitchBlock) inner).getSwitchStmt(), outerTarget);
            Stmt subSwitchStmt = ((SwitchBlock) inner).getSwitchStmt();
            for (int i1 = 0; i1 < ((SwitchStmt) subSwitchStmt).getTargets().size(); i1++) {
                ((JLookupSwitchStmt) subSwitchStmt).setTarget(i1, outerTarget);
            }
            ((JLookupSwitchStmt) subSwitchStmt).setDefaultTarget(outerTarget);
        } else if (inner instanceof TrapBlock) {

            Unit innerHead = ((TrapBlock) inner).getInitCond();
            //03 - 1 调整goto跳转到target(内部trap外，if后)
            GotoStmt gotoStmt = ((TrapBlock) inner).getGotoStmt();
            gotoStmt.setTarget(outerTarget);
            //03 - 2
            Stmt innerHeadStmt = ((TrapBlock) inner).getHeadCond();
            ((IfStmt) innerHeadStmt).setTarget(gotoStmt);
            //03 - 3 insert if condition and if stmt
            sootMethodBody.getUnits().insertBeforeNoRedirect(((TrapBlock) inner).getInitCond(), outerTarget);
            sootMethodBody.getUnits().insertBeforeNoRedirect(innerHeadStmt, outerTarget);
            //03 - 4 插入try语句
            for (Stmt stmt : ((TrapBlock) inner).getThrowBlock()) {
                sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, outerTarget);
            }
            //03 - 5 插入goto语句
            sootMethodBody.getUnits().insertBeforeNoRedirect(gotoStmt, outerTarget);
            //03 - 6 插入catch语句
            for (Stmt stmt : ((TrapBlock) inner).getCatchBlock()) {
                sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, outerTarget);
            }
            //03 - 7 set trap
            Trap trap = ((TrapBlock) inner).getTrap();
            trap.setBeginUnit(innerHead);
            trap.setEndUnit(gotoStmt);
            //03 - 8 插入trap
            sootMethodBody.getTraps().add(((TrapBlock) inner).getTrap());
        }
    }

    public static void insertSwitchBlock(ClassInfo clazz, SootMethod sootMethod, BasicBlock currentBlock) {

        //01 设置插入点
        Unit insertionPoint = currentBlock.getInserationTarget();
        //02 获取顺序语句，为default 执行语句
        List<Stmt> seq = ((SwitchBlock) currentBlock).getContents();
        //03 获取switch语句
        JLookupSwitchStmt switchStmt = ((SwitchBlock) currentBlock).getSwitchStmt();
        //04 设置switch中所有label的跳转对象nop， 以及提前创建不同label的代码体
        NopStmt nop = Jimple.v().newNopStmt();
        ArrayList<BasicBlock> blocks = new ArrayList<>();
        for (int i = 0; i < switchStmt.getTargetCount(); i++) {
            switchStmt.setTarget(i, nop);
            blocks.add(Generator.nextBlock(clazz, sootMethod.getSignature(), seq.subList(0, 1)));
        }
        //05 插入swtich设计变量的初始化操作
        Body sootMethodBody = sootMethod.retrieveActiveBody();
        sootMethodBody.getLocals().addAll(currentBlock.getLocalVars());
        sootMethodBody.getUnits().insertBefore(currentBlock.getStmts(), insertionPoint);
        //06 插入switch 语句
        sootMethodBody.getUnits().insertBeforeNoRedirect(switchStmt, insertionPoint);
        //07 插入不同label的代码体
        for (int i = 0; i < blocks.size(); i++) {

            BasicBlock block = blocks.get(i);
            sootMethodBody.getLocals().addAll(block.getLocalVars());

            //08 记录每个block 的第一条语句作为switch语句不同case的target
            Unit condTarget = null;
            //09 创建每个case body 最后的goto stmt
            GotoStmt gotoStmt = Jimple.v().newGotoStmt(nop);

            if (block instanceof StmtBlock) {
                block.addStmt(gotoStmt);
                condTarget = block.getStmts().get(0);
                sootMethodBody.getUnits().insertBefore(block.getStmts(), insertionPoint);
            } else if (block instanceof CondBlock) {

                //10 - 1 更新 if 语句的 target
                Stmt subHeadStmt = ((CondBlock) block).getHeadStmt();
                //10 - 2 插入 if condition
                sootMethodBody.getUnits().insertBefore(block.getStmts(), insertionPoint);
                //10 - 3 插入 if Stmt
                sootMethodBody.getUnits().insertBeforeNoRedirect(subHeadStmt, insertionPoint);
                //10 - 4 插入 sub if target
                sootMethodBody.getUnits().insertBeforeNoRedirect(((CondBlock) block).getGotoTarget(), insertionPoint);
                //10 - 5 插入最后的goto语句
                sootMethodBody.getUnits().insertBeforeNoRedirect(gotoStmt, insertionPoint);
                if (block.getStmts().size() > 0) {
                    condTarget = block.getStmts().get(0);
                } else {
                    condTarget = subHeadStmt;
                }
            } else if (block instanceof LoopBlock) {

                //10 插入 block 语句
                //10 - 1 插入 sub block 的初始化语句
                sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) block).getInitStmt(), insertionPoint);
                //10 - 2 插入 head Stmt
                sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) block).getHeadStmt(), insertionPoint);
                //10 - 3 插入 step 更新语句
                sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) block).getStepStmt(), insertionPoint);
                //10 - 4 插入 goto 语句
                sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) block).getBackJumpStmt(), insertionPoint);
                //10 - 5 插入 sub loop target
                sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) block).getGotoTarget(), insertionPoint);
                //10 - 6 插入 case 语句中最后一个 goto 语句
                sootMethodBody.getUnits().insertBeforeNoRedirect(gotoStmt, insertionPoint);
                if (((LoopBlock) block).getInitStmt() != null) {
                    condTarget = ((LoopBlock) block).getInitStmt();
                } else {
                    condTarget = ((LoopBlock) block).getHeadStmt();
                }
            } else if (block instanceof SwitchBlock) {

                //10 - 1 插入可能的局部变量初始化语句
                sootMethodBody.getUnits().insertBefore(block.getStmts(), insertionPoint);
                Stmt subSwitchStmt = ((SwitchBlock) block).getSwitchStmt();
                sootMethodBody.getUnits().insertBeforeNoRedirect(subSwitchStmt, insertionPoint);
                for (int i1 = 0; i1 < ((SwitchStmt) subSwitchStmt).getTargets().size(); i1++) {
                    ((JLookupSwitchStmt) subSwitchStmt).setTarget(i1, insertionPoint);
                }
                ((JLookupSwitchStmt) subSwitchStmt).setDefaultTarget(insertionPoint);
                sootMethodBody.getUnits().insertBeforeNoRedirect(gotoStmt, insertionPoint);
                if (block.getStmts().size() > 0) {
                    condTarget = block.getStmts().get(0);
                } else {
//                    NopStmt placeHolder = Jimple.v().newNopStmt();
//                    sootMethodBody.getUnits().insertBeforeNoRedirect(placeHolder, subSwitchStmt);
                    condTarget = subSwitchStmt;
                }
            } else if (block instanceof TrapBlock) {

                Unit innerHead = ((TrapBlock) block).getInitCond();
                //10 - 1 调整goto跳转到target(内部trap外，if后)
                GotoStmt innerGotoStmt = ((TrapBlock) block).getGotoStmt();
                innerGotoStmt.setTarget(insertionPoint);
                //10 - 2
                Stmt innerHeadStmt = ((TrapBlock) block).getHeadCond();
                ((IfStmt) innerHeadStmt).setTarget(innerGotoStmt);
                //10 - 3 insert if condition and if stmt
                sootMethodBody.getUnits().insertBeforeNoRedirect(((TrapBlock) block).getInitCond(), insertionPoint);
                sootMethodBody.getUnits().insertBeforeNoRedirect(innerHeadStmt, insertionPoint);
                //10 - 4 插入try语句
                for (Stmt stmt : ((TrapBlock) block).getThrowBlock()) {
                    sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, insertionPoint);
                }
                //10 - 5 插入goto语句
                sootMethodBody.getUnits().insertBeforeNoRedirect(innerGotoStmt, insertionPoint);
                //10 - 6 插入catch语句
                for (Stmt stmt : ((TrapBlock) block).getCatchBlock()) {
                    sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, insertionPoint);
                }
                //10 - 7 ⚠️ pay attention here
                sootMethodBody.getUnits().insertBeforeNoRedirect(gotoStmt, insertionPoint);
                //10 - 8 set trap
                Trap trap = ((TrapBlock) block).getTrap();
                trap.setBeginUnit(innerHead);
                trap.setEndUnit(innerGotoStmt);
                //10 - 9 插入trap
                sootMethodBody.getTraps().add(((TrapBlock) block).getTrap());
                //10 - 10
                condTarget = innerHead;
            }
            //set switch cases target
            switchStmt.setTarget(i, condTarget);
        }
        sootMethodBody.getUnits().insertBefore(nop, insertionPoint);
        switchStmt.setDefaultTarget(nop);
    }

    /**
     * @param clazz
     * @param sootMethod
     * @param currentBlock
     */
    public static void insertTrapBlock(ClassInfo clazz, SootMethod sootMethod, BasicBlock currentBlock) {

        Body sootMethodBody = sootMethod.retrieveActiveBody();
        List<Stmt> seq = ((TrapBlock)currentBlock).getContents();
        List<Stmt> def = getDefinitions(clazz, sootMethod, seq);

        sootMethodBody.getLocals().addAll(currentBlock.getLocalVars());
        if (seq.size() - def.size() > 0 && FuzzingRandom.flipCoin()) {

            // 移除原chain中的def语句
            seq.removeAll(def);
            sootMethodBody.getUnits().removeAll(def);
            insertTrapStmt(sootMethodBody, currentBlock, seq, def);
        } else {

            BasicBlock block = Generator.nextBlock(clazz, sootMethod.getSignature(), seq.subList(seq.indexOf(currentBlock.getInserationTarget()), seq.size()));
            sootMethodBody.getLocals().addAll(block.getLocalVars());
            insertTrapBlockStmts(sootMethodBody, currentBlock, block);
        }
    }

    public static void insertTrapStmt(Body sootMethodBody, BasicBlock currentBlock, List<Stmt> seq, List<Stmt> def) {

        //01 先将def插入after前
        for (Stmt stmt : def) {
            sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, seq.get(0));
        }
        //02 语句插入到seq后一个位置
        Unit tail = getNextUnit(sootMethodBody, seq.get(seq.size() - 1));
        //03 调整goto跳转到target
        GotoStmt gotoStmt = ((TrapBlock) currentBlock).getGotoStmt();
        gotoStmt.setTarget(tail);
        //04 set target of if stmt
        Stmt ifStmt = ((TrapBlock) currentBlock).getHeadCond();
        ((IfStmt) ifStmt).setTarget(gotoStmt);
        //05 insert if condition and if stmt
        sootMethodBody.getUnits().insertBeforeNoRedirect(((TrapBlock) currentBlock).getInitCond(), tail);
        sootMethodBody.getUnits().insertBeforeNoRedirect(ifStmt, tail);
        //06 insert try block body
        for (Stmt stmt : ((TrapBlock) currentBlock).getThrowBlock()) {
            sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, tail);
        }
        //07 插入goto语句
        sootMethodBody.getUnits().insertBeforeNoRedirect(gotoStmt, tail);
        //08 插入catch语句
        for (Stmt stmt : ((TrapBlock) currentBlock).getCatchBlock()) {
            sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, tail);
        }
        //09 set trap stmt
        Trap trap = ((TrapBlock) currentBlock).getTrap();
        trap.setBeginUnit(seq.get(0));
        trap.setEndUnit(gotoStmt);
        //10 插入trap
        sootMethodBody.getTraps().add(((TrapBlock) currentBlock).getTrap());
    }

    public static void insertTrapBlockStmts(Body sootMethodBody, BasicBlock outer, BasicBlock inner) {

        //01 语句插入到seq后一个位置
        Unit insertionPoint = outer.getInserationTarget();
        //02 插入新生成的 inner block 中的语句（抛异常之前）
        Unit innerTarget = ((TrapBlock)outer).getInitCond();
        Unit innerHead = innerTarget;
        if (inner instanceof StmtBlock) {
            //03 插入新生成的 StmtBlock 中的语句
            if (inner.getStmts().size() != 0) {
                innerHead = inner.getStmts().get(0);
            }
            sootMethodBody.getUnits().insertBefore(inner.getStmts(), insertionPoint);
        } else if (inner instanceof CondBlock) {
            //03 - 1 更新 if 语句的 target
            Stmt subHeadStmt = ((CondBlock) inner).getHeadStmt();
            innerHead = subHeadStmt;
            //03 - 2 插入 if condition
            sootMethodBody.getUnits().insertBefore(inner.getStmts(), insertionPoint);
            //03 - 3 插入 if Stmt
            sootMethodBody.getUnits().insertBeforeNoRedirect(subHeadStmt, insertionPoint);
            //03 - 4 插入 sub if target
            sootMethodBody.getUnits().insertBeforeNoRedirect(((CondBlock) inner).getGotoTarget(), insertionPoint);
        } else if (inner instanceof LoopBlock) {
            innerHead = ((LoopBlock) inner).getInitStmt();
            //03 插入 block 语句
            //03 - 1 插入 sub block 的初始化语句
            sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) inner).getInitStmt(), insertionPoint);
            //03 - 2 插入 head Stmt
            sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) inner).getHeadStmt(), insertionPoint);
            //03 - 3 插入 step 更新语句
            sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) inner).getStepStmt(), insertionPoint);
            //03 - 4 插入 goto 语句
            sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) inner).getBackJumpStmt(), insertionPoint);
            //03 - 5 插入 sub loop target
            sootMethodBody.getUnits().insertBeforeNoRedirect(((LoopBlock) inner).getGotoTarget(), insertionPoint);
        } else if (inner instanceof SwitchBlock) {
            if (inner.getStmts().size() != 0) {
                innerHead = inner.getStmts().get(0);
            }
            //03 - 1 插入可能的局部变量初始化语句
            sootMethodBody.getUnits().insertBefore(inner.getStmts(), insertionPoint);
            sootMethodBody.getUnits().insertBeforeNoRedirect(((SwitchBlock) inner).getSwitchStmt(), insertionPoint);
            Stmt subSwitchStmt = ((SwitchBlock) inner).getSwitchStmt();
            for (int i1 = 0; i1 < ((SwitchStmt) subSwitchStmt).getTargets().size(); i1++) {
                ((JLookupSwitchStmt) subSwitchStmt).setTarget(i1, insertionPoint);
            }
            ((JLookupSwitchStmt) subSwitchStmt).setDefaultTarget(insertionPoint);
        } else if (inner instanceof TrapBlock) {

            innerHead = ((TrapBlock) inner).getInitCond();
            //03 - 1 调整goto跳转到target(外部trap的抛异常前)
            GotoStmt innerGotoStmt = ((TrapBlock) inner).getGotoStmt();
            innerGotoStmt.setTarget(innerTarget);
            //03 - 2 set target of if stmt
            Stmt headStmt = ((TrapBlock) inner).getHeadCond();
            ((IfStmt) headStmt).setTarget(innerGotoStmt);
            //03 - 3 insert if condition and if stmt
            sootMethodBody.getUnits().insertBeforeNoRedirect(((TrapBlock) inner).getInitCond(), insertionPoint);
            sootMethodBody.getUnits().insertBeforeNoRedirect(headStmt, insertionPoint);
            //03 - 4 插入try语句
            for (Stmt stmt : ((TrapBlock) inner).getThrowBlock()) {
                sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, insertionPoint);
            }
            //03 - 5 插入goto语句
            sootMethodBody.getUnits().insertBeforeNoRedirect(innerGotoStmt, insertionPoint);
            //03 - 6 插入catch语句
            for (Stmt stmt : ((TrapBlock) inner).getCatchBlock()) {
                sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, insertionPoint);
            }
            //03 - 7 set trap
            Trap trap = ((TrapBlock) inner).getTrap();
            trap.setBeginUnit(innerHead);
            trap.setEndUnit(innerGotoStmt);
            //03 - 8 插入trap
            sootMethodBody.getTraps().add(((TrapBlock) inner).getTrap());
        }
        //04 调整goto跳转到target
        GotoStmt outerGotoStmt = ((TrapBlock) outer).getGotoStmt();
        outerGotoStmt.setTarget(insertionPoint);
        //05 set target of if stmt
        Stmt headStmt = ((TrapBlock) outer).getHeadCond();
        ((IfStmt) headStmt).setTarget(outerGotoStmt);
        //06 insert if condition and if stmt
        sootMethodBody.getUnits().insertBeforeNoRedirect(((TrapBlock) outer).getInitCond(), insertionPoint);
        sootMethodBody.getUnits().insertBeforeNoRedirect(headStmt, insertionPoint);
        //07 插入try语句（抛异常）
        for (Stmt stmt : ((TrapBlock) outer).getThrowBlock()) {
            sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, insertionPoint);
        }
        //08 插入goto语句
        sootMethodBody.getUnits().insertBeforeNoRedirect(outerGotoStmt, insertionPoint);
        //09 插入catch语句
        for (Stmt stmt : ((TrapBlock) outer).getCatchBlock()) {
            sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, insertionPoint);
        }
        //10 set trap
        Trap trap = ((TrapBlock) outer).getTrap();
        trap.setBeginUnit(innerHead);
        trap.setEndUnit(outerGotoStmt);
        //11 插入trap
        sootMethodBody.getTraps().add(trap);
    }

    /******************************************************************************************************
     ***************************************         utils        *****************************************
     ******************************************************************************************************/

    public static Unit getNextUnit(Body body, Unit targetUnit) {

        Unit ret = null;

        Iterator<Unit> unitIterator = body.getUnits().iterator();
        while (unitIterator.hasNext()) {
            ret = unitIterator.next();
            if (ret == targetUnit && unitIterator.hasNext()) {
                ret = unitIterator.next();
                break;
            }
        }
        return ret;
    }

    /**
     * @description: 判断需要被包裹的代码块中的定义语句是否需要提前
     * @param clazz:
     * @param sootMethod:
     * @return void
     * @author: jiachen
     * @date: 2022/8/10 12:21
     */
    public static List<Stmt> getDefinitions(ClassInfo clazz, SootMethod sootMethod, List<Stmt> seq) {

        Body sootMethodBody = sootMethod.retrieveActiveBody();
        List<Stmt> def = new ArrayList<>();
        if (seq.size() <= 0) {
            return def;
        }
        InitDataFlowAnalysis analysis = new InitDataFlowAnalysis(new CompleteUnitGraph(sootMethodBody));
        Iterator<Unit> units = sootMethodBody.getUnits().iterator(seq.get(0), seq.get(seq.size() - 1));
        while (units.hasNext()) {
            Unit stmt = units.next();
            // 变量赋值，初始化
            if (! stmt.getDefBoxes().isEmpty()) {
                for (ValueBox box: stmt.getDefBoxes()) {
                    Value value = box.getValue();
                    if (value instanceof Local) { // 判断是否是局部变量（包括 基础变量 和 对象new）
                        Local local = (Local) value;
                        if(value instanceof ArrayRef){ // 判断是否是 数组 的定义
                            Value base = ((ArrayRef) value).getBase();
                            local = (Local) base;
                        }
                        if(! analysis.getInLocal(stmt).toList().contains(local)) {
                            def.add((Stmt) stmt);
                        }
                    }
                }
            } else if (!stmt.getUseBoxes().isEmpty()){ // 寻找对象的 构造语句 （new 与 构造 需要同时提前）
                for (ValueBox box: stmt.getUseBoxes()) {
                    if (box.getValue() instanceof SpecialInvokeExpr) {
                        SpecialInvokeExpr expr = (SpecialInvokeExpr) box.getValue();
                        if (expr.getMethodRef().resolve().isConstructor()) {
                            def.add((Stmt) stmt);
                        }
                    }
                }
                // 变量使用，不需要提前
            }
        }

        Set<List<Stmt>> finalSet = getUnitChain(def);
        List<Stmt> removeDef = removedef(clazz, sootMethod, seq, def, finalSet);

        def.clear();
        def.addAll(removeDef);
        return def;
    }

    public static Set<List<Stmt>> getUnitChain(List<Stmt> def) {
        Set<List<Stmt>> defChain = new HashSet<>();
        Set<List<Stmt>> finalSet = new HashSet<>();
        Set<Stmt> meetUnits = new HashSet<>();
        // 遍历所有提取出的def语句
        for (Stmt stmt : def) {
            boolean hasChain = false;
            // 遍历当前已经提取出来的chain，如果有符合的链加在后面
            for (List<Stmt> unitList : defChain) {
                unitListLabel:
                for (int i = 0; i < unitList.size(); i++) {
                    for (ValueBox box : unitList.get(i).getDefBoxes()) { // 全部已经声明的def
                        // 对于每一条语句的usebox，由于存在多条usebox，会被添加到不同的chain中，我们需要整合这些chain
                        if(unitList.get(i).equals(stmt)) {
                            break unitListLabel; // var_5 = var_5 + 0;
                        }
                        for (ValueBox useBox : stmt.getUseBoxes()) {
                            if (box.getValue().equals(useBox.getValue())) {
                                if(hasChain) {
                                    // 说明有至少有两条链包含了该条语句
                                    meetUnits.add(stmt);
                                }
                                unitList.add(stmt);
                                hasChain = true;
                                continue unitListLabel;
                            }
                        }
                    }
                }
            }

            if(!hasChain) {
                List<Stmt> unitList = new ArrayList<>();
                unitList.add(stmt);
                defChain.add(unitList);
            }
        }

        if(meetUnits.isEmpty()) {
            finalSet.addAll(defChain);
        }

        for (Unit meetUnit : meetUnits) {
            Set<List<Stmt>> set = new HashSet<>();
            for (List<Stmt> unitList : defChain) {
                if(unitList.contains(meetUnit)) {
                    set.add(unitList); // 待归并的几个list，按照def中的顺序进行归并
                } else {
                    finalSet.add(unitList);
                }
            }

            List<Stmt> list = new ArrayList<>(); // 归并后的list
            boolean removal = true;
            // 按照def的顺序归并list
            for (Stmt stmt : def) {
                for (List<Stmt> unitList : set) {
                    for (Unit unit : unitList) {
                        // 到unit时，如果list中已经包含了unit，则不添加，都为false
                        // 如果不是unit时!meetUnits.contains(stmt)为true
                        if (stmt.equals(unit) && ( !meetUnits.contains(stmt) || removal )) {
                            list.add(stmt);
                            if(meetUnits.contains(stmt)) {
                                removal = false;
                            }
                        }
                    }
                }
            }
            finalSet.add(list);
        }

        return finalSet;
    }


    public static List<Stmt> removedef(ClassInfo clazz, SootMethod sootMethod, List<Stmt> seq, List<Stmt> def, Set<List<Stmt>> finalSet) {
        List<Stmt> removeDef = new ArrayList<>();

        Body sootMethodBody = sootMethod.retrieveActiveBody();
        CompleteUnitGraph graph = new CompleteUnitGraph(sootMethodBody);
        DominatorsFinder<Unit> df = new MHGDominatorsFinder<>(graph);

        // 遍历从seq后一句到最后
        Unit next = getNextUnit(sootMethodBody, seq.get(seq.size() - 1));
        Iterator<Unit> units = sootMethodBody.getUnits().iterator(next, sootMethodBody.getUnits().getLast());
        while (units.hasNext()) {
            Unit unit = units.next();
            List<Unit> dom = df.getDominators(unit);
            // 该语句是否使用def, 以及该语句是否被def支配

            // unit 后面的语句
            for (ValueBox useBox : unit.getUseBoxes()) {
                for (Unit stmt : dom) { // 支配该语句的所有语句
                    for (ValueBox defBox : stmt.getDefBoxes()) {
                        if (defBox.getValue().equals(useBox.getValue())) {
                            // 将该条语句的使用情况设置为被调用
                            if(def.contains(stmt)) {
                                for (List<Stmt> unitList : finalSet) {
                                    if(unitList.contains(stmt)) {
                                        removeDef.addAll(unitList.subList(0, unitList.indexOf(stmt) + 1));
                                    }
                                }
                            }
                        } else if (defBox.getValue() instanceof ArrayRef) {
                            Value base = ((ArrayRef) defBox.getValue()).getBase();
                            if (base.equals(useBox.getValue())) {
                                if(def.contains(stmt)) {
                                    for (List<Stmt> unitList : finalSet) {
                                        if(unitList.contains(stmt)) {
                                            removeDef.addAll(unitList.subList(0, unitList.indexOf(stmt) + 1));
                                        }
                                    }
                                }
                            }
                            if (useBox.getValue() instanceof ArrayRef) {
                                if (((ArrayRef) useBox.getValue()).getBase().equals(base)) {
                                    if(def.contains(stmt)) {
                                        for (List<Stmt> unitList : finalSet) {
                                            if(unitList.contains(stmt)) {
                                                removeDef.addAll(unitList.subList(0, unitList.indexOf(stmt) + 1));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (useBox.getValue().getType() instanceof RefType) {
                    for (Unit stmt : dom) {
                        for (ValueBox box : stmt.getUseBoxes()) { // def中所有的定义
                            if (box.getValue() instanceof SpecialInvokeExpr) {
                                SpecialInvokeExpr expr = (SpecialInvokeExpr) box.getValue();
                                if (expr.getMethodRef().resolve().isConstructor()) {
                                    if (useBox.getValue().equals(expr.getBase())) {
                                        if(def.contains(stmt)) {
                                            for (List<Stmt> unitList : finalSet) {
                                                if(unitList.contains(stmt)) {
                                                    removeDef.addAll(unitList.subList(0, unitList.indexOf(stmt) + 1));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return removeDef;
    }

}
