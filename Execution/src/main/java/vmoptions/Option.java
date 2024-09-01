package vmoptions;

import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Option {
    private String availability;
    private String component;
    private String defaultValue;
    private String definedIn;
    private String description;
    private int id;
    private String name;
    private String prefix;
    private String range;
    private int minValue = -1;
    private int maxValue = -1;
    private String type;

    public Option() {
    }

    public Option(String availability, String component, String defaultValue, String definedIn, String description, int id, String name, String prefix, String range, String type) {
        this.availability = availability;
        this.component = component;
        this.defaultValue = defaultValue;
        this.definedIn = definedIn;
        this.description = description;
        this.id = id;
        this.name = name;
        this.prefix = prefix;
        this.range = range;
        parseRange();
        this.type = type;
    }

    public int getMinValue() {
        if (range != null) {
            return minValue;
        }
        return 0;
    }

    public int getMaxValue() {
        if (range != null) {
            return maxValue;
        }
       return Integer.MAX_VALUE;
    }
    public void parseRange() {

        if (range == null) {
            return;
        }
        Pattern pattern = Pattern.compile("range\\((.*?),(.*?)\\)");
        Matcher matcher = pattern.matcher(range);
        if (matcher.find()) {
            String minValue = matcher.group(1).trim();
            String maxValue = matcher.group(2).trim();
            if (NumberUtils.isCreatable(minValue)) {
                if (minValue.contains(".")) {
                    this.minValue = (int)Float.parseFloat(minValue);
                } else {
                    this.minValue = Integer.parseInt(minValue);
                }
            } else {
                this.maxValue = 0;
            }
            if (NumberUtils.isCreatable(maxValue)) {
                if (maxValue.contains(".")) {
                    this.maxValue = (int)Float.parseFloat(maxValue);
                } else if (maxValue.startsWith("0X")) {
                    BigInteger bigIntValue = new BigInteger(maxValue.substring(2), 16);
                    if (bigIntValue.compareTo(new BigInteger(String.valueOf(Integer.MAX_VALUE))) < 0) {
                        this.maxValue = bigIntValue.intValue();
                    } else {
                        this.maxValue = Integer.MAX_VALUE;
                    }
                }else {
                    this.maxValue = Integer.parseInt(maxValue);
                }
            } else {
                this.maxValue = Integer.MAX_VALUE;
            }
        }
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefinedIn() {
        return definedIn;
    }

    public void setDefinedIn(String definedIn) {
        this.definedIn = definedIn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "VMOption [ " +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", prefix='" + prefix + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", range='" + range + '\'' +
                ", component='" + component + '\'' +
                ", availability='" + availability + '\'' +
                ", definedIn='" + definedIn + '\'' +
                ']';
    }
}
