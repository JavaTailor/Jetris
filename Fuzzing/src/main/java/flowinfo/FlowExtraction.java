package flowinfo;

import codegen.providers.OperatorProvider;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.toolkits.graph.*;
import soot.util.Chain;
import utils.CFGGenerator;
import utils.ClassUtils;

import java.util.*;

public class FlowExtraction {
    public static Body currentMethodBody;
    public static UnitGraph currentUnitGraph;
    public static MHGDominatorsFinder currentUnitDominator;
    public static ArrayList<Block> condBlocks;
    public static ArrayList<Block> loopBlocks;
    public static ArrayList<Block> switchBlocks;
    public static void initialize(Body methodBody) {

        currentMethodBody = methodBody;
        currentUnitGraph = new ExceptionalUnitGraph(currentMethodBody);
        currentUnitDominator = new MHGDominatorsFinder(currentUnitGraph);
        condBlocks = new ArrayList<>();
        loopBlocks = new ArrayList<>();
        switchBlocks = new ArrayList<>();
    }

    public static void main(String[] args) {

        String className = "templates.demo10";
        ClassUtils.initSootEnv();
        SootClass seedClass = ClassUtils.loadClass(className);
        List<SootMethod> sootMethods = seedClass.getMethods();

        for (SootMethod sootMethod : sootMethods) {
            if (sootMethod.getName().contains("main")) {
                System.out.println(sootMethod.getName());
                CFGGenerator.printCFGPng(sootMethod.retrieveActiveBody());
                initialize(sootMethod.retrieveActiveBody());
                System.out.println(abstractCurrentMethod());
            }
        }
    }

    public static NodeSequence abstractCurrentMethod() {

        BlockGraph blockGraph = new ZonedBlockGraph(currentMethodBody);
        Iterator<Block> iter = blockGraph.iterator();
        ArrayList<Block> blocks = new ArrayList<>();
        while (iter.hasNext()) {
            Block block = iter.next();
            blocks.add(block);
        }
        if (blocks.size() > 200) {
            return new NodeSequence();
        }
        Deque<AbstractNode> allNodes = abstractCFGNodes(blocks.subList(0, blocks.size()), new ArrayList<>());
        return new NodeSequence(allNodes);
    }


    public static void printAbstractNode(AbstractNode node) {
        System.out.println(formatAbstractNode(node, 0));
    }

    public static String formatAbstractNode(AbstractNode node, int depth) {
        StringBuilder formatString = new StringBuilder();
        String space = String.join("", Collections.nCopies(depth, "\t"));
        formatString.append(space).append(node).append("\n");
        for (AbstractNode succor : node.succors) {
            formatString.append(formatAbstractNode(succor, depth + 1));
        }
        return formatString.toString();
    }

    public static Deque<AbstractNode> abstractCFGNodes(List<Block> blocks,
                                                       ArrayList<Block> visitedBlocks) {

        Deque<AbstractNode> curNodes = new ArrayDeque<>();
        for (int i = blocks.size() - 1; i >= 0 ; i--) {

            Block block = blocks.get(i);
            //01 abstract loop nodes
            if (!visitedBlocks.contains(block)) {
                i = abstractLoopNode(blocks, i, curNodes, visitedBlocks);
            }
            // abstract switch nodes
            if (!visitedBlocks.contains(block)) {
                i = abstractSwitchNode(blocks, i, curNodes, visitedBlocks);
            }
            //02 abstract cond nodes
            if (!visitedBlocks.contains(block) || condBlocks.contains(block) || loopBlocks.contains(block) ) {
                i = abstractCondNode(blocks, i, curNodes, visitedBlocks);
            }
            //03 abstract seq nodes
            if (!visitedBlocks.contains(block)) {
                i = abstractSeqNode(blocks, i, curNodes, visitedBlocks);
            }
            //04 abstract trap nodes
            if (!visitedBlocks.contains(block)) {
                i = abstractTrapNode(blocks, i, curNodes, visitedBlocks);
            }
        }
        return curNodes;
    }

    public static int abstractLoopNode(List<Block> blocks,
                                        int currentIndex,
                                        Deque<AbstractNode> curNodes,
                                        ArrayList<Block> visitedBlocks) {

        Block curBlock = blocks.get(currentIndex);
        Iterator<Unit> blockUnits = currentMethodBody.getUnits().iterator(curBlock.getHead(), curBlock.getTail());
        while (blockUnits.hasNext()) {

            Unit unit = blockUnits.next();
            List<Unit> succs = currentUnitGraph.getSuccsOf(unit);
            List<Unit> dominaters = currentUnitDominator.getDominators(unit);
            List<Stmt> headers = new ArrayList<>();
            for (Unit succ : succs) {
                if (dominaters.contains(succ)) {
                    headers.add((Stmt) succ);
                }
            }
            for (Unit header : headers) {

                //create seq node
                AbstractNode seqNode = null;
                boolean doWhileFlag = false;
                if (!visitedBlocks.contains(curBlock)){

                    // do-while
                    if (curBlock.getTail() instanceof JIfStmt && ((JIfStmt) curBlock.getTail()).getTarget() == header) {

                        AbstractNode loopNode = new AbstractNode(curBlock.getIndexInMethod(), FlowType.LOOP, new ArrayList<>(), abstractBlockProperty(curBlock));
                        visitedBlocks.add(curBlock);
                        loopBlocks.add(curBlock);
                        curNodes.push(loopNode);
                        doWhileFlag = true;
                    } else {
                        seqNode = new AbstractNode(curBlock.getIndexInMethod(), FlowType.SEQ, new ArrayList<>(), abstractBlockProperty(curBlock));
                        visitedBlocks.add(curBlock);
                        loopBlocks.add(curBlock);
                        curNodes.push(seqNode);
                    }
                } else {
                    for (AbstractNode curNode : curNodes) {
                        if (curNode.getNid() == curBlock.getIndexInMethod()) {
                            seqNode  = curNode;
                            break;
                        }
                    }
                }
                //create loop node
                int headerIndex = getBlockIdByHeader(blocks, (Stmt) header);
                if (doWhileFlag) {

                    //do nothing
//                    Block headerBlock = blocks.get(headerIndex);
//                    if (! (headerBlock.getTail() instanceof JIfStmt) ) {
//                        seqNode = new AbstractNode(headerBlock.getIndexInMethod(), FlowType.SEQ, new ArrayList<>(), abstractBlockProperty(headerBlock));
//                        visitedBlocks.add(headerBlock);
//                        loopBlocks.add(headerBlock);
//                        curNodes.push(seqNode);
//                    }
                } else {

                    AbstractNode loopNode = new AbstractNode(blocks.get(headerIndex).getIndexInMethod(), FlowType.LOOP, new ArrayList<>(), abstractBlockProperty(blocks.get(headerIndex)));
                    visitedBlocks.add(blocks.get(headerIndex));
                    curNodes.push(loopNode);
                    Block headerBlock = blocks.get(headerIndex);
                    if (curBlock.getPreds().contains(headerBlock) && headerBlock.getPreds().contains(curBlock)) {
                        if (seqNode != null) {
                            curNodes.remove(seqNode);
                            loopNode.succors.add(seqNode);
                        }
                    }
                    //loop body (succor nodes)
                    if (headerIndex > currentIndex) {
                        //do nothing
                        return currentIndex;
                    }
                    Deque<AbstractNode> succorNodes = abstractCFGNodes(blocks.subList(headerIndex + 1, currentIndex + 1), visitedBlocks);
                    while (succorNodes.size() > 0) {
                        AbstractNode node = succorNodes.pop();
                        int insertIndex = 999999999;
                        for (AbstractNode succor : loopNode.succors) {
                            if (succor.getNid() > node.getNid()
                                    && loopNode.succors.indexOf(succor) < insertIndex) {
                                insertIndex = loopNode.succors.indexOf(succor);
                            }
                        }
                        if (insertIndex != 999999999) {
                            loopNode.succors.add(insertIndex, node);
                        } else {
                            loopNode.succors.add(node);
                        }
                    }
                    currentIndex = headerIndex;
                }
            }
        }
        return currentIndex;
    }

    public static int abstractCondNode(List<Block> blocks,
                                      int currentIndex,
                                      Deque<AbstractNode> curNodes,
                                      ArrayList<Block> visitedBlocks) {

        Block curBlock = blocks.get(currentIndex);
        List<Block> preBlocks = curBlock.getPreds();

        Block ifHeaderBlock = null;

        if (containsTrapPreds(curBlock)) {
            return currentIndex;
        }
        if (preBlocks.size() >= 2) {

            List<Unit> dominators = currentUnitDominator.getDominators(curBlock.getHead());
            dominators.remove(curBlock.getHead());
            Stmt tail = (Stmt) dominators.get(dominators.size() - 1);
            if (tail instanceof IfStmt) {
                int tailIndex = getBlockIdByTail(blocks, tail);
                if (tailIndex >= 0
                        && allPathTo(blocks.get(tailIndex), curBlock, new HashSet<>(), new ArrayList<>())) {
                    ifHeaderBlock = blocks.get(tailIndex); //identify if block
                }
            }

            if (ifHeaderBlock == null && (condBlocks.contains(curBlock) || loopBlocks.contains(curBlock)) || switchBlocks.contains(curBlock)) {
                ifHeaderBlock = getSubDominatorNode(blocks, curBlock);
            }
            if (ifHeaderBlock != null) {

                //create seq node
                if (!visitedBlocks.contains(curBlock)) {
                    AbstractNode seqNode = new AbstractNode(curBlock.getIndexInMethod(), FlowType.SEQ, new ArrayList<>(), abstractBlockProperty(curBlock));
                    visitedBlocks.add(curBlock);
                    condBlocks.add(curBlock);
                    curNodes.push(seqNode);
                }

                int headerIndex = blocks.indexOf(ifHeaderBlock);
                if (headerIndex == currentIndex) return currentIndex;
                //create cond node
                AbstractNode condNode = new AbstractNode(blocks.get(headerIndex).getIndexInMethod(), FlowType.COND, new ArrayList<>(), abstractBlockProperty(blocks.get(headerIndex)));
                visitedBlocks.add(blocks.get(headerIndex));
                condBlocks.add(blocks.get(headerIndex));
                curNodes.push(condNode);
                //cond body (succor nodes)
                Deque<AbstractNode> succorNodes = abstractCFGNodes(blocks.subList(headerIndex + 1, currentIndex + 1), visitedBlocks);
                while (succorNodes.size() > 0) {
                    AbstractNode node = succorNodes.pop();
                    condNode.succors.add(node);
                }
                currentIndex = headerIndex + 1;
            }
        }
        //for break
        if (containsIfStmt(curBlock)
                && !visitedBlocks.contains(curBlock)
                && !condBlocks.contains(curBlock)) {

            AbstractNode condNode = new AbstractNode(curBlock.getIndexInMethod(), FlowType.COND, new ArrayList<>(), abstractBlockProperty(curBlock));
            visitedBlocks.add(curBlock);
            condBlocks.add(curBlock);
            curNodes.push(condNode);
            int target_id = condNode.getProperty().getTarget_id();
            for (Block block : blocks) {
                if (block.getIndexInMethod() > curBlock.getIndexInMethod() && block.getIndexInMethod() < target_id) {

                    ArrayList<AbstractNode> removedNode = new ArrayList<>();
                    if (hasPathTo(curBlock, block, new HashSet<>())) {
                        for (AbstractNode curNode : curNodes) {
                            if (curNode.getNid() == block.getIndexInMethod()) {
                                removedNode.add(curNode);
                                condNode.succors.add(curNode);
                            }
                        }
                    }
                    curNodes.removeAll(removedNode);
                }
            }
        }
        return currentIndex;
    }

    public static int abstractSwitchNode(List<Block> blocks,
                                       int currentIndex,
                                       Deque<AbstractNode> curNodes,
                                       ArrayList<Block> visitedBlocks) {

        Block curBlock = blocks.get(currentIndex);
        List<Block> preBlocks = curBlock.getPreds();

        Block switchHeaderBlock = null;

        if (containsTrapPreds(curBlock)) {
            return currentIndex;
        }
        if (preBlocks.size() >= 2) {

            List<Unit> dominators = currentUnitDominator.getDominators(curBlock.getHead());
            dominators.remove(curBlock.getHead());
            Stmt tail = (Stmt) dominators.get(dominators.size() - 1);
            if (tail instanceof JTableSwitchStmt || tail instanceof JLookupSwitchStmt) {
                int tailIndex = getBlockIdByTail(blocks, tail);
                if (tailIndex >= 0
                        && allPathTo(blocks.get(tailIndex), curBlock, new HashSet<>(), new ArrayList<>())) {
                    switchHeaderBlock = blocks.get(tailIndex); //identify if block
                }
            }
            if (switchHeaderBlock != null) {

                //create seq node
                AbstractNode seqNode = new AbstractNode(curBlock.getIndexInMethod(), FlowType.SEQ, new ArrayList<>(), abstractBlockProperty(curBlock));
                visitedBlocks.add(curBlock);
                switchBlocks.add(curBlock);
                curNodes.push(seqNode);

                int headerIndex = blocks.indexOf(switchHeaderBlock);
                //create cond node
                AbstractNode switchNode = new AbstractNode(blocks.get(headerIndex).getIndexInMethod(), FlowType.SWITCH, new ArrayList<>(), abstractBlockProperty(blocks.get(headerIndex)));
                visitedBlocks.add(blocks.get(headerIndex));
                curNodes.push(switchNode);
                //switch body (succor nodes)
                Deque<AbstractNode> succorNodes = abstractCFGNodes(blocks.subList(headerIndex + 1, currentIndex+1), visitedBlocks);
                while (succorNodes.size() > 0) {
                    AbstractNode node = succorNodes.pop();
                    switchNode.succors.add(node);
                }
                currentIndex = headerIndex + 1;
            }
        }
        return currentIndex;
    }

    public static Block getSubDominatorNode(List<Block> allBlocks,Block curBlock) {

        Block dominatorBlock = curBlock;
        Queue<Block> blockQueue = new LinkedList<>();
        blockQueue.offer(curBlock);
        ArrayList<Block> visited = new ArrayList<>();
        while (!blockQueue.isEmpty()) {

            Block block = blockQueue.poll();
            visited.add(block);
            for (Block pred : block.getPreds()) {
                if (allBlocks.contains(pred)) {
                    if (!visited.contains(pred)) {
                        blockQueue.offer(pred);
                    }
                    if (containsIfStmt(pred)) {

                        ArrayList<Block> loopHeader = new ArrayList<>();
                        if (allPathTo(pred, curBlock, new HashSet<>(), loopHeader)
                                && !loopHeader.contains(pred)
                                && pred.getIndexInMethod() < dominatorBlock.getIndexInMethod()) {
                            dominatorBlock = pred;
                        }
                    }
                }
            }
        }
//        if (!allPathTo(dominatorBlock, curBlock, new HashSet<>())) {
//            dominatorBlock = null;
//        }
        return dominatorBlock;
    }

    private static boolean hasPathTo(Block curBlock, Block block, Set<Block> visited) {

        ArrayList<Boolean> status = new ArrayList<>();
        if (curBlock == block) {
            return true;
        }
        visited.add(curBlock);
        for (Block succ : curBlock.getSuccs()) {
            if (!visited.contains(succ)) {
                status.add(hasPathTo(succ, block, visited));
            }
        }
        if (status.size() == 0) {
            return false;
        } else {
            return status.stream().reduce(false, (a, b) -> a || b);
        }
    }

    public static boolean allPathTo(Block start, Block target, Set<Block> visited, ArrayList<Block> loopHeader) {

        ArrayList<Block> ignored = new ArrayList<>();
        if (containsIfStmt(start)) {
            for (Block succ : start.getSuccs()) {
                if (succ.getSuccs().contains(start)) {
                    ignored.add(succ);
                    loopHeader.add(start);
                }
            }
        }
        ArrayList<Boolean> status = new ArrayList<>();
        if (start == target) {
            return true;
        }
        visited.add(start);
        for (Block succ : start.getSuccs()) {
            if (!visited.contains(succ) && !ignored.contains(succ)) {
                status.add(allPathTo(succ, target, visited, loopHeader));
            }
        }
        if (status.size() == 0) {
            return false;
        } else {
            return status.stream().reduce(true, (a, b) -> a && b);
        }
    }

    public static int abstractSeqNode(List<Block> blocks,
                                      int currentIndex,
                                      Deque<AbstractNode> curNodes,
                                      ArrayList<Block> visitedBlocks) {
        Block block = blocks.get(currentIndex);
        if (block.getHead() instanceof JIdentityStmt
                && ((JIdentityStmt) block.getHead()).getRightOpBox().getValue() instanceof JCaughtExceptionRef) {
            return currentIndex;
        }
        if (!visitedBlocks.contains(block) && !containsIfStmt(block)) {
            AbstractNode seqNode = new AbstractNode(block.getIndexInMethod(), FlowType.SEQ, new ArrayList<>(), abstractBlockProperty(block));
            curNodes.push(seqNode);
            visitedBlocks.add(block);
        }
        return currentIndex;
    }

    public static int abstractTrapNode(List<Block> blocks,
                                       int currentIndex,
                                       Deque<AbstractNode> curNodes,
                                       ArrayList<Block> visitedBlocks) {

        Unit beginUnit = null;
        Unit endUnit = null;
        int instSize = 0;
        int targetId = -1;
        int beginId = -1;
        int endId = -1;
        Block block = blocks.get(currentIndex);
        if (block.getHead() instanceof JIdentityStmt
                && ((JIdentityStmt) block.getHead()).getRightOpBox().getValue() instanceof JCaughtExceptionRef) {

            Iterator<Unit> blockUnits = currentMethodBody.getUnits().iterator(block.getHead(), block.getTail());
            while (blockUnits.hasNext()) {
                blockUnits.next();
                instSize++;
            }
            Chain<Trap> traps = currentMethodBody.getTraps();
            for (Trap trap : traps) {
                if (trap.getHandlerUnit() == block.getHead()) {
                    beginUnit = trap.getBeginUnit();
                    endUnit = trap.getEndUnit();
                    break;
                }
            }

            if (beginUnit != null && endUnit != null) {

                //01 target
                List<Block> succors = block.getSuccs();
//                assert succors.size() == 1;
                if (succors.size() == 0) {
                    visitedBlocks.add(block);
                    return currentIndex;
                }
                Block target = succors.get(0);
                targetId = target.getIndexInMethod();

                //02 begin & end
                Deque<Block> stack = new ArrayDeque<Block>();
                ArrayList<Block> visited = new ArrayList<>();
                stack.push(target);
                while (!stack.isEmpty()) {

                    Block preBlock = stack.pop();
                    visited.add(preBlock);
                    if (beginId == -1 && preBlock.getHead() == beginUnit) {
                        beginId = preBlock.getIndexInMethod();
                    }
                    if (endId == -1 && preBlock.getHead() == endUnit) {
                        endId = preBlock.getIndexInMethod();
                    }
                    for (Block pred : preBlock.getPreds()) {
                        if (!visited.contains(pred)) {
                            stack.push(pred);
                        }
                    }
                    if (beginId != -1 && endId != -1) {
                        break;
                    }
                }

                //03 new node
                NodeProperty nodeProperty = new NodeProperty(instSize, targetId);
                nodeProperty.setBegin_id(beginId);
                nodeProperty.setEnd_id(endId);
                AbstractNode trapNode = new AbstractNode(block.getIndexInMethod(), FlowType.TRAP, new ArrayList<>(), nodeProperty);
                curNodes.push(trapNode);
                visitedBlocks.add(block);
            }
        }

        return currentIndex;
    }

    public static NodeProperty abstractBlockProperty(Block curBlock) {

        int instSize = 0;
        int targetId = -1;
        int caseSize = 0;
        HashSet<Type> defSet = new HashSet();
        HashMap<Type, Set<Expr>> useSet = new HashMap<>();

        Iterator<Unit> blockUnits = currentMethodBody.getUnits().iterator(curBlock.getHead(), curBlock.getTail());
        while (blockUnits.hasNext()) {
            Unit unit = blockUnits.next();
            instSize++;
            List<Block> targets;
            if (unit instanceof JGotoStmt) {
                targets = curBlock.getSuccs();
                assert targets.size() == 1;
                targetId = targets.get(0).getIndexInMethod();
            } else if (unit instanceof JIfStmt) {
                Stmt targetStmt = ((JIfStmt) unit).getTarget();
                targets = curBlock.getSuccs();
                for (Block target : targets) {

                    Iterator<Unit> units = currentMethodBody.getUnits().iterator(target.getHead(), target.getTail());
                    ArrayList<Unit> subUnits = new ArrayList<>();
                    while (units.hasNext()) {
                        subUnits.add(units.next());
                    }
                    if (subUnits.contains(targetStmt)) {
                        targetId = target.getIndexInMethod();
                        break;
                    }
                }
            } else if (unit instanceof SwitchStmt) {
                caseSize = ((SwitchStmt) unit).getTargets().size() + 1;
            } else {
                parseDefUseBox(unit, defSet, useSet);
            }
        }
        NodeProperty nodeProperty = new NodeProperty(instSize, targetId, caseSize);
        nodeProperty.setDefSet(defSet);
        nodeProperty.setUseSet(useSet);
        return nodeProperty;
    }

    public static void parseDefUseBox(Unit unit, HashSet<Type> defSet, HashMap<Type, Set<Expr>> useSet) {

        for (ValueBox defBox : unit.getDefBoxes()) {
            if (defBox.getValue() instanceof JimpleLocal) {
                defSet.add(defBox.getValue().getType());
            }
        }
        for (ValueBox useBox : unit.getUseBoxes()) {
            if (useBox.getValue() instanceof JimpleLocal) {
                if (!useSet.keySet().contains(useBox.getValue().getType())) {
                    useSet.put(useBox.getValue().getType(), new HashSet<>());
                }
            }
        }
        if (unit instanceof JAssignStmt) {

            ValueBox rightBox = ((JAssignStmt) unit).getRightOpBox();
            if (rightBox.getValue() instanceof Expr) {

                if (rightBox.getValue() instanceof InvokeExpr) {

                    InvokeExpr invokeValue = (InvokeExpr) rightBox.getValue();

                    if (invokeValue instanceof JSpecialInvokeExpr) {

                        Value base = ((JSpecialInvokeExpr) invokeValue).getBase();
                        String invokeName = invokeValue.getMethod().getName();
                        if (base instanceof Local) {
                            if (useSet.keySet().contains(base.getType())) {
                                addIfAbsent(useSet.get(base.getType()), invokeName);
                            }
                        }
                    } else if (invokeValue instanceof JVirtualInvokeExpr) {

                        Value base = ((JVirtualInvokeExpr) invokeValue).getBase();
                        String invokeName = invokeValue.getMethod().getName();
                        if (base instanceof Local) {
                            if (useSet.keySet().contains(base.getType())) {
                                addIfAbsent(useSet.get(base.getType()), invokeName);
                            }
                        }
                    } else {
                        //do nothing
                    }
                } else if (rightBox.getValue() instanceof NewExpr) {
                    //do nothing
                } else {
                    Type nType = rightBox.getValue().getType();
                    if (useSet.keySet().contains(nType)) {

                        for (Expr expr : OperatorProvider.operatorList) {

                            if (rightBox.getValue().getClass() == expr.getClass()) {
                                useSet.get(nType).add(expr);
                                break;
                            }
                        }
                    }
                }
            }
        } else if (unit instanceof JInvokeStmt) {

            Value invokeValue = ((JInvokeStmt) unit).getInvokeExprBox().getValue();
            if (invokeValue instanceof JSpecialInvokeExpr) {
                Value base = ((JSpecialInvokeExpr) invokeValue).getBase();
                String invokeName = ((JSpecialInvokeExpr) invokeValue).getMethod().getName();
                if (base instanceof Local) {
                    if (useSet.keySet().contains(base.getType())) {
                        addIfAbsent(useSet.get(base.getType()), invokeName);
                    }
                }
            } else if (invokeValue instanceof JVirtualInvokeExpr) {
                Value base = ((JVirtualInvokeExpr) invokeValue).getBase();
                String invokeName = ((JVirtualInvokeExpr) invokeValue).getMethod().getName();
                if (base instanceof Local) {
                    if (useSet.keySet().contains(base.getType())) {
                        addIfAbsent(useSet.get(base.getType()), invokeName);
                    }
                }
            } else {
                //do nothing
            }
        }
    }

    public static void addIfAbsent(Set<Expr> absMethods, String methodName) {

        for (Expr absMethod : absMethods) {
            if (absMethod instanceof AbstractMethodCall) {
                if (((AbstractMethodCall) absMethod).getMethodName().equals(methodName)) {
                    ((AbstractMethodCall) absMethod).callTimeIncrease();
                    return;
                }
            }
        }
        absMethods.add(new AbstractMethodCall(methodName));
    }

    public static boolean containsIfStmt(Block block) {

        Iterator<Unit> blockUnits = block.getBody().getUnits().iterator(block.getHead(), block.getTail());
        while (blockUnits.hasNext()) {

            Unit unit = blockUnits.next();
            if (unit instanceof IfStmt) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsTrapPreds(Block block) {

        List<Block> preds = block.getPreds();
        for (Block pred : preds) {

            if (pred.getHead() instanceof JIdentityStmt
                    && ((JIdentityStmt) pred.getHead()).getRightOpBox().getValue() instanceof JCaughtExceptionRef) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsBreakStmt(Block block) {

        Iterator<Unit> blockUnits = block.getBody().getUnits().iterator(block.getHead(), block.getTail());
        while (blockUnits.hasNext()) {

            Unit unit = blockUnits.next();
            if (unit instanceof JGotoStmt) {
                return true;
            }
        }
        return false;
    }
    public static int getBlockIdByHeader(List<Block> blocks, Stmt header) {

        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            if (block.getHead() == header) {
                return i;
            }
        }
        return 0;
    }

    public static int getBlockIdByTail(List<Block> blocks, Stmt tail) {

        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            if (block.getTail() == tail) {
                return i;
            }
        }
        return -1;
    }
}
