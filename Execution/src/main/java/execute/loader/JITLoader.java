package execute.loader;

import java.io.File;
import java.util.*;

import config.ExecutionPlatform;
import execute.JvmInfo;
import execute.BenchmarkInfo;

public class JITLoader extends Loader{

    private static JITLoader jitloader;
    private boolean usePredefinedClasses = true;

    public static JITLoader getInstance(){

        if (jitloader == null){
            jitloader = new JITLoader();
        }
        return jitloader;
    }

    /**
     * load java cmd of different JVM versions according to the OS type
     * @return
     */
    @Override
    public ArrayList<JvmInfo> loadJvms() {

        super.loadJvms();
        return JVMCMDS;
    }

    @Override
    public BenchmarkInfo loadBenchmark() {

        super.loadBenchmark();
        return BENCHMARK;
    }

    @Override
    public BenchmarkInfo loadBenchmark(ArrayList<JvmInfo> bootJvms) {

        super.loadBenchmark(bootJvms);
        return BENCHMARK;
    }

    public void enablePredefinedClasses(){
        this.usePredefinedClasses = true;
    }

    public void disablePredefinedClasses(){
        this.usePredefinedClasses = false;
    }

    public BenchmarkInfo loadBenchmarkWithGivenPath(String benchDir, String benchName, ArrayList<JvmInfo> bootJvms){

        JvmInfo bootJvm = null;
        if (bootJvms != null && bootJvms.size() > 0){
            bootJvm = LoaderHelper.getBootJvm(bootJvms);
        }

        String benchPath = benchDir + ExecutionPlatform.FILE_SEPARATOR + benchName;
        File projectFile = new File(benchPath);
        BenchmarkInfo currentProject;
        if (projectFile.exists()){

            currentProject = LoaderHelper.analysisProject(projectFile);
            if (usePredefinedClasses) {
                String predefinedClassPath = benchPath + ExecutionPlatform.FILE_SEPARATOR + "testcases.txt";
                File predefinedClassFile = new File(predefinedClassPath);
                if (predefinedClassFile.exists()) {
                    currentProject.setPredefinedClassPath(predefinedClassPath);
                }
            }
        } else {
            throw new RuntimeException("loadBenchmarkWithGivenPath - Benchmark " + benchPath + " not exist!");
        }

        return setupRunnableClasses(currentProject, bootJvm);
    }
}
