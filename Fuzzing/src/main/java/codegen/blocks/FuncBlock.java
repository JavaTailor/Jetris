package codegen.blocks;


import soot.SootMethod;

public class FuncBlock extends BasicBlock {
    private SootMethod method;

    public FuncBlock(SootMethod method) {
        this.method = method;
    }

    public void setMethod(SootMethod method) {
        this.method = method;
    }

    public SootMethod getMethod() {
        return method;
    }

    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {
        return false;
    }
}



