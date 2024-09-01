package codegen.providers;

import config.FuzzingRandom;
import soot.jimple.NegExpr;
import config.FuzzingConfig;
import soot.*;
import soot.jimple.Expr;
import soot.jimple.Jimple;
import soot.jimple.internal.*;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OperatorProvider {

    public static Value placeholder1 = Jimple.v().newLocal("placeholder1", IntType.v());
    public static Value placeholder2 = Jimple.v().newLocal("placeholder2", IntType.v());
    public static Type placeholder3 = IntType.v();

    //算术运算符
    public static JAddExpr addExpr = (JAddExpr) Jimple.v().newAddExpr(placeholder1, placeholder2); // +
    public static JSubExpr subExpr = (JSubExpr) Jimple.v().newSubExpr(placeholder1, placeholder2); // -
    public static JMulExpr mulExpr = (JMulExpr) Jimple.v().newMulExpr(placeholder1, placeholder2); // *
    public static JDivExpr divExpr = (JDivExpr) Jimple.v().newDivExpr(placeholder1, placeholder2); // /

    public static JRemExpr remExpr = (JRemExpr) Jimple.v().newRemExpr(placeholder1, placeholder2); // %

    public static JCastExpr castExpr = (JCastExpr) Jimple.v().newCastExpr(placeholder1, placeholder3); //checkcast

    //逻辑运算符
    public static JOrExpr orExpr = (JOrExpr) Jimple.v().newOrExpr(placeholder1, placeholder2);
    public static JAndExpr andExpr = (JAndExpr) Jimple.v().newAndExpr(placeholder1, placeholder2);
    public static JXorExpr xorExpr = (JXorExpr) Jimple.v().newXorExpr(placeholder1, placeholder2);
    public static JNegExpr negExpr = (JNegExpr) Jimple.v().newNegExpr(placeholder1);
    public static JShlExpr shlExpr = (JShlExpr) Jimple.v().newShlExpr(placeholder1, placeholder2);
    public static JShrExpr shrExpr = (JShrExpr) Jimple.v().newShrExpr(placeholder1, placeholder2);
    public static JUshrExpr ushrExpr = (JUshrExpr) Jimple.v().newUshrExpr(placeholder1, placeholder2);

    //关系运算符
    public static JEqExpr eqExpr = (JEqExpr) Jimple.v().newEqExpr(placeholder1, placeholder2);
    public static JGtExpr gtExpr = (JGtExpr) Jimple.v().newGtExpr(placeholder1, placeholder2);
    public static JGeExpr geExpr = (JGeExpr) Jimple.v().newGeExpr(placeholder1, placeholder2);
    public static JLtExpr ltExpr = (JLtExpr) Jimple.v().newLtExpr(placeholder1, placeholder2);
    public static JLeExpr leExpr = (JLeExpr) Jimple.v().newLeExpr(placeholder1, placeholder2);
    public static JNeExpr neExpr = (JNeExpr) Jimple.v().newNeExpr(placeholder1, placeholder2);

    public static HashMap<String, HashMap<Expr, Integer>> operators = new HashMap<>();

    public static List<Expr> operatorList = new ArrayList<>();

    static {


        operators.put("Arith", FuzzingConfig.PROB_ARITH_GROUP);

        HashMap<Expr, Integer> refOperators = new HashMap<>();
        refOperators.put(castExpr, FuzzingConfig.PROB_CAST_VALUE);

        operators.put("Ref", refOperators);
        operators.put("Relation", FuzzingConfig.PROB_RELATION_GROUP);
        operators.put("Bitwise", FuzzingConfig.PROB_LOGIC_GROUP);

        operatorList.add(addExpr);
        operatorList.add(subExpr);
        operatorList.add(mulExpr);
        operatorList.add(divExpr);

        operatorList.add(remExpr);

        operatorList.add(castExpr);

        operatorList.add(orExpr);
        operatorList.add(andExpr);
        operatorList.add(xorExpr);
        operatorList.add(negExpr);
        operatorList.add(shlExpr);
        operatorList.add(shrExpr);
        operatorList.add(ushrExpr);

        operatorList.add(eqExpr);
        operatorList.add(gtExpr);
        operatorList.add(geExpr);
        operatorList.add(ltExpr);
        operatorList.add(leExpr);
        operatorList.add(neExpr);
    }

    public static Expr anyOperator(Value op1, Value op2){

        if (op1 instanceof PrimType && op2 instanceof PrimType) {
            return FuzzingRandom.randomUpTo(operators.get("Arith"));
        }
        return null;
    }

    public static Expr anyOperator(String type) {

        if (operators.keySet().contains(type)) {
            return FuzzingRandom.randomUpTo(operators.get(type));
        } else {
            throw new RuntimeException("UNKNOWN Type: " + type);
        }
    }

    public static Expr anyOperator(Type type) {

        if (type instanceof PrimType) {

            if (!(type instanceof IntType)) {
                return FuzzingRandom.randomUpTo(operators.get("Arith"));
            } else {
                if (FuzzingRandom.flipCoin()) {
                    return FuzzingRandom.randomUpTo(operators.get("Arith"));
                } else {
                    return FuzzingRandom.randomUpTo(operators.get("Bitwise"));
                }
            }
        } else if (type instanceof RefType) {

            if (((RefType) type).getClassName().equals("java.lang.String")) {
                return addExpr;
            } else {
                return null;
            }
        }
        return null;
    }

    public static Object createOperatorFormulaStmt(Expr operator, Value var1, Value var2) {

        try {
            Class<? extends Expr> operatorClass = operator.getClass();
            if (operator instanceof NegExpr) {
                Constructor method = operatorClass.getConstructor(Value.class);
                Object stmt = method.newInstance(var2);
                return stmt;
            } else {
                Constructor method = operatorClass.getConstructor(Value.class, Value.class);
                Object stmt = method.newInstance(var1, var2);
                return stmt;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object createOperatorFormulaStmt(Expr operator, Value var1) {

        try {
            Class<? extends Expr> operatorClass = operator.getClass();
            Constructor method = operatorClass.getConstructor(Value.class);
            Object stmt = method.newInstance(var1);
            return stmt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
