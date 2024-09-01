package config;

import codegen.operators.*;
import codegen.providers.OperatorProvider;
import codegen.providers.TypeProvider;
import org.junit.Test;
import soot.Type;
import soot.jimple.Expr;

import java.util.*;

public class FuzzingConfig {

    // max value configurations
    public static int MAX_FIELD_NUM = 10;
    public static int MAX_FUNS_NUM = 10;
    public static int MAX_FUNS_PARAMS = 5;
    public static int MAX_METHOD_INVOCATION = 15;

    public static int MAX_ARRAY_DIM = 5;
    public static int MAX_ARRAY_SIZE_PERDIM = 20;
    public static int MAX_ARRAY_LENGTH = 256;

    public static int MAX_FUZZ_STEP = 20;
    public static int MAX_NESTED_SIZE = 10;
    public static int MAX_BLOCK_INST = 100;
    public static int MAX_LOOP_SIZE = 100;
    public static int MAX_LOOP_STEP = 5;
    public static int MAX_SWITCH_CASES = 10;

    //probability configurations
    /**
     * group probability for Arith
     */
//    public static
    public static HashMap<Expr, Integer> PROB_ARITH_GROUP = new HashMap<>();
    public static int PROB_ADD_VALUE = 30;
    public static int PROB_SUB_VALUE = 40;
    public static int PROB_MUL_VALUE = 50;
    public static int PROB_DIV_VALUE = 60;
    public static int PROB_MOD_VALUE = 100;

    public static int PROB_CAST_VALUE = 50;

    /**
     * group probability for Logic
     */
    public static HashMap<Expr, Integer> PROB_LOGIC_GROUP = new HashMap<>();
    public static int PROB_OR_VALUE = 20;
    public static int PROB_AND_VALUE = 30;
    public static int PROB_XOR_VALUE = 50;
    public static int PROB_NEG_VALUE = 70;
    public static int PROB_SHL_VALUE = 80;
    public static int PROB_SHR_VALUE = 90;
    public static int PROB_USHR_VALUE = 100;

    /**
     * group probability for Relation
     */
    public static HashMap<Expr, Integer> PROB_RELATION_GROUP = new HashMap<>();
    public static int PROB_EQ_VALUE = 20;
    public static int PROB_GT_VALUE = 30;
    public static int PROB_GE_VALUE = 40;
    public static int PROB_LT_VALUE = 70;
    public static int PROB_LE_VALUE = 100;

    /**
     * group probability for Type
     */
    public static HashMap<Type, Integer> PROB_TYPE_GROUP = new HashMap<>();
    public static int PROB_CHAR_VALUE = 5;
    public static int PROB_INT_VALUE = 40;
    public static int PROB_BOOL_VALUE = 45;
    public static int PROB_FLOAT_VALUE = 60;
    public static int PROB_DOUBLE_VALUE = 75;
    public static int PROB_LONG_VALUE = 85;
    public static int PROB_SHORT_VALUE = 90;
    public static int PROB_OBJECT_VALUE = 100;

    public static int PROB_NULL_VALUE = 50;

    public static int PROB_GLOBAL_FIELD = 50;
    public static int PROB_NEW_ARRAY = 50;
    public static int PROB_REUSE_VAR = 50;
    public static int PROB_REUSE_REF_VAR = 50;

    public static int PROB_VOID_METHOD = 50;
    public static int PROB_STATIC_FIELD = 50;
    public static int PROB_VOLATILE_FIELD = 50;

    /**
     * group probability for Operations
     */
    public static HashMap<Operator, Integer> PROB_OPERATOR_GROUP = new HashMap<>();
    public static int PROB_API_VALUE = 3;
    public static int PROB_FUNC_VALUE = 8;
    public static int PROB_ARITH_VALUE = 45;
    public static int PROB_ARRAY_VALUE = 60;
    public static int PROB_IF_VALUE = 70;
    public static int PROB_LOOP_VALUE = 80;
    public static int PROB_SWITCH_VALUE = 90;
    public static int PROB_TRAP_VALUE = 100;

    public static HashMap<Operator, Integer> PROB_INTERNAL_OPERATOR_GROUP = new HashMap<>();
    public static int PROB_API_INTERNAL_VALUE = 30;
    public static int PROB_ARITH_INTERNAL_VALUE = 60;
    public static int PROB_ARRAY_INTERNAL_VALUE = 100;

    public static int PROB_RETURN_VALUE = 50;
    public static int PROB_BREAK_VALUE = 50;
    public static int PROB_GOTO_VALUE = 50;
    public static int PROB_REUSE_INST = 50;

    public static int PROB_STATIC_METHOD_INVOCATION = 50;

    public static int PROB_SELF_METHOD_INVOCATION = 60;

    public static int PROB_INITIALIZE_METHOD = 50;

    public static Set<String> INVALID_REFERENCE_TYPE = new HashSet<>();
    public static Set<String> INVALID_EXCEPTION_TYPE = new HashSet<>();

    static {

        PROB_ARITH_GROUP.put(OperatorProvider.addExpr, PROB_ADD_VALUE);
        PROB_ARITH_GROUP.put(OperatorProvider.subExpr, PROB_SUB_VALUE);
        PROB_ARITH_GROUP.put(OperatorProvider.mulExpr, PROB_MUL_VALUE);
        PROB_ARITH_GROUP.put(OperatorProvider.divExpr, PROB_DIV_VALUE);
        PROB_ARITH_GROUP.put(OperatorProvider.remExpr, PROB_MOD_VALUE);

        PROB_LOGIC_GROUP.put(OperatorProvider.orExpr, PROB_OR_VALUE);
        PROB_LOGIC_GROUP.put(OperatorProvider.andExpr, PROB_AND_VALUE);
        PROB_LOGIC_GROUP.put(OperatorProvider.xorExpr, PROB_XOR_VALUE);
        PROB_LOGIC_GROUP.put(OperatorProvider.negExpr, PROB_NEG_VALUE);
        PROB_LOGIC_GROUP.put(OperatorProvider.shlExpr, PROB_SHL_VALUE);
        PROB_LOGIC_GROUP.put(OperatorProvider.shrExpr, PROB_SHR_VALUE);
        PROB_LOGIC_GROUP.put(OperatorProvider.ushrExpr, PROB_USHR_VALUE);

        PROB_RELATION_GROUP.put(OperatorProvider.eqExpr, PROB_EQ_VALUE);
        PROB_RELATION_GROUP.put(OperatorProvider.gtExpr, PROB_GT_VALUE);
        PROB_RELATION_GROUP.put(OperatorProvider.geExpr, PROB_GE_VALUE);
        PROB_RELATION_GROUP.put(OperatorProvider.ltExpr, PROB_LT_VALUE);
        PROB_RELATION_GROUP.put(OperatorProvider.leExpr, PROB_LE_VALUE);

        PROB_TYPE_GROUP.put(TypeProvider.charType, PROB_CHAR_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.intType, PROB_INT_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.booleanType, PROB_BOOL_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.floatType, PROB_FLOAT_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.doubleType, PROB_DOUBLE_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.longType, PROB_LONG_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.shortType, PROB_SHORT_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.refType, PROB_OBJECT_VALUE);

        PROB_OPERATOR_GROUP.put(ApiOperator.getInstance(), PROB_API_VALUE);
        PROB_OPERATOR_GROUP.put(FuncOperator.getInstance(), PROB_FUNC_VALUE);
        PROB_OPERATOR_GROUP.put(ArithOperator.getInstance(), PROB_ARITH_VALUE);
        PROB_OPERATOR_GROUP.put(ArrayOperator.getInstance(), PROB_ARRAY_VALUE);
        PROB_OPERATOR_GROUP.put(IfOperator.getInstance(), PROB_IF_VALUE);
        PROB_OPERATOR_GROUP.put(LoopOperator.getInstance(), PROB_LOOP_VALUE);
        PROB_OPERATOR_GROUP.put(SwitchOperator.getInstance(), PROB_SWITCH_VALUE);
        PROB_OPERATOR_GROUP.put(TrapOperator.getInstance(), PROB_TRAP_VALUE);

        PROB_INTERNAL_OPERATOR_GROUP.put(ApiOperator.getInstance(), PROB_API_INTERNAL_VALUE);
        PROB_INTERNAL_OPERATOR_GROUP.put(ArithOperator.getInstance(), PROB_ARITH_INTERNAL_VALUE);
        PROB_INTERNAL_OPERATOR_GROUP.put(ArrayOperator.getInstance(), PROB_ARRAY_INTERNAL_VALUE);
    }

}
