package config;

import java.util.*;

public class ExecutionConfig {

    public static Boolean debug = false;
    public static boolean useVMOptions = true;
    public static Set<String> TESTED_JVMS = new HashSet<>();
    public static String TESTED_BENCHMARK;
    public static String TESTED_BENCHMARK_HOME;
    public static String TESTED_SOOTOUTPUT_HOME;
    public static Set<String> FILTER_WORDS = new HashSet<>();
    public static Set<String> RESULT_KEY_WORDS = new HashSet<>();
    public static Set<String> PROJECTS_ELEMENTS = new HashSet<>();
    public static Set<String> DIAGNOSTIC_OPTIONS = new HashSet<>();
    public static Set<String> OPTION_FILTER_KEYWORDS = new HashSet<>();
    public static Set<String> AVAILABILITY_FILTER_VERSION = new HashSet<>();
    public static Set<String> OPTION_INVALID_TYPE = new HashSet<>();
    public static Map<String, String> OPTIONS_CONSTRAINTS_VALUE = new HashMap();
    public static String PROJECT_RUNTIME_CONFIG;
    public static int OPTION_MAX_SIZE = 5;
    public static int MAX_SYNTHESIS_TIME = 10;
    public static long CLASS_MAX_RUNTIME = 5;
    public static Set<String> PROJECTS_FILTER_CLASSES = new HashSet<>();
}
