package codegen.blocks;

import soot.Body;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JLookupSwitchStmt;

import java.util.List;

public class SwitchBlock extends BasicBlock{

    protected JLookupSwitchStmt switchStmt;

    protected AssignStmt init = null;

    public JLookupSwitchStmt getSwitchStmt() {
        return switchStmt;
    }

    public void setSwitchStmt(JLookupSwitchStmt switchStmt) {
        this.switchStmt = switchStmt;
    }

    public void setInit(AssignStmt init) {
        this.init = init;
    }

    public AssignStmt getInit() {
        return init;
    }

    @Override
    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {

        if (target == null) return false;
        Body sootMethodBody = sootMethod.retrieveActiveBody();
        sootMethodBody.getLocals().addAll(localVars);
        if (init != null) sootMethodBody.getUnits().insertBeforeNoRedirect(init, target);
        sootMethodBody.getUnits().insertBeforeNoRedirect(switchStmt, target);
//        System.out.println("switch " + switchStmt);
//        System.out.println("stmts " + stmts);
        for (Stmt stmt: stmts) {
            sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, target);
        }
//        sootMethodBody.getUnits().insertBefore(stmts, target);
        return true;
    }

    @Override
    public String toString() {
        return "SwitchBlock[ " + stmts + " " + switchStmt + " ]";
    }

}
