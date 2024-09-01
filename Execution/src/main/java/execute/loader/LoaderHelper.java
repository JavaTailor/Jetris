package execute.loader;

import config.ExecutionConfig;
import config.ExecutionPlatform;
import execute.*;
import org.apache.commons.lang3.StringUtils;
import util.RuntimeConfigParser;
import vmoptions.*;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

public class LoaderHelper {

    /**
     * search java cmd from a given path
     * @param jvmPath   DTConfiguration.JVM_DEPENS_ROOT + / + OSTYPE + openjdkVersion
     * @param openjdkVersion openjdkVersion
     * @return
     */
    public static JvmInfo loadJavaCmd(String jvmPath, String jvmName, String openjdkVersion){

        File javaCmd = new File(jvmPath);
        if (javaCmd.exists()){
            if (javaCmd.exists()){
                JvmInfo jvmInfo = new JvmInfo(jvmPath, javaCmd.getAbsolutePath(), jvmName, openjdkVersion, javaCmd.getAbsolutePath());
                if(ExecutionConfig.useVMOptions){
                    jvmInfo.setVmOptions(ParseVmOptions.parse(jvmInfo.getJvmName(), jvmInfo.getVersion()));
                }
                return jvmInfo;
            }
        }else {
            System.err.println("Directory: " + jvmPath + " not exists!");
        }
        return null;
    }

    /**
     * analysis target project elements
     * @param projectFile project file
     * @return ProjectInfo object
     */
    public static BenchmarkInfo analysisProject(File projectFile){

        String projectName = projectFile.getName();
        String projectRootPath = projectFile.getAbsolutePath();
        BenchmarkInfo currentProject = new BenchmarkInfo(projectName, projectRootPath);
        //TODO: analysis runtime config
        String runtimeConfigPath = projectRootPath + ExecutionPlatform.FILE_SEPARATOR + ExecutionConfig.PROJECT_RUNTIME_CONFIG;
        File rcFile = new File(runtimeConfigPath);
        if (rcFile.exists()){
            currentProject.setProjoptions(RuntimeConfigParser.parse(rcFile));
        } else {
            currentProject.setProjoptions(new ArrayList<>());
        }
        File[] contentFiles = projectFile.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                //replace with default.properties
                return pathname.isDirectory() && ExecutionConfig.PROJECTS_ELEMENTS.contains(pathname.getName());
            }
        });
        if (contentFiles.length > 0) {
            for (File contentFile : contentFiles) {
                switch (contentFile.getName()) {
                    case "lib":
                        currentProject.setLibPath(contentFile.getAbsolutePath());
                        currentProject.setLibsString(parseLibString(contentFile));
                        break;
                    case "out":
                        String oSrcClassPath = contentFile.getAbsolutePath() + ExecutionPlatform.FILE_SEPARATOR
                                + "production" + ExecutionPlatform.FILE_SEPARATOR + currentProject.getBenchmarkName();
                        File oClassFile = new File(oSrcClassPath);
                        if (oClassFile.exists()) {
                            currentProject.setSrcClassPath(oSrcClassPath);
                            currentProject.setTotalSrcClassSize(countFileSizeWithSuffix(oClassFile, ".class"));
                        }
                        String oTestClassPath = contentFile.getAbsolutePath() + ExecutionPlatform.FILE_SEPARATOR
                                + "test" + ExecutionPlatform.FILE_SEPARATOR + currentProject.getBenchmarkName();
                        File oTestFile = new File(oTestClassPath);
                        if (oTestFile.exists()) {
                            currentProject.setTestClassPath(oTestClassPath);
                            currentProject.setTotalTestClassSize(countFileSizeWithSuffix(oTestFile, ".class"));
                        }
                        break;
                    case "target":
                        //TODO target analysis
                        String tSrcClassPath = contentFile.getAbsolutePath() + ExecutionPlatform.FILE_SEPARATOR
                                + "classes";
                        File tClassFile = new File(tSrcClassPath);
                        if (tClassFile.exists()) {
                            currentProject.setSrcClassPath(tSrcClassPath);
                            currentProject.setTotalSrcClassSize(countFileSizeWithSuffix(tClassFile, ".class"));
                        }
                        String tTestClassPath = contentFile.getAbsolutePath() + ExecutionPlatform.FILE_SEPARATOR
                                + "test-classes";
                        File tTestFile = new File(tTestClassPath);
                        if (tTestFile.exists()) {
                            currentProject.setTestClassPath(tTestClassPath);
                            currentProject.setTotalTestClassSize(countFileSizeWithSuffix(tTestFile, ".class"));
                        }
                        break;
                    case "src":
                        currentProject.setSrcJavaPath(contentFile.getAbsolutePath());
                        currentProject.setTotalSrcJavaSize(countFileSizeWithSuffix(contentFile, ".java"));
                        break;
                    case "test":
                        currentProject.setTestJavaPath(contentFile.getAbsolutePath());
                        currentProject.setTotalTestJavaSize(countFileSizeWithSuffix(contentFile, ".java"));

                        break;
                    case "java":
                        if (currentProject.getSrcJavaPath() == null){
                            currentProject.setSrcJavaPath(contentFile.getAbsolutePath());
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        if (currentProject.getSrcClassPath() == null &&  currentProject.getTestClassPath() == null){

            long classNumber = countFileSizeWithSuffix(projectFile, ".class");
            if (classNumber > 0){
                currentProject.setSrcClassPath(projectRootPath);
                currentProject.setTotalSrcClassSize(classNumber);
                currentProject.setTotalTestClassSize(classNumber);
            } else {
                System.err.println("Project " + projectName + " seems to be an empty project! Please check the project root path: " + projectRootPath);
            }
        }
        return currentProject;
    }
    public static JvmInfo getBootJvm(ArrayList<JvmInfo> jvms){

        for (JvmInfo jvm : jvms) {

            if (jvm.getJvmName().equalsIgnoreCase("hotspot")){
                return jvm;
            }
        }
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        return jvms.get(random.nextInt(jvms.size()));
    }
    public static long countFileSizeWithSuffix(File currentPath, String suffix){

        long fileCounter = 0;
        Stack<File> stack = new Stack<>();
        stack.push(currentPath);

        while (!stack.isEmpty()) {
            File path = stack.pop();
            File[] classFiles = path.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isDirectory() || pathname.getName().endsWith(suffix);
                }
            });
            if (classFiles == null) {
                break;
            }
            for (File subFile : classFiles) {
                if (subFile.isDirectory()) {
                    stack.push(subFile);
                } else {
                    fileCounter++;
                }
            }
        }
        return fileCounter;
    }
    public static String parseLibString(File libFile){

        File[] libs = libFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(ExecutionPlatform.JAR_SUFFIX);
            }
        });
        if (libs.length > 0){

            String[] libsPath = new String[libs.length];
            for (int i = 0; i < libs.length; i++){
                libsPath[i] = libs[i].getAbsolutePath();
            }
            return StringUtils.join(Arrays.asList(libsPath), ExecutionPlatform.PATH_SEPARATOR);
        }else{
            return null;
        }
    }
}
