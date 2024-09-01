package flowinfo;

import java.util.List;

public class AbstractNode {
    public int nid;
    public FlowType type;
    public List<AbstractNode> succors;
    public NodeProperty property;

    public AbstractNode() {
    }

    public AbstractNode(int nid, FlowType type, List<AbstractNode> succors, NodeProperty property) {
        this.nid = nid;
        this.type = type;
        this.succors = succors;
        this.property = property;
    }

    public AbstractNode(int nid, FlowType type, List<AbstractNode> succors) {
        this.nid = nid;
        this.type = type;
        this.succors = succors;
    }

    public int getNid() {
        return nid;
    }

    public void setNid(int nid) {
        this.nid = nid;
    }

    public FlowType getType() {
        return type;
    }

    public void setType(FlowType type) {
        this.type = type;
    }

    public List<AbstractNode> getSuccors() {
        return succors;
    }

    public void setSuccors(List<AbstractNode> succors) {
        this.succors = succors;
    }

    public NodeProperty getProperty() {
        return property;
    }

    public void setProperty(NodeProperty property) {
        this.property = property;
    }

    @Override
    public String toString() {
        return "{ ID: " + nid + " Type: " + type + " succors: " + succors.size() + " property: " + property + " }";
    }
}