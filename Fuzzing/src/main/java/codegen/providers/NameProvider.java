package codegen.providers;

import soot.Local;
import soot.util.Chain;

public class NameProvider {

    public static long globalId = 0;

    public static String genVarName() {
        return "var_" + globalId++;
    }

    public static String genFuncName() {
        return "func_" + globalId++;
    }

    public static String genFieldName() {return "field_" + globalId++; }

    public static String genUniqueVarName(String targetName, Chain<Local> locals){
        for (Local local : locals) {
            if(local.getName().contains(targetName)){
                targetName = targetName + "_N";
            }
        }
        return targetName;
    }
}
