package vmoptions;

import config.ExecutionConfig;
import config.ExecutionRandom;
import execute.executor.Executor;

import java.util.ArrayList;
import java.util.List;

public class OptionValidate {

    public static String CMD = "JAVACMD VMOPTIONS -cp CLASSPATH CLASSNAME";

    //UseOldInlining de
    //InteriorEntryAlignment  guarantee(CodeEntryAlignment >= InteriorEntryAlignment) failed:

    public static void main(String[] args) {

        String vmImpl = "hotspot";
        String javaVersion = "OpenJDK8";
        String JavaCmd = "/Users/yingquanzhao/Workspace/JVM/02JIT/Projects/JITFuzzing/01JVMS/macOSx64/openjdk8/OpenJDK8U-jre_x64_mac_hotspot_8u372b07/Contents/Home/bin/java";

        VMOptions options = ParseVmOptions.parse("hotspot", javaVersion);
        List<Option> ret = options.getOptionsByComponent("jit", true);

        String classPath = "./DTJVM/target/classes";
        String className = "examples.HelloJVM";

        CMD = CMD.replace("JAVACMD", JavaCmd)
                .replace("CLASSPATH", classPath)
                .replace("CLASSNAME", className);

        String cmd = CMD;

        ArrayList<Option> opts = new ArrayList<>();
        for (Option option : ret) {

            if (
//                    option.getAvailability().equals("develop") ||
                    option.getAvailability().equals("notproduct") ||
//                    option.getAvailability().equals("develop_pd") ||
                    option.getAvailability().equals("define_pd_global")) {

//                String optStr = initOptionWithMaxValue(option);
//                optStr = "-XX:+UnlockExperimentalVMOptions -XX:+UnlockDiagnosticVMOptions " + optStr;
//                cmd = CMD.replace("VMOPTIONS", optStr);

//                System.out.println(cmd);
//                Executor.getInstance().execute(cmd);
            } else {

//                String optStr = initOptionWithMaxValue(option);
//
//                optStr = "-XX:+UnlockExperimentalVMOptions -XX:+UnlockDiagnosticVMOptions " + optStr;
//                cmd = CMD.replace("VMOPTIONS", optStr);
//
//                System.out.println(cmd);
//                Executor.getInstance().execute(cmd);
                opts.add(option);
//                System.out.println("OPT: " + option);
            }
        }
        System.out.println(opts.size());
    }

    public static String initOptionWithMaxValue(Option option) {

        String optStr = null;
        switch (option.getType()) {
            case "bool":
                if (option.getDefaultValue() != null) {
                    if (option.getDefaultValue().contains("true")) {
                        if (ExecutionRandom.flipCoin(20)) {
                            optStr = option.getPrefix() + "+" + option.getName();
                        } else {
                            optStr = option.getPrefix() + "-" + option.getName();
                        }
                    } else if (option.getDefaultValue().contains("false")) {
                        if (ExecutionRandom.flipCoin(20)) {
                            optStr = option.getPrefix() + "-" + option.getName();
                        } else {
                            optStr = option.getPrefix() + "+" + option.getName();
                        }
                    } else {
                        System.err.println("WARNING: UNKNOWN OPTION DEFAULT VALUE - initHotSpotOption");
                    }
                } else {

                    if (ExecutionRandom.flipCoin()) {
                        optStr = option.getPrefix() + "+" + option.getName();
                    } else {
                        optStr = option.getPrefix() + "-" + option.getName();
                    }
                }
                break;
            case "intx":

                if (option.getRange() != null) {

//                    System.out.println("Min: " + option.getMinValue() + ", Max: " + option.getMaxValue());
                    String value = String.valueOf(option.getMaxValue());
                    optStr = option.getPrefix() + option.getName() + "=" + value;
                } else {
                    String value = String.valueOf(Integer.MAX_VALUE);
                    optStr = option.getPrefix() + option.getName() + "=" + value;
                }
                break;
            case "double":

                if (option.getRange() != null) {
                    String value = String.format("%." + 1 + "f", Float.valueOf(option.getMaxValue()));
                    optStr = option.getPrefix() + option.getName() + "=" + value;
                } else {
                    String value = String.format("%." + 1 + "f", Float.valueOf(option.getMaxValue()));
                    optStr = option.getPrefix() + option.getName() + "=" + value;
                }
                break;
            default:
                if (option.getType().equals("uintx")) {

                    if (option.getRange() != null) {
                        String value = String.valueOf(option.getMaxValue());
                        optStr = option.getPrefix() + option.getName() + "=" + value;
                    } else {
                        String value = String.valueOf(Integer.MAX_VALUE);
                        optStr = option.getPrefix() + option.getName() + "=" + value;
                    }
                }
                break;
        }
        return optStr;
    }

}
