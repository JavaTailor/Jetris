package execute.loader;

import config.ExecutionConfig;
import config.ExecutionPlatform;
import execute.JvmInfo;
import execute.BenchmarkInfo;
import execute.executor.ExecutorHelper;
import execute.executor.RunnalbeClassExecutor;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Loader {

    protected ArrayList<JvmInfo> JVMCMDS;
    protected BenchmarkInfo BENCHMARK;

    /**
     * load java cmd of different JVM versions according to the OS type
     * @return
     */
    public ArrayList<JvmInfo> loadJvms(){

        if (JVMCMDS != null){
            return JVMCMDS;
        }
        JVMCMDS = new ArrayList<>();
        for (String testedJvmHome : ExecutionConfig.TESTED_JVMS) {

//            System.out.println(testedJvmHome);
            String[] testInfo = testedJvmHome.split("=");
            String vm_version = testInfo[0].trim();
            String jdkHome = testInfo[1].trim();
            String[] jvmStr = vm_version.split("@");
            String jvmVersion = jvmStr[0].trim();
            String jvmImpl = jvmStr[1].trim();

            JvmInfo jvmInfo = LoaderHelper.loadJavaCmd(jdkHome, jvmImpl, jvmVersion);
            if (jvmImpl != null) {
                JVMCMDS.add(jvmInfo);
            }
        }
        if (JVMCMDS == null || JVMCMDS.size() <= 0){
            throw new RuntimeException("There is no java cmd available!");
        }
        return JVMCMDS;
    }

    public BenchmarkInfo loadBenchmark() {

        File bench = new File(ExecutionConfig.TESTED_BENCHMARK_HOME + ExecutionPlatform.FILE_SEPARATOR + ExecutionConfig.TESTED_BENCHMARK);
        if (bench.exists()) {
            BenchmarkInfo currentProject = LoaderHelper.analysisProject(bench);
            String predefinedClassPath = bench.getAbsolutePath() + ExecutionPlatform.FILE_SEPARATOR + "testcases.txt";
            File predefinedClassFile = new File(predefinedClassPath);
            if (predefinedClassFile.exists()){
                currentProject.setPredefinedClassPath(predefinedClassPath);
            }
            BENCHMARK = setupRunnableClasses(currentProject, null);
        } else{
            throw new RuntimeException("Project-depens directory: " + bench.getAbsolutePath() + " not exists!");
        }
        return BENCHMARK;
    }

    /**
     * initial test projects form PROJECT_DEPENS_ROOT with boot jvms
     * @param bootJvms
     * @return
     */
    public BenchmarkInfo loadBenchmark(ArrayList<JvmInfo> bootJvms){

        JvmInfo bootJvm = null;
        if (bootJvms != null && bootJvms.size() > 0){
            bootJvm = LoaderHelper.getBootJvm(bootJvms);
        }

        File bench = new File(ExecutionConfig.TESTED_BENCHMARK_HOME + ExecutionPlatform.FILE_SEPARATOR + ExecutionConfig.TESTED_BENCHMARK);
        if (bench.exists()) {
            BenchmarkInfo currentProject = LoaderHelper.analysisProject(bench);

            String predefinedClassPath = bench.getAbsolutePath() + ExecutionPlatform.FILE_SEPARATOR + "testcases.txt";
            File predefinedClassFile = new File(predefinedClassPath);
            if (predefinedClassFile.exists()){
                currentProject.setPredefinedClassPath(predefinedClassPath);
            }
            BENCHMARK = setupRunnableClasses(currentProject, bootJvm);
        } else{
            throw new RuntimeException("Project-depens directory: " + bench.getAbsolutePath() + " not exists!");
        }
        return BENCHMARK;
    }

    public static BenchmarkInfo setupRunnableClasses(BenchmarkInfo currentProject, JvmInfo bootJVM){

        String className = ExecutionPlatform.RUNNABLE_CLASS_LOADER;
        String classPath = ExecutionPlatform.getJavaClassPath();
        ArrayList<String> classPathArray = new ArrayList<>();
        classPathArray.add(classPath);
        String libPath = currentProject.getLibPath();

        if (libPath != null){
            libPath = libPath + ExecutionPlatform.FILE_SEPARATOR + "*";
            classPathArray.add(libPath);
        }

        ArrayList<String> junitClasses = new ArrayList<>();
        ArrayList<String> applicationClasses = new ArrayList<>();

        if (currentProject.getSrcClassPath() != null){
            classPathArray.add(currentProject.getSrcClassPath());
        }
        if (currentProject.getTestClassPath() != null){
            classPathArray.add(currentProject.getTestClassPath());
        }
        if (currentProject.getSrcClassPath() == null &&
                currentProject.getTestClassPath() == null &&
                currentProject.getBenchmarkRootPath() != null){
            classPathArray.add(currentProject.getBenchmarkRootPath());
        }

        classPath = StringUtils.join(classPathArray, ExecutionPlatform.PATH_SEPARATOR);

        if (currentProject.getSrcClassPath() != null){

            HashMap<String, ArrayList<String>> classes = getRunnableClasses(bootJVM, currentProject, classPath, className, currentProject.getSrcClassPath());
            junitClasses.addAll(classes.get("Junit"));
            applicationClasses.addAll(classes.get("Application"));
        }

        if (currentProject.getTestClassPath() != null){

            HashMap<String, ArrayList<String>> classes = getRunnableClasses(bootJVM, currentProject, classPath, className, currentProject.getTestClassPath());
            junitClasses.addAll(classes.get("Junit"));
            applicationClasses.addAll(classes.get("Application"));
        }

        if (currentProject.getSrcClassPath() == null &&
                currentProject.getTestClassPath() == null &&
                currentProject.getBenchmarkRootPath() != null){

            HashMap<String, ArrayList<String>> classes = getRunnableClasses(bootJVM, currentProject, classPath, className, currentProject.getBenchmarkRootPath());
            junitClasses.addAll(classes.get("Junit"));
            applicationClasses.addAll(classes.get("Application"));
        }
        currentProject.setJunitClasses(junitClasses);
        currentProject.setApplicationClasses(applicationClasses);
        return currentProject;
    }

    public static HashMap<String, ArrayList<String>> getRunnableClasses(JvmInfo bootJVM, BenchmarkInfo currentProject, String classPath, String className, String calssDependsPath){

        HashMap<String, ArrayList<String>> classes = new HashMap<>();
        ArrayList<String> junitClasses = new ArrayList<>();
        ArrayList<String> applicationClasses = new ArrayList<>();
        String cmd;
        if (bootJVM != null){
            cmd = ExecutorHelper.assembleJavaCmd(bootJVM.getJavaCmd(),
                    currentProject.getVmoptions(),
                    classPath,
                    className,
                    false,
                    calssDependsPath,
                    currentProject.getBenchmarkName(),
                    currentProject.getPredefinedClassPath());
        } else {

            cmd = ExecutorHelper.assembleJavaCmd(ExecutionPlatform.JAVA_CMD,
                    currentProject.getVmoptions(),
                    classPath,
                    className,
                    false,
                    calssDependsPath,
                    currentProject.getBenchmarkName(),
                    currentProject.getPredefinedClassPath());
        }
        HashMap<String, ArrayList<String>> runnableClasses = RunnalbeClassExecutor.getInstance().execute(cmd);
        runnableClasses.keySet().forEach(type -> {
            if (type.equals("Junit")){
                junitClasses.addAll(runnableClasses.get(type));
            } else {
                applicationClasses.addAll(runnableClasses.get(type));
            }
        });
        classes.put("Junit", junitClasses);
        classes.put("Application", applicationClasses);
        return classes;
    }

    public ArrayList<JvmInfo> getJVMCMDS() {
        return JVMCMDS;
    }

    public void setJVMCMDS(ArrayList<JvmInfo> JVMCMDS) {
        this.JVMCMDS = JVMCMDS;
    }

    public BenchmarkInfo getBENCHMARK() {
        return BENCHMARK;
    }

    public void setBENCHMARK(BenchmarkInfo BENCHMARK) {
        this.BENCHMARK = BENCHMARK;
    }
}
