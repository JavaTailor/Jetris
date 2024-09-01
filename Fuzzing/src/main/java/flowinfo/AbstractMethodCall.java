package flowinfo;

import soot.Type;
import soot.UnitPrinter;
import soot.ValueBox;
import soot.jimple.Expr;
import soot.util.Switch;

import java.util.List;

public class AbstractMethodCall implements Expr {

    public String methodName;
    public int callTimes;

    public AbstractMethodCall() {
    }

    public AbstractMethodCall(String methodName) {
        this.methodName = methodName;
        this.callTimes = 1;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getCallTimes() {
        return callTimes;
    }

    public void setCallTimes(int callTimes) {
        this.callTimes = callTimes;
    }

    public void callTimeIncrease() {
        this.callTimes++;
    }

    @Override
    public String toString() {
        return methodName + "*" + callTimes;
    }

    @Override
    public List<ValueBox> getUseBoxes() {
        return null;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public Object clone() {
        return null;
    }

    @Override
    public void toString(UnitPrinter up) {

    }

    @Override
    public boolean equivTo(Object o) {
        return false;
    }

    @Override
    public int equivHashCode() {
        return 0;
    }

    @Override
    public void apply(Switch sw) {

    }
}
