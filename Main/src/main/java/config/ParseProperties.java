package config;

import codegen.operators.*;
import codegen.providers.OperatorProvider;
import codegen.providers.TypeProvider;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ParseProperties {
    public static final String SEMICOLON = ";";
    public static void parseExecutionProperties(Properties properties) {

        storePropertyValues(properties.getProperty(ExecutionProperties.TESTED_JVMS_KEY), ExecutionConfig.TESTED_JVMS);

        ExecutionConfig.TESTED_BENCHMARK = properties.getProperty(ExecutionProperties.TESTED_BENCHMARK_NAME_KEY);
        ExecutionConfig.TESTED_BENCHMARK_HOME = properties.getProperty(ExecutionProperties.TESTED_BENCHMARK_KEY);
        ExecutionConfig.TESTED_SOOTOUTPUT_HOME = properties.getProperty(ExecutionProperties.TESTED_SOOTOUTPUT_KEY);

        storePropertyValues(properties.getProperty(ExecutionProperties.RESULT_KEY_WORDS_KEY), ExecutionConfig.RESULT_KEY_WORDS);
        storePropertyValues(properties.getProperty(ExecutionProperties.PROJECTS_ELEMENTS_KEY), ExecutionConfig.PROJECTS_ELEMENTS);
        storePropertyValues(properties.getProperty(ExecutionProperties.DIAGNOSTIC_OPTIONS_KEY), ExecutionConfig.DIAGNOSTIC_OPTIONS);
        storePropertyValues(properties.getProperty(ExecutionProperties.OPTION_FILTER_KEYWORDS_KEY), ExecutionConfig.OPTION_FILTER_KEYWORDS);
        storePropertyValues(properties.getProperty(ExecutionProperties.AVAILABILITY_FILTER_VERSION_KEY), ExecutionConfig.AVAILABILITY_FILTER_VERSION);
        storePropertyValues(properties.getProperty(ExecutionProperties.OPTION_INVALID_TYPE_KEY), ExecutionConfig.OPTION_INVALID_TYPE);
        storePropertyValues(properties.getProperty(ExecutionProperties.OPTION_CONSTRAINTS_VALUE_KEY), ExecutionConfig.OPTIONS_CONSTRAINTS_VALUE);

        ExecutionConfig.PROJECT_RUNTIME_CONFIG = properties.getProperty(ExecutionProperties.PROJECTS_RUNTIME_CONFIG_KEY);
        ExecutionConfig.OPTION_MAX_SIZE = Integer.parseInt(properties.getProperty(ExecutionProperties.OPTION_MAX_SIZE_KEY));
        ExecutionConfig.CLASS_MAX_RUNTIME = Long.parseLong(properties.getProperty(ExecutionProperties.CLASS_MAX_RUNTIME_KEY));

        storePropertyValues(properties.getProperty(ExecutionProperties.PROJECTS_FILTER_CLASSES_KEY), ExecutionConfig.PROJECTS_FILTER_CLASSES);
    }
    public static void parseFuzzingProperties(Properties properties) {

        // max value configurations
        FuzzingConfig.MAX_FIELD_NUM = Integer.parseInt(properties.getProperty(FuzzingProperties.MAX_FIELD_NUM_KEY));
        FuzzingConfig.MAX_FUNS_NUM = Integer.parseInt(properties.getProperty(FuzzingProperties.MAX_FUNS_NUM_KEY));
        FuzzingConfig.MAX_FUNS_PARAMS = Integer.parseInt(properties.getProperty(FuzzingProperties.MAX_FUNS_PARAMS_KEY));
        FuzzingConfig.MAX_METHOD_INVOCATION = Integer.parseInt(properties.getProperty(FuzzingProperties.MAX_METHOD_INVOCATION_KEY));

        FuzzingConfig.MAX_ARRAY_DIM = Integer.parseInt(properties.getProperty(FuzzingProperties.MAX_ARRAY_DIM_KEY));
        FuzzingConfig.MAX_ARRAY_SIZE_PERDIM = Integer.parseInt(properties.getProperty(FuzzingProperties.MAX_ARRAY_SIZE_PERDIM_KEY));
        FuzzingConfig.MAX_ARRAY_LENGTH = Integer.parseInt(properties.getProperty(FuzzingProperties.MAX_ARRAY_LENGTH_KEY));

        FuzzingConfig.MAX_FUZZ_STEP = Integer.parseInt(properties.getProperty(FuzzingProperties.MAX_FUZZ_STEP_KEY));
        FuzzingConfig.MAX_NESTED_SIZE = Integer.parseInt(properties.getProperty(FuzzingProperties.MAX_NESTED_SIZE_KEY));
        FuzzingConfig.MAX_BLOCK_INST = Integer.parseInt(properties.getProperty(FuzzingProperties.MAX_BLOCK_INST_KEY));
        FuzzingConfig.MAX_LOOP_SIZE = Integer.parseInt(properties.getProperty(FuzzingProperties.MAX_LOOP_SIZE_KEY));
        FuzzingConfig.MAX_LOOP_STEP = Integer.parseInt(properties.getProperty(FuzzingProperties.MAX_LOOP_STEP_KEY));
        FuzzingConfig.MAX_SWITCH_CASES = Integer.parseInt(properties.getProperty(FuzzingProperties.MAX_SWITCH_CASES_KEY));

        FuzzingConfig.PROB_ADD_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_ADD_VALUE_KEY));
        FuzzingConfig.PROB_SUB_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_SUB_VALUE_KEY));
        FuzzingConfig.PROB_MUL_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_MUL_VALUE_KEY));
        FuzzingConfig.PROB_DIV_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_DIV_VALUE_KEY));
        FuzzingConfig.PROB_MOD_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_MOD_VALUE_KEY));
        FuzzingConfig.PROB_ARITH_GROUP.put(OperatorProvider.addExpr, FuzzingConfig.PROB_ADD_VALUE);
        FuzzingConfig.PROB_ARITH_GROUP.put(OperatorProvider.subExpr, FuzzingConfig.PROB_SUB_VALUE);
        FuzzingConfig.PROB_ARITH_GROUP.put(OperatorProvider.mulExpr, FuzzingConfig.PROB_MUL_VALUE);
        FuzzingConfig.PROB_ARITH_GROUP.put(OperatorProvider.divExpr, FuzzingConfig.PROB_DIV_VALUE);
        FuzzingConfig.PROB_ARITH_GROUP.put(OperatorProvider.remExpr, FuzzingConfig.PROB_MOD_VALUE);

        FuzzingConfig.PROB_CAST_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_CAST_VALUE_KEY));

        FuzzingConfig.PROB_OR_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_OR_VALUE_KEY));
        FuzzingConfig.PROB_AND_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_AND_VALUE_KEY));
        FuzzingConfig.PROB_XOR_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_XOR_VALUE_KEY));
        FuzzingConfig.PROB_NEG_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_NEG_VALUE_KEY));
        FuzzingConfig.PROB_SHL_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_SHL_VALUE_KEY));
        FuzzingConfig.PROB_SHR_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_SHR_VALUE_KEY));
        FuzzingConfig.PROB_USHR_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_USHR_VALUE_KEY));
        FuzzingConfig.PROB_LOGIC_GROUP.put(OperatorProvider.orExpr, FuzzingConfig.PROB_OR_VALUE);
        FuzzingConfig.PROB_LOGIC_GROUP.put(OperatorProvider.andExpr, FuzzingConfig.PROB_AND_VALUE);
        FuzzingConfig.PROB_LOGIC_GROUP.put(OperatorProvider.xorExpr, FuzzingConfig.PROB_XOR_VALUE);
        FuzzingConfig.PROB_LOGIC_GROUP.put(OperatorProvider.negExpr, FuzzingConfig.PROB_NEG_VALUE);
        FuzzingConfig.PROB_LOGIC_GROUP.put(OperatorProvider.shlExpr, FuzzingConfig.PROB_SHL_VALUE);
        FuzzingConfig.PROB_LOGIC_GROUP.put(OperatorProvider.shrExpr, FuzzingConfig.PROB_SHR_VALUE);
        FuzzingConfig.PROB_LOGIC_GROUP.put(OperatorProvider.ushrExpr, FuzzingConfig.PROB_USHR_VALUE);

        FuzzingConfig.PROB_EQ_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_EQ_VALUE_KEY));
        FuzzingConfig.PROB_GT_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_GT_VALUE_KEY));
        FuzzingConfig.PROB_GE_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_GE_VALUE_KEY));
        FuzzingConfig.PROB_LT_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_LT_VALUE_KEY));
        FuzzingConfig.PROB_LE_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_LE_VALUE_KEY));
        FuzzingConfig.PROB_RELATION_GROUP.put(OperatorProvider.eqExpr, FuzzingConfig.PROB_EQ_VALUE);
        FuzzingConfig.PROB_RELATION_GROUP.put(OperatorProvider.gtExpr, FuzzingConfig.PROB_GT_VALUE);
        FuzzingConfig.PROB_RELATION_GROUP.put(OperatorProvider.geExpr, FuzzingConfig.PROB_GE_VALUE);
        FuzzingConfig.PROB_RELATION_GROUP.put(OperatorProvider.ltExpr, FuzzingConfig.PROB_LT_VALUE);
        FuzzingConfig.PROB_RELATION_GROUP.put(OperatorProvider.leExpr, FuzzingConfig.PROB_LE_VALUE);

        FuzzingConfig.PROB_CHAR_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_CHAR_VALUE_KEY));
        FuzzingConfig.PROB_INT_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_INT_VALUE_KEY));
        FuzzingConfig.PROB_BOOL_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_BOOL_VALUE_KEY));
        FuzzingConfig.PROB_FLOAT_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_FLOAT_VALUE_KEY));
        FuzzingConfig.PROB_DOUBLE_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_DOUBLE_VALUE_KEY));
        FuzzingConfig.PROB_LONG_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_LONG_VALUE_KEY));
        FuzzingConfig.PROB_SHORT_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_SHORT_VALUE_KEY));
        FuzzingConfig.PROB_OBJECT_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_OBJECT_VALUE_KEY));
        FuzzingConfig.PROB_TYPE_GROUP.put(TypeProvider.charType, FuzzingConfig.PROB_CHAR_VALUE);
        FuzzingConfig.PROB_TYPE_GROUP.put(TypeProvider.intType, FuzzingConfig.PROB_INT_VALUE);
        FuzzingConfig.PROB_TYPE_GROUP.put(TypeProvider.booleanType, FuzzingConfig.PROB_BOOL_VALUE);
        FuzzingConfig.PROB_TYPE_GROUP.put(TypeProvider.floatType, FuzzingConfig.PROB_FLOAT_VALUE);
        FuzzingConfig.PROB_TYPE_GROUP.put(TypeProvider.doubleType, FuzzingConfig.PROB_DOUBLE_VALUE);
        FuzzingConfig.PROB_TYPE_GROUP.put(TypeProvider.longType, FuzzingConfig.PROB_LONG_VALUE);
        FuzzingConfig.PROB_TYPE_GROUP.put(TypeProvider.shortType, FuzzingConfig.PROB_SHORT_VALUE);
        FuzzingConfig.PROB_TYPE_GROUP.put(TypeProvider.refType, FuzzingConfig.PROB_OBJECT_VALUE);

        FuzzingConfig.PROB_NULL_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_NULL_VALUE_KEY));

        FuzzingConfig.PROB_GLOBAL_FIELD = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_GLOBAL_FIELD_KEY));
        FuzzingConfig.PROB_NEW_ARRAY = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_NEW_ARRAY_KEY));
        FuzzingConfig.PROB_REUSE_VAR = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_REUSE_VAR_KEY));
        FuzzingConfig.PROB_REUSE_REF_VAR = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_REUSE_REF_VAR_KEY));

        FuzzingConfig.PROB_VOID_METHOD = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_VOID_METHOD_KEY));
        FuzzingConfig.PROB_STATIC_FIELD = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_STATIC_FIELD_KEY));
        FuzzingConfig.PROB_VOLATILE_FIELD = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_VOLATILE_FIELD_KEY));

        FuzzingConfig.PROB_API_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_API_VALUE_KEY));
        FuzzingConfig.PROB_FUNC_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_FUNC_VALUE_KEY));
        FuzzingConfig.PROB_ARITH_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_ARITH_VALUE_KEY));
        FuzzingConfig.PROB_ARRAY_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_ARRAY_VALUE_KEY));
        FuzzingConfig.PROB_IF_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_IF_VALUE_KEY));
        FuzzingConfig.PROB_LOOP_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_LOOP_VALUE_KEY));
        FuzzingConfig.PROB_SWITCH_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_SWITCH_VALUE_KEY));
        FuzzingConfig.PROB_TRAP_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_TRAP_VALUE_KEY));
        FuzzingConfig.PROB_OPERATOR_GROUP.put(ApiOperator.getInstance(), FuzzingConfig.PROB_API_VALUE);
        FuzzingConfig.PROB_OPERATOR_GROUP.put(FuncOperator.getInstance(), FuzzingConfig.PROB_FUNC_VALUE);
        FuzzingConfig.PROB_OPERATOR_GROUP.put(ArithOperator.getInstance(), FuzzingConfig.PROB_ARITH_VALUE);
        FuzzingConfig.PROB_OPERATOR_GROUP.put(ArrayOperator.getInstance(), FuzzingConfig.PROB_ARRAY_VALUE);
        FuzzingConfig.PROB_OPERATOR_GROUP.put(IfOperator.getInstance(), FuzzingConfig.PROB_IF_VALUE);
        FuzzingConfig.PROB_OPERATOR_GROUP.put(LoopOperator.getInstance(), FuzzingConfig.PROB_LOOP_VALUE);
        FuzzingConfig.PROB_OPERATOR_GROUP.put(SwitchOperator.getInstance(), FuzzingConfig.PROB_SWITCH_VALUE);
        FuzzingConfig.PROB_OPERATOR_GROUP.put(TrapOperator.getInstance(), FuzzingConfig.PROB_TRAP_VALUE);

        FuzzingConfig.PROB_API_INTERNAL_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_API_INTERNAL_VALUE_KEY));
        FuzzingConfig.PROB_ARITH_INTERNAL_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_ARITH_INTERNAL_VALUE_KEY));
        FuzzingConfig.PROB_ARRAY_INTERNAL_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_ARRAY_INTERNAL_VALUE_KEY));
        FuzzingConfig.PROB_INTERNAL_OPERATOR_GROUP.put(ApiOperator.getInstance(), FuzzingConfig.PROB_API_INTERNAL_VALUE);
        FuzzingConfig.PROB_INTERNAL_OPERATOR_GROUP.put(ArithOperator.getInstance(), FuzzingConfig.PROB_ARITH_INTERNAL_VALUE);
        FuzzingConfig.PROB_INTERNAL_OPERATOR_GROUP.put(ArrayOperator.getInstance(), FuzzingConfig.PROB_ARRAY_INTERNAL_VALUE);

        FuzzingConfig.PROB_RETURN_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_RETURN_VALUE_KEY));
        FuzzingConfig.PROB_BREAK_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_BREAK_VALUE_KEY));
        FuzzingConfig.PROB_GOTO_VALUE = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_GOTO_VALUE_KEY));
        FuzzingConfig.PROB_REUSE_INST = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_REUSE_INST_KEY));

        FuzzingConfig.PROB_STATIC_METHOD_INVOCATION = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_STATIC_METHOD_INVOCATION_KEY));
        FuzzingConfig.PROB_SELF_METHOD_INVOCATION = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_SELF_METHOD_INVOCATION_KEY));

        FuzzingConfig.PROB_INITIALIZE_METHOD = Integer.parseInt(properties.getProperty(FuzzingProperties.PROB_INITIALIZE_METHOD_VALUE_KEY));

        storePropertyValues(properties.getProperty(FuzzingProperties.INVALID_REFERENCE_TYPE_KEY), FuzzingConfig.INVALID_REFERENCE_TYPE);
        storePropertyValues(properties.getProperty(FuzzingProperties.INVALID_EXCEPTION_TYPE_KEY), FuzzingConfig.INVALID_EXCEPTION_TYPE);
    }

    private static void storePropertyValues(String values, Set<String> toSet) {

        if (values != null) {
            String[] split = values.split(SEMICOLON);
            for (String val : split) {
                val = val.trim();
                if (!val.isEmpty()) {
                    toSet.add(val);
                }
            }
        }
    }

    private static void storePropertyValues(String values, Map<String, String> toMap) {

        if (values != null) {
            String[] split = values.split(SEMICOLON);
            for (String val : split) {
                val = val.trim();
                if (!val.isEmpty()) {

                    if (val.contains("->")) {
                        String key = val.split("->")[0];
                        String value = val.split("->")[1];
                        toMap.put(key, value);
                    } else {
                        //TODO for future
                    }
//                    toSet.add(val);
                }
            }
        }
    }
}
