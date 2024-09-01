package vmoptions;

import config.ExecutionConfig;

import java.util.*;
import java.util.stream.Collectors;

public class VMOptions {

    private String vmname;
    private List<Option> options;
    private List<Option> retOptions;
    public VMOptions(String name) {
        this.vmname = name;
        options = new ArrayList<>();
    }

    public VMOptions(String jvmName, String jvmVersion) {
        this.vmname = jvmName + "-" + jvmVersion;
        options = new ArrayList<>();
    }
    public List<Option> getOptionsByComponent(String component) {

        List<Option> ret = new ArrayList<>();
        if (component.toLowerCase().equals("jit")) {

            if (retOptions != null) {
                return retOptions;
            }

            if (vmname.toLowerCase().contains("hotspot")
					|| vmname.toLowerCase().contains("bisheng")) {
                retOptions = options
                        .stream()
                        .filter(option -> option.getComponent() != null && (option.getComponent().equals("c1") || option.getComponent().equals("c2")))
                        .collect(Collectors.toList());
            } else if (vmname.toLowerCase().contains("openj9")) {
                retOptions = options.stream()
                        .filter(option -> option.getName().toLowerCase().contains("jit"))
                        .collect(Collectors.toList());
            } else {
                System.err.println("WARNING: UNDEFINED OPTION INIT FOR " + vmname + " - getOptionsByComponent");
            }
            ret = retOptions;
        }
        return ret;
    }

    /**
     * filter invalid options
     * @param component
     * @param filter
     * @return
     */
    public List<Option> getOptionsByComponent(String component, boolean filter) {

        List<Option> ret = new ArrayList<>();
        if (component.toLowerCase().equals("jit")) {

            if (retOptions != null && filter == false) {
                return retOptions;
            }
            if (vmname.toLowerCase().contains("hotspot")
					|| vmname.toLowerCase().contains("bisheng")) {
                retOptions = options
                        .stream()
                        .filter(option -> option.getComponent() != null && (option.getComponent().equals("c1") || option.getComponent().equals("c2")))
                        .collect(Collectors.toList());
            } else if (vmname.toLowerCase().contains("openj9")) {
                retOptions = options.stream()
                        .filter(option -> option.getName().toLowerCase().contains("jit"))
                        .collect(Collectors.toList());
            } else {
                System.err.println("WARNING: UNDEFINED OPTION INIT FOR" + vmname + " - getOptionsByComponent");
                return ret;
            }
            if (filter) {
                ExecutionConfig.OPTION_FILTER_KEYWORDS.forEach(keyword -> {
                    retOptions = retOptions.stream()
                            .filter(option -> !option.getName().toLowerCase().contains(keyword.toLowerCase()))
                            .collect(Collectors.toList());
                });
                ExecutionConfig.AVAILABILITY_FILTER_VERSION.forEach(version -> {
                    retOptions = retOptions.stream()
                            .filter(option -> option.getAvailability() == null || (option.getAvailability() != null && !option.getAvailability().toLowerCase().contains(version.toLowerCase())))
                            .collect(Collectors.toList());
                });
                ExecutionConfig.OPTION_INVALID_TYPE.forEach(type -> {
                    retOptions = retOptions.stream()
                            .filter(option -> option.getType() == null || (option.getType() != null && !option.getType().toLowerCase().contains(type.toLowerCase())))
                            .collect(Collectors.toList());
                });
            }
            ret = retOptions;
        }
        return ret;
    }

    public List<Option> getOptionsByComponents(ArrayList<String> components, boolean filter) {

        List<Option> ret = new ArrayList<>();
        for (String component : components) {

            if (component.toLowerCase().equals("jit")) {
                if (vmname.toLowerCase().contains("hotspot")
                        || vmname.toLowerCase().contains("bisheng")) {
                    retOptions = options
                            .stream()
                            .filter(option -> option.getComponent() != null && (option.getComponent().equals("c1") || option.getComponent().equals("c2")))
                            .collect(Collectors.toList());
                } else if (vmname.toLowerCase().contains("openj9")) {
                    retOptions = options.stream()
                            .filter(option -> option.getName().toLowerCase().contains("jit"))
                            .collect(Collectors.toList());
                } else {
                    System.err.println("WARNING: UNDEFINED OPTION INIT FOR" + vmname + " - getOptionsByComponent");
                }
            } else if (component.toLowerCase().equals("runtime")) {
                if (vmname.toLowerCase().contains("hotspot")
                        || vmname.toLowerCase().contains("bisheng")) {
                    retOptions = options.stream()
                            .filter(option -> option.getComponent() != null && option.getComponent().equals("runtime"))
                            .collect(Collectors.toList());
                }
            } else {
                System.err.println("WARNING: UNDEFINED OPTION INIT FOR " + vmname + " - getOptionsByComponent");
            }
        }
        if (filter) {
            ExecutionConfig.OPTION_FILTER_KEYWORDS.forEach(keyword -> {
                retOptions = retOptions.stream()
                        .filter(option -> !option.getName().toLowerCase().contains(keyword.toLowerCase()))
                        .collect(Collectors.toList());
            });
            ExecutionConfig.AVAILABILITY_FILTER_VERSION.forEach(version -> {
                retOptions = retOptions.stream()
                        .filter(option -> option.getAvailability() == null || (option.getAvailability() != null && !option.getAvailability().toLowerCase().contains(version.toLowerCase())))
                        .collect(Collectors.toList());
            });
            ExecutionConfig.OPTION_INVALID_TYPE.forEach(type -> {
                retOptions = retOptions.stream()
                        .filter(option -> option.getType() == null || (option.getType() != null && !option.getType().toLowerCase().contains(type.toLowerCase())))
                        .collect(Collectors.toList());
            });
        }
        ret = retOptions;
        return ret;
    }

    public void addOption(Option option){
        options.add(option);
    }
    public String getVmname() {
        return vmname;
    }
    public void setVmname(String vmname) {
        this.vmname = vmname;
    }

    public List<Option> getOptions() {
        return options;
    }
    public void setOptions(List<Option> options) {
        this.options = options;
    }
}
