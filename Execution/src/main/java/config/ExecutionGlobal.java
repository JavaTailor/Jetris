package config;

import util.LoggerUtils;

import java.util.logging.Logger;

public class ExecutionGlobal {
    private static Logger logger;
    private static Logger diffLogger;
    private static Logger dataLogger;
    public static int debugInfo;

    public static void setLogger(String timeStamp){

        if (logger == null){
            logger = LoggerUtils.getInstance(timeStamp);
        }
    }

    public static void setDiffLogger(String timeStamp, String filename){

        if (diffLogger == null){
            diffLogger = LoggerUtils.getInstance(timeStamp, filename, true);
        }
    }

    public static void setDataLogger(String timeStamp, String filename){

        if (dataLogger == null){
            dataLogger = LoggerUtils.getInstance(timeStamp, filename, true);
        }
    }

    public static Logger getDiffLogger() {
        return diffLogger;
    }

    public static Logger getDataLogger() {
        return dataLogger;
    }

}
