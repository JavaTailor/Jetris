package utils;

import core.SeedInfo;
import config.ExecutionPlatform;
import execute.BenchmarkInfo;
import org.apache.commons.cli.*;
import soot.Printer;
import soot.SootClass;
import soot.baf.BafASMBackend;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class MainHelper {

    public static Options options = null;

    public static void loadOptions(){

        options = new Options();
        options.addOption(new Option("t", "timestamp", true, "time stamp for saving results"));
        options.addOption(new Option("p", "project", true, "test project, seed programs"));
        options.addOption(new Option("s", "randomSeed", true, "random seed, default: 1"));
        options.addOption(new Option("f", "filepath", true, "set result file path"));
        options.addOption(new Option("c", "replayClassName", true, "set reply class name"));
        options.addOption(new Option("h", "help", false, "print all system settings"));
    }

    public static void restoreBadClasses(List<String> badClasses, BenchmarkInfo originProject, BenchmarkInfo targetProject){

        String originAppOutPath = originProject.getSrcClassPath();
        String targetAppOutPath = targetProject.getSrcClassPath();
        String originTestOutPath = originProject.getTestClassPath();
        String targetTestOutPath = targetProject.getTestClassPath();

        for (String badClass : badClasses) {

            String cpath = badClass.replace(".", ExecutionPlatform.FILE_SEPARATOR) + ".class";

            if (originProject.getApplicationClasses().contains(badClass)){

                String sourceFilePath = originAppOutPath + ExecutionPlatform.FILE_SEPARATOR + cpath;
                String targetFilePath = targetAppOutPath + ExecutionPlatform.FILE_SEPARATOR + cpath;
                copyToFile(sourceFilePath, targetFilePath);
            }
            if (originProject.getJunitClasses().contains(badClass)){

                String sourceFilePath = originTestOutPath + ExecutionPlatform.FILE_SEPARATOR + cpath;
                String targetFilePath = targetTestOutPath + ExecutionPlatform.FILE_SEPARATOR + cpath;
                copyToFile(sourceFilePath, targetFilePath);
            }
        }
    }

    public static CommandLine parseArgs(String[] args){

        if (options == null){
            loadOptions();
        }
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (cmd == null){

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "JITFuzzing Commands", options );
            System.out.println();
            return null;
        }
        return cmd;
    }

    public static void createFolderIfNotExist(String folderPath){
        File folder = new File(folderPath);
        if (!folder.exists()){
            folder.mkdirs();
        }
    }

    public static void saveSootClassToTargetPath(SootClass seedClass, String path) {

        try{
            OutputStream jimpleStreamOut = new FileOutputStream(path.replace(".class", ".jimple"));
            PrintWriter jimpleWriterOut = new PrintWriter(new OutputStreamWriter(jimpleStreamOut));
            Printer.v().printTo(seedClass, jimpleWriterOut);
            jimpleStreamOut.flush();
            jimpleWriterOut.flush();
            jimpleStreamOut.close();

            OutputStream classStreamOut = new FileOutputStream(path);
            BafASMBackend backend = new BafASMBackend(seedClass, soot.options.Options.v().java_version());
            backend.generateClassFile(classStreamOut);
            classStreamOut.flush();
            classStreamOut.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static List<SeedInfo> initialSeeds(List<String> classes, String srcClassPath) {

        List<SeedInfo> classInfos = new ArrayList<>();
        classes.forEach(clazz -> {
            String cpath = srcClassPath + ExecutionPlatform.FILE_SEPARATOR +
                    clazz.replace(".", ExecutionPlatform.FILE_SEPARATOR) + ".class";
            classInfos.add(new SeedInfo(clazz, cpath, clazz, cpath, 0, 0));
        });
        return classInfos;
    }

    public static List<SeedInfo> initialSeedsWithType(List<String> classes, String srcClassPath, boolean isJunit, String bakPath) {

        List<SeedInfo> classInfos = new ArrayList<>();
        classes.forEach(clazz -> {

            String cpath = srcClassPath + ExecutionPlatform.FILE_SEPARATOR +
                    clazz.replace(".", ExecutionPlatform.FILE_SEPARATOR) + ".class";
            if (!bakPath.equals("")){

                String classFileFolder = bakPath + ExecutionPlatform.FILE_SEPARATOR + clazz;
                MainHelper.createFolderIfNotExist(classFileFolder);
                String originClassBakPath = classFileFolder + ExecutionPlatform.FILE_SEPARATOR + clazz + "-origin.class";
                MainHelper.copyToFile(cpath, originClassBakPath);
                classInfos.add(new SeedInfo(clazz, cpath, clazz, originClassBakPath , isJunit, 0, 0));
            } else {
                classInfos.add(new SeedInfo(clazz, cpath, clazz, cpath, isJunit, 0, 0));
            }
        });
        return classInfos;
    }

    public static void copyToFile(String sourceFilePath, String targetFilePath){

        try {
            File sourceFile = new File(sourceFilePath);
            if (sourceFile.exists()){
                File targetFile = new File(targetFilePath);
                Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
