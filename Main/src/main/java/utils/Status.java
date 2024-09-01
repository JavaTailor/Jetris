package utils;

import config.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

public class Status {
    public static int initialSeed = 2;
    public static long currentSeed = 0;
    public static boolean useVMOptions = true;
    public static String timeStamp;
    public static String defineClassesPath = "testcases.txt";
    public static String mutationHistoryPath;
    public static String diffClassPath;
    public static String propertiesPath = "./Jetris.properties";

    public static void printGlobalStatus() {

        if (ExecutionGlobal.getDataLogger() == null) {
            return;
        }
        String title = "Jetris (2.0). " + new Date() + "(" + System.currentTimeMillis() + ")";
        ExecutionGlobal.getDataLogger().info(String.join("", Collections.nCopies(20,"#")) +
                title + String.join("", Collections.nCopies(20,"#")));
        ExecutionGlobal.getDataLogger().info("Test Project: " + ExecutionConfig.TESTED_BENCHMARK);
        ExecutionGlobal.getDataLogger().info("Random Seed: " + initialSeed);
        ExecutionGlobal.getDataLogger().info("Fuzzing Round: " + FuzzingConfig.MAX_FUZZ_STEP);
        ExecutionGlobal.getDataLogger().info("Max Running Time: " + ExecutionConfig.CLASS_MAX_RUNTIME);
        ExecutionGlobal.getDataLogger().info("Generation History: " + mutationHistoryPath);
        ExecutionGlobal.getDataLogger().info("Differential History: " + diffClassPath);
        ExecutionGlobal.getDataLogger().info("XXXX: " + new RuntimeException("Print Stack Trace: ").getMessage());
        ExecutionGlobal.getDataLogger().info(String.join("", Collections.nCopies(40 + title.length(),"#")));
        
    }

    public static void updateStatus(String superClass, String className, String status) {
        if (ExecutionGlobal.getDataLogger() != null) {
            ExecutionGlobal.getDataLogger().info(new Date() + "(" + currentSeed + ")" + " "
                    + String.format("%-18s", status) + " " + superClass + " " + className);
        } else {
            System.err.println(new Date() + "(" + currentSeed + ")" + " "
                    + String.format("%-18s", status) + " " + superClass + " " + className);
        }
    }

    public static void argsParser(String[] args) {

        /**
         * 01 parse properties files
         */
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(propertiesPath)) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ParseProperties.parseFuzzingProperties(properties);
        ParseProperties.parseExecutionProperties(properties);

        /**
         * 02 parse commandline args
         */
        CommandLine options = MainHelper.parseArgs(args);
        HelpFormatter formatter = new HelpFormatter();
        if (options != null){

            if (options.hasOption("t")) {
                Status.timeStamp = options.getOptionValue("t");
            } else {
                Status.timeStamp = String.valueOf(new Date().getTime());
            }
			if (options.hasOption("s")) {
                Status.initialSeed = Integer.parseInt(options.getOptionValue("s"));
            } else {
                Status.initialSeed = (int) System.currentTimeMillis();
            }
            if (options.hasOption("p")) {
                ExecutionConfig.TESTED_BENCHMARK = options.getOptionValue("p");
            }

            if (options.hasOption("h")){
                formatter.printHelp( "JITFuzzing Commands", MainHelper.options );
                System.out.println();
            }
        }
    }
}
