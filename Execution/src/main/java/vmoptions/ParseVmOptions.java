package vmoptions;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import config.ExecutionConfig;
import config.ExecutionPlatform;
import execute.JvmInfo;
import execute.loader.JITLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ParseVmOptions {
    public static ArrayList<String> possibleOptionsROOT = new ArrayList<>();
    static {
        possibleOptionsROOT.add("./Execution/Options");
        possibleOptionsROOT.add("./Options");
    }
    /**
     *
     * @param jvmName
     * @param jdkVersion
     */
    public static VMOptions parse(String jvmName, String jdkVersion) {

        VMOptions vmOptions = new VMOptions(jvmName, jdkVersion);
        String optionsROOT = null;
        for (String path : possibleOptionsROOT) {
            if (Paths.get(path).toFile().exists()) {
                optionsROOT = path;
                break;
            }
        }
        if (optionsROOT == null) {
            vmOptions.setOptions(new ArrayList<>());
        }
        String optionPath = null;
        if (jvmName.toLowerCase().contains("hotspot")
				|| jvmName.toLowerCase().contains("bisheng")){
            if (jdkVersion.toLowerCase().contains("openjdk8")){
                optionPath = optionsROOT + ExecutionPlatform.FILE_SEPARATOR + "OpenJDK8.json";
            } else if (jdkVersion.toLowerCase().contains("openjdk11")){
                optionPath = optionsROOT + ExecutionPlatform.FILE_SEPARATOR + "OpenJDK11.json";
            } else {}
        } else if (jvmName.toLowerCase().contains("openj9")){
            optionPath = optionsROOT + ExecutionPlatform.FILE_SEPARATOR + "OpenJ9.json";
        } else {}

        if (optionPath != null && Paths.get(optionPath).toFile().exists()) {
            vmOptions.setOptions(parseOptionJsonFile(optionPath));
        }
        return vmOptions;
    }

    public static List<Option> parseOptionJsonFile(String path) {

        List<Option> optionList = new ArrayList<>();
        try {
            // 读取JSON文件内容
            String jsonContent = new String(Files.readAllBytes(Paths.get(path)));
            // 解析JSON
            JSONObject jsonObject = JSONObject.parseObject(jsonContent);
            JSONArray optionArray = jsonObject.getJSONArray("switches");


            for (int i = 0; i < optionArray.size(); i++) {
                JSONObject optionObject = optionArray.getJSONObject(i);

                Option option = new Option(
                        optionObject.getString("availability"),
                        optionObject.getString("component"),
                        optionObject.getString("defaultValue"),
                        optionObject.getString("definedIn"),
                        optionObject.getString("description"),
                        optionObject.getIntValue("id"),
                        optionObject.getString("name"),
                        optionObject.getString("prefix"),
                        optionObject.getString("range"),
                        optionObject.getString("type")
                );
                optionList.add(option);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return optionList;
    }
}
