package execute.executor.JIT;

import config.ExecutionConfig;
import config.ExecutionGlobal;
import config.ExecutionPlatform;
import config.ExecutionRandom;
import execute.*;
import execute.analyzer.DiffCore;
import execute.analyzer.JDKAnalyzer;
import execute.executor.Executor;
import execute.executor.ExecutorHelper;
import vmoptions.Option;
import vmoptions.OptionWheel;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class JITExecutor extends Executor {
    private JvmOutput currentOutput;
    private Process currentProcess;
    private boolean debug = true;

    private boolean diffFound;
    private boolean disCard;

    public static JITExecutor cfmExecutor;

    private HashMap<String, JvmOutput> lastResults = new HashMap<>();

    public static JITExecutor getInstance(){

        if (cfmExecutor == null){
            cfmExecutor = new JITExecutor();
        }
        return cfmExecutor;
    }

    private long getCurrentProcessId() throws Exception {

        long pid = 0;
        if (ExecutionPlatform.isLinux() || ExecutionPlatform.isMac()) {
            Field f = currentProcess.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            pid = f.getLong(currentProcess);
            f.setAccessible(false);
        } else if (ExecutionPlatform.isWin()) {
            //TODO
        } else {
            throw new RuntimeException("UNKNOWN OS");
        }
        return pid;
    }

    @Override
    public JvmOutput execute(String cmd) {

        JvmOutput output = null;
        currentOutput = null;
        try {
            currentProcess = Runtime.getRuntime().exec(cmd);
            output = ExecutorHelper.getJvmOutput(currentProcess);
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentOutput = output;
        return output;
    }

    public void shutDown(){

        if (currentProcess != null){
            currentProcess.destroy();
            currentProcess.destroyForcibly();
        }
    }

    public boolean dtSingleClassInProj(ArrayList<JvmInfo> jvmCmds,
                                    BenchmarkInfo currentProject,
                                    String executeClassName,
                                    String logClassName) {

        diffFound = false;
        disCard = false;
        HashMap<String, JvmOutput> results = new HashMap<>();

        ArrayList<String> vmOptions = currentProject.getVmoptions();
        ArrayList<String> projOptions = currentProject.getProjoptions();
        String[] projOptionsArray = projOptions.toArray(new String[projOptions.size()]);
        String classPath = currentProject.getpClassPath();
        /**
         * 01 differential testing java application class
         */
        if (currentProject.getApplicationClasses().contains(executeClassName)){

            System.out.println("Project-application: "
                    + currentProject.getBenchmarkName()
                    + "-"
                    + logClassName
                    + "...");
            results = dtSingleClass(jvmCmds, vmOptions, classPath, currentProject.getBenchmarkName() , executeClassName, false, projOptionsArray);
        }
        /**
         * 02 differential testing junit test case
         */
        if (currentProject.getJunitClasses().contains(executeClassName)){

            System.out.println("Project-junit: "
                    + currentProject.getBenchmarkName()
                    + "-"
                    + logClassName
                    + "...");
            results = dtSingleClass(jvmCmds, vmOptions, classPath, currentProject.getBenchmarkName() ,executeClassName, true, projOptionsArray);
        }
        lastResults = results;
        DiffCore diff = JDKAnalyzer.getInstance().analysis(logClassName, results);

        if (diff != null){
            diffFound = true;
            ExecutorHelper.logJvmOutput(ExecutionGlobal.getDiffLogger(), currentProject.getBenchmarkName(), logClassName, diff ,results);
        }
        disCard = JDKAnalyzer.getInstance().getDiscardFlag();
        return disCard;
    }

    public boolean isDiffFound() {
        return diffFound;
    }

    public boolean isDisCard() {
        return disCard;
    }

    public HashMap<String, JvmOutput> dtSingleClass(ArrayList<JvmInfo> jvms,
                                                    ArrayList<String> vmOptions,
                                                    String classpath,
                                                    String projName,
                                                    String className,
                                                    boolean isJunit,
                                                    String... args) {

        HashMap<String, JvmOutput> results = new HashMap<>();

        for (JvmInfo jvm : jvms) {

            ArrayList<String> sOptions = new ArrayList<>();
            sOptions.addAll(vmOptions);
            /**
             * set vm options
             */
            if(jvm.getVmOptions() != null
                    && jvm.getVmOptions().getOptions() != null
                    && jvm.getVmOptions().getOptions().size() > 0){

                if (ExecutionConfig.DIAGNOSTIC_OPTIONS.size() > 0){
                    sOptions.addAll(ExecutionConfig.DIAGNOSTIC_OPTIONS);
                    int maxOptionSize = ExecutionRandom.nextChoice(ExecutionConfig.OPTION_MAX_SIZE);
                    if (jvm.getJvmName().toLowerCase().contains("openj9") && maxOptionSize > 0 ) {
                        maxOptionSize = ExecutionRandom.nextChoice(3);
                    }
                    ArrayList<String> components = new ArrayList<>();
                    components.add("JIT");
                    components.add("RUNTIME");
                    List<Option> options = jvm.getVmOptions().getOptionsByComponents(components, true);
//                    List<Option> options = jvm.getVmOptions().getOptionsByComponent("JIT", true);
                    if (options.size() > 0){
                        sOptions.addAll(OptionWheel.wheel(jvm.getJvmName(), options, maxOptionSize));
                    }
                }
            }

            String jvmId = jvm.getJvmId() != null ? jvm.getJvmId() : jvm.getJvmName();
            String cmdExecute = ExecutorHelper.assembleJavaCmd(jvm.getJavaCmd(), sOptions, classpath, className, isJunit, args);
            System.out.println("cmdExecute: " + cmdExecute);
            Thread ctester = new Thread(new Runnable() {
                @Override
                public void run() {
                    getInstance().execute(cmdExecute);
                }
            });
            ctester.start();
            try {
                ctester.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (currentOutput != null){
                if(currentOutput.getStdout().contains("CHECKSUM")){
                    currentOutput.setStdout(currentOutput.getStdout().substring(currentOutput.getStdout().indexOf("CHECKSUM"))+"\n");
                    currentOutput.setStdout(currentOutput.getStdout().substring(0,currentOutput.getStdout().indexOf("\n")).replace("CHECKSUM:",""));
                }
                else {
                    currentOutput.setStdout("");
                }

                results.put(jvmId + "@" + String.join(" ", sOptions), currentOutput);
            } else {
                currentOutput = new JvmOutput("JvmOutput-TIMEOUT");
                results.put(jvmId + "@" + String.join(" ", sOptions), currentOutput);
            }
            if (debug) {
                System.out.println(String.join("", Collections.nCopies(50,"=")) +
                        jvm.getJvmName() + "@" + jvm.getVersion() + String.join("", Collections.nCopies(50,"=")));
                System.out.println(currentOutput.getOutput());
            }
        }
        return results;
    }

    public JvmOutput getCurrentOutput() {
        return currentOutput;
    }

    public void enableDebugMode() {
        debug = true;
    }

    public void disableDebugMode() {
        debug = false;
    }

    public HashMap<String, JvmOutput> getLastResults() {
        return lastResults;
    }
}
