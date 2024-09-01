package config;

import java.io.InputStream;
import java.util.Properties;

public class ExecutionProperties extends Properties {

    public static final String TESTED_JVMS_KEY = "tested.jvm.jdkhome";
    public static final String TESTED_BENCHMARK_NAME_KEY = "tested.benchmark.name";
    public static final String TESTED_BENCHMARK_KEY = "tested.benchmark.home";
    public static final String TESTED_SOOTOUTPUT_KEY = "tested.sootoutput.home";
    public static final String FILTER_WORDS_KEY = "result.filter.words";
    public static final String RESULT_KEY_WORDS_KEY = "result.key.words";
    public static final String PROJECTS_ELEMENTS_KEY = "projects.elements";
    public static final String DIAGNOSTIC_OPTIONS_KEY = "diagnostic.options";
    public static final String CLASS_MAX_RUNTIME_KEY = "class.max.runtime";
    public static final String OPTION_MAX_SIZE_KEY = "option.max.size";
    public static final String OPTION_FILTER_KEYWORDS_KEY = "option.filter.keywords";
    public static final String OPTION_CONSTRAINTS_VALUE_KEY = "option.constraints.value";
    public static final String AVAILABILITY_FILTER_VERSION_KEY = "availability.filter.version";
    public static final String OPTION_INVALID_TYPE_KEY = "option.invalid.type";
    public static final String PROJECTS_RUNTIME_CONFIG_KEY = "projects.runtime.config.file";
    public static final String PROJECTS_FILTER_CLASSES_KEY = "projects.filter.classes.prefix";

    @Override
    public String getProperty(String key) {
        String prop = System.getProperty(key);
        if (prop == null) {
            prop = super.getProperty(key);
        }
        return prop;
    }
}
