package vmoptions;

import config.ExecutionConfig;
import config.ExecutionRandom;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptionWheel {

    public static Option wheel(List<Option> options) {
        return options.get(ExecutionRandom.nextChoice(options.size()));
    }
    public static ArrayList<String> wheel(String vmName, List<Option> options, int maxOptionSize) {

        /**
         * select candidate options
         */
        HashSet<Option> candidate = new HashSet<>();
        for (int i = 0; i < maxOptionSize; i++) {
            if (options.size() > 0){
                candidate.add(options.get(ExecutionRandom.nextChoice(options.size())));
            }
        }
        ArrayList<String> ret = new ArrayList<>();
        for (Option option : candidate) {
            String opt = initVMOption(vmName, option);
            if (opt != null) {
                ret.add(opt);
            }
        }
        return ret;
    }

    /**
     * init vm option according to their type, default value and prefix
     * @param option
     * @return
     */
    public static String initVMOption(String vmName, Option option) {

        if (vmName.toLowerCase().contains("hotspot")
				|| vmName.toLowerCase().contains("bisheng")) {
            return initHotSpotOption(option);
        } else if (vmName.toLowerCase().contains("openj9")) {
            return initOpenJ9Option(option);
        } else {
            System.err.println("WARNING: UNDEFINED OPTION INIT FOR" + vmName + " - initVMOption");
        }
        return null;
    }

    public static String initHotSpotOption(Option option) {

        String optStr = null;
        String optValue = "";

        switch (option.getType()) {
            case "bool":
                if (option.getDefaultValue() != null) {
                    if (option.getDefaultValue().contains("true")) {
                        if (ExecutionRandom.flipCoin(20)) {
                            optStr = option.getPrefix() + "+" + option.getName();
                            optValue = "true";
                        } else {
                            optStr = option.getPrefix() + "-" + option.getName();
                            optValue = "false";
                        }
                    } else if (option.getDefaultValue().contains("false")) {
                        if (ExecutionRandom.flipCoin(20)) {
                            optStr = option.getPrefix() + "-" + option.getName();
                            optValue = "false";
                        } else {
                            optStr = option.getPrefix() + "+" + option.getName();
                            optValue = "true";
                        }
                    } else {
                        System.err.println("WARNING: UNKNOWN OPTION DEFAULT VALUE - initHotSpotOption");
                    }
                } else {

                    if (ExecutionRandom.flipCoin()) {
                        optStr = option.getPrefix() + "+" + option.getName();
                        optValue = "true";
                    } else {
                        optStr = option.getPrefix() + "-" + option.getName();
                        optValue = "false";
                    }
                }
                break;
            case "intx":

                if (option.getRange() != null) {

//                    System.out.println("Min: " + option.getMinValue() + ", Max: " + option.getMaxValue());
//                    String value = String.valueOf(ExecutionRandom.nextChoice(option.getMinValue(), option.getMaxValue()));
                    if (option.getMaxValue() - option.getMaxValue() > 10 ) {
                        if (ExecutionRandom.flipCoin()) {
                            optValue = String.valueOf(ExecutionRandom.nextChoice(option.getMaxValue() + 10));
                        } else {
                            optValue = String.valueOf(ExecutionRandom.nextChoice(option.getMaxValue() - 10));
                        }
                    } else {
                        optValue = String.valueOf(ExecutionRandom.nextChoice(option.getMinValue(), option.getMaxValue()));
                    }
                    optStr = option.getPrefix() + option.getName() + "=" + optValue;
                } else {
                    optValue = String.valueOf(ExecutionRandom.nextChoice(Integer.MAX_VALUE));
                    optStr = option.getPrefix() + option.getName() + "=" + optValue;
                }
                break;
            case "double":

                if (option.getRange() != null) {

//                    String value = String.format("%." + 1 + "f", Float.valueOf(ExecutionRandom.nextChoice(option.getMinValue(), option.getMaxValue())));
                    if (option.getMaxValue() - option.getMaxValue() > 10 ) {
                        if (ExecutionRandom.flipCoin()) {
                            optValue = String.valueOf(ExecutionRandom.nextChoice(option.getMaxValue() + 10));
                        } else {
                            optValue = String.valueOf(ExecutionRandom.nextChoice(option.getMaxValue() - 10));
                        }
                    } else {
                        optValue = String.valueOf(ExecutionRandom.nextChoice(option.getMinValue(), option.getMaxValue()));
                    }
                    optValue = String.format("%." + 1 + "f", Float.valueOf(optValue));
                    optStr = option.getPrefix() + option.getName() + "=" + optValue;
                } else {
                    optValue = String.format("%." + 1 + "f", Float.valueOf(ExecutionRandom.nextChoice(Integer.MAX_VALUE)));
                    optStr = option.getPrefix() + option.getName() + "=" + optValue;
                }
                break;
            default:
                if (option.getType().equals("uintx")) {

                    if (option.getRange() != null) {
//                        String value = String.valueOf(ExecutionRandom.nextChoice(option.getMinValue(), option.getMaxValue()));
                        if (option.getMaxValue() - option.getMaxValue() > 10 ) {
                            if (ExecutionRandom.flipCoin()) {
                                optValue = String.valueOf(ExecutionRandom.nextChoice(option.getMaxValue() + 10));
                            } else {
                                optValue = String.valueOf(ExecutionRandom.nextChoice(option.getMaxValue() - 10));
                            }
                        } else {
                            optValue = String.valueOf(ExecutionRandom.nextChoice(option.getMinValue(), option.getMaxValue()));
                        }
                        optStr = option.getPrefix() + option.getName() + "=" + optValue;
                    } else {
                        optValue = String.valueOf(ExecutionRandom.nextChoice(Integer.MAX_VALUE));
                        optStr = option.getPrefix() + option.getName() + "=" + optValue;
                    }
                }
                break;
        }
        //option constraints
        if (ExecutionConfig.OPTIONS_CONSTRAINTS_VALUE.keySet().contains(option.getName())) {

            //  MaxNodeLimit -> NodeLimitFudgeFactor:=MaxNodeLimit(0.02,0.4);
            String value = ExecutionConfig.OPTIONS_CONSTRAINTS_VALUE.get(option.getName());
            String[] cOptions = value.split(":=");
            if (cOptions.length == 2) {
                String cOptName = cOptions[0];
                String cRange = cOptions[1];
                String cValue = null;
                Pattern pattern = Pattern.compile("\\((.*?),(.*?)\\)");
                Matcher matcher = pattern.matcher(cRange);
                if (matcher.find()) {
                    String minValue = matcher.group(1).trim();
                    String maxValue = matcher.group(2).trim();
                    Float minBounder = 0.0f;
                    Float maxBounder = 0.0f;
                    if (NumberUtils.isCreatable(minValue)) {
                        minBounder = Float.parseFloat(minValue);
                    }
                    if (NumberUtils.isCreatable(maxValue)) {
                        maxBounder = Float.parseFloat(maxValue);
                    }
                    cValue = String.valueOf(ExecutionRandom.nextChoice((int) (Integer.valueOf(optValue) * minBounder),
                            (int) (Integer.valueOf(optValue) * maxBounder)));

                }
                if (cValue != null) {
                    optStr += option.getPrefix() + cOptName + "=" + cValue;
                }
            }
        }
        return optStr;
    }

    public static String initOpenJ9Option(Option option) {

        String optStr = null;
        if (option.getName().equals("jit")) {
//            if (DTRandom.flipCoin()) {
                optStr = option.getPrefix() + option.getName();
//            } else {
//                int count = DTRandom.nextChoice(Integer.MAX_VALUE);
//                optStr = option.getPrefix() + option.getName() + ":count=<" + count + ">";
//            }
        } else if (option.getName().contains("<size>")) {
            int size = ExecutionRandom.nextChoice(Integer.MAX_VALUE);
            optStr = option.getPrefix() + option.getName().replace("size", String.valueOf(size));
        } else {
            if (option.getPrefix() != null) {
                if (option.getPrefix().equals("-X")) {
                    optStr = option.getPrefix() + option.getName();
                } else if (option.getPrefix().equals("-XX:")) {
                    if (ExecutionRandom.flipCoin()) {
                        optStr = option.getPrefix() + "+" + option.getName();
                    } else {
                        optStr = option.getPrefix() + "-" + option.getName();
                    }
                }
            }
        }
        return optStr;
    }
}
