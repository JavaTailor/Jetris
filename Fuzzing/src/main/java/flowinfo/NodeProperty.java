package flowinfo;

import soot.Type;
import soot.Value;
import soot.jimple.Expr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NodeProperty {
    public int inst_size;
    public int target_id;
    public int begin_id; //designed for trap
    public int end_id; //designed for trap
    public int case_size; //designed for switch
    public HashSet<Type> defSet;
    public HashMap<Type, Set<Expr>> useSet; //type operand, {<StringBuilder, {invoke }>, <int, {+}>}
    public NodeProperty() {
        inst_size = 0;
        target_id = 0;
        case_size = 0;
    }

    public NodeProperty(int inst_size, int target_id) {
        this.inst_size = inst_size;
        this.target_id = target_id;
        this.case_size = 0;
    }

    public NodeProperty(int inst_size, int target_id, int case_size) {
        this.inst_size = inst_size;
        this.target_id = target_id;
        this.case_size = case_size;
    }

    public int getInst_size() {
        return inst_size;
    }

    public void setInst_size(int inst_size) {
        this.inst_size = inst_size;
    }

    public int getTarget_id() {
        return target_id;
    }

    public void setTarget_id(int target_id) {
        this.target_id = target_id;
    }

    public int getCase_size() {
        return case_size;
    }

    public void setCase_size(int case_size) {
        this.case_size = case_size;
    }

    public int getBegin_id() {
        return begin_id;
    }

    public void setBegin_id(int begin_id) {
        this.begin_id = begin_id;
    }

    public int getEnd_id() {
        return end_id;
    }

    public void setEnd_id(int end_id) {
        this.end_id = end_id;
    }

    public HashSet<Type> getDefSet() {
        return defSet;
    }

    public void setDefSet(HashSet<Type> defSet) {
        this.defSet = defSet;
    }

    public HashMap<Type, Set<Expr>> getUseSet() {
        return useSet;
    }

    public void setUseSet(HashMap<Type, Set<Expr>> useSet) {
        this.useSet = useSet;
    }

    @Override
    public String toString() {
        return "Inst: " + inst_size
                + " TargetId: " + target_id
                + " BeginId: " + begin_id
                + " EndId: " + end_id
                + " CaseSize: " + case_size
                + " DefSet: " + defSet
                + " UseSet: " + useSet;
    }
}

