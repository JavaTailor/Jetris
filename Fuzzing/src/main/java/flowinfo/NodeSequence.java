package flowinfo;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

public class NodeSequence {

    protected Deque<AbstractNode> nodes;
    protected int nodeLength = -1;
    protected int nodeDepth = -1;

    public NodeSequence() {
        nodes = new LinkedList<>();
    }

    public NodeSequence(Deque<AbstractNode> nodes) {
        this.nodes = nodes;
    }

    public NodeSequence(Deque<AbstractNode> nodes, int nodeLength) {
        this.nodes = nodes;
        this.nodeLength = nodeLength;
    }

    public Deque<AbstractNode> getNodes() {
        return nodes;
    }

    public void setNodes(Deque<AbstractNode> nodes) {
        this.nodes = nodes;
    }

    public int getNodeLength() {

        if (nodeLength == -1) {
            if (nodes != null && nodes.size() > 0) {

                nodeLength = 0;
                nodeLength += nodes.size();
                Deque<AbstractNode> queue = new ArrayDeque<>();
                for (AbstractNode node : nodes) {
                    if (node.succors.size() > 0) {
                        queue.addAll(node.succors);
                        nodeLength += node.succors.size();
                    }
                }
                while (!queue.isEmpty()) {
                    AbstractNode currentNode = queue.poll();
                    if (currentNode.succors.size() > 0) {
                        queue.addAll(currentNode.succors);
                        nodeLength += currentNode.succors.size();
                    }
                }
            }
        }
        return nodeLength;
    }

    public void setNodeLength(int nodeLength) {
        this.nodeLength = nodeLength;
    }

    public int getNodeDepth() {

        if (nodeDepth == -1) {
            if (nodes != null && nodes.size() > 0) {
                for (AbstractNode node : nodes) {
                    int depth = recurseNodeDepth(node, 1);
                    if (depth > nodeDepth) {
                        nodeDepth = depth;
                    }
                }
            }
        }
        return nodeDepth;
    }

    public int recurseNodeDepth(AbstractNode node, int depth) {
        int maxDepth = depth;
        for (AbstractNode succor : node.succors) {
            int subDepth = recurseNodeDepth(succor, depth + 1);
            if (subDepth > maxDepth) {
                maxDepth = subDepth;
            }
        }
        return maxDepth;
    }

    public void setNodeDepth(int nodeDepth) {
        this.nodeDepth = nodeDepth;
    }

    @Override
    public String toString() {

        String ret = "";
        for (AbstractNode node : nodes) {
            ret += FlowExtraction.formatAbstractNode(node, 0);
        }
        return ret;
    }
}
