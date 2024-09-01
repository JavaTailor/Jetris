package execute.executor;

import config.ExecutionConfig;
import config.ExecutionGlobal;
import config.ExecutionPlatform;
import execute.analyzer.DiffCore;
import execute.executor.JIT.JvmOutput;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class ExecutorHelper {

    public static JvmOutput getJvmOutput(Process process) throws IOException {

        final String[] stdoutBuffer = {""};
        String stderrBuffer = "";

        try {
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();

            Thread inputThread = new Thread(() -> {

                BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
                try {
                    String line = null ;
                    while ((line = inputReader.readLine()) !=  null ){
                        stdoutBuffer[0] = stdoutBuffer[0] + line + "\n";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{

                    try {
                        inputStream.close();
                        inputReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            inputThread.start();
            inputThread.join();

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
            try {
                String line = null;
                while ((line = errorReader.readLine()) != null) {
                    stderrBuffer = stderrBuffer + line + "\n";
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    errorStream.close();
                    errorReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            process.waitFor();
            process.destroy();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new JvmOutput(stdoutBuffer[0], stderrBuffer, process.exitValue());
    }

    /**
     * assemble the given param to a runnable java cmd
     * @param javaCmd   path to java
     * @param vmoptions jvm vm options
     * @param classpath classpath (jar + jvm classpath + project path)
     * @param classname class name
     * @param isJunit   if current class is a junit class, use junit cmd
     * @param args  ars
     * @return
     */
    public static String assembleJavaCmd(String javaCmd, ArrayList<String> vmoptions, String classpath, String classname, boolean isJunit, String... args){

        String argString = "";
        String optString = "";
        if (args.length > 0){
            argString = StringUtils.join(Arrays.asList(args), " ");
        }
        if (vmoptions != null && vmoptions.size() > 0){
            optString = StringUtils.join(vmoptions, " ");
        }
        String timeout = "";
        //timeout command
        if (ExecutionPlatform.isMac()) {
            timeout = "gtimeout " + ExecutionConfig.CLASS_MAX_RUNTIME + " ";
        } else if (ExecutionPlatform.isLinux()) {
            timeout = "timeout " + ExecutionConfig.CLASS_MAX_RUNTIME + " ";
        } else {
            //windows
        }
        if (isJunit){
            return timeout + ExecutionPlatform.JUNIT_CMD.replace("JAVACMD", javaCmd)
                    .replace("VMOPTIONS", optString)
                    .replace("CLASSPATH", classpath)
                    .replace("CLASSNAME", classname)
                    .replace("ARGS", argString);
        }else{
            return timeout + ExecutionPlatform.APPLICATION_CMD.replace("JAVACMD", javaCmd)
                    .replace("VMOPTIONS", optString)
                    .replace("CLASSPATH", classpath)
                    .replace("CLASSNAME", classname)
                    .replace("ARGS", argString);
        }
    }

    public static void logJvmOutput(Logger logger, String projName, String className, DiffCore diff, HashMap<String, JvmOutput> results){

        if (logger != null){

            logger.info("Difference found: project-" + projName + "-class-" + className);
            logger.info(diff.getDetailedMessage());
            for (String s : results.keySet()) {

                logger.info(String.join("", Collections.nCopies(50,"=")) +
                        s + String.join("", Collections.nCopies(50,"=")));
                logger.info(String.valueOf(diff.getDiffMessage() + ":" + results.get(s).getFEEInfo()));
                logger.info(String.valueOf(results.get(s)));
            }
        } else {

            System.err.println("Difference found: project-" + projName + "@class-" + className);
            System.err.println(diff.getDetailedMessage());
            for (String s : results.keySet()) {

                System.err.println(String.join("", Collections.nCopies(50,"=")) +
                        s + String.join("", Collections.nCopies(50,"=")));
                System.err.println(String.valueOf(diff.getDiffMessage() + ":" + results.get(s).getFEEInfo()));
                System.err.println(results.get(s));
            }
        }
    }

    public static void logJvmOutput(String projName, String className, DiffCore diff, HashMap<String, JvmOutput> results){

        if (ExecutionGlobal.getDiffLogger() != null){

            ExecutionGlobal.getDiffLogger().info("Difference found: project-" + projName + "-class-" + className);
            ExecutionGlobal.getDiffLogger().info(diff.getDetailedMessage());
            for (String s : results.keySet()) {

                ExecutionGlobal.getDiffLogger().info(String.join("", Collections.nCopies(50,"=")) +
                        s + String.join("", Collections.nCopies(50,"=")));
                ExecutionGlobal.getDiffLogger().info(String.valueOf(diff.getDiffMessage() + ":" + results.get(s).getFEEInfo()));
                ExecutionGlobal.getDiffLogger().info(String.valueOf(results.get(s)));
            }
        } else {

            System.err.println("Difference found: project-" + projName + "@class-" + className);
            System.err.println(diff.getDetailedMessage());
            for (String s : results.keySet()) {

                System.err.println(String.join("", Collections.nCopies(50,"=")) +
                        s + String.join("", Collections.nCopies(50,"=")));
                System.err.println(String.valueOf(diff.getDiffMessage() + ":" + results.get(s).getFEEInfo()));
                System.err.println(results.get(s));
            }
        }
    }
}
