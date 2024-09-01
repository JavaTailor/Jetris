package codegen.providers;

import config.FuzzingConfig;
import config.FuzzingRandom;
import soot.*;

import java.util.ArrayList;
import java.util.List;

// 常见异常
public class ExceptionProvider {

    public static List<RefType> exceptions = new ArrayList<>();

    public static int exceptionMsgNum = 0;
    public static int exceptionHandlerNum = 0;

    static {

        for (SootClass clazz: Scene.v().getClasses()) {
            if (clazz.isAbstract()) continue;
            if (!clazz.isPublic()) continue;
            if (!clazz.getName().endsWith("Exception")) continue;
            if(FuzzingConfig.INVALID_EXCEPTION_TYPE.contains(clazz.getName()) || !clazz.getName().startsWith("java.")) continue;
            for (SootMethod method : clazz.getMethods()) {
                if (method.isPublic() && method.isConstructor()) {
                    exceptions.add(clazz.getType());
                    break;
                }
            }
        }
    }

    public static Type anyException() {
        Type candidate = exceptions.get(FuzzingRandom.nextChoice(exceptions.size()));
        return candidate;
    }

    public static String genExceptionMSG(){
        return "Exception MSG_" + exceptionMsgNum++;
    }

    public static String genHandleException(){
        return "ExceptionHandler_" + exceptionHandlerNum++;
    }
}
