import config.ExecutionPlatform;
import execute.*;
import execute.loader.JITLoader;
import java.util.ArrayList;

public class ExecutionPreview {

    /**
     * Overview of the test platform, test jvms and test projects
     */
    public static void main(String[] args) {

        /**
         * Testing platform
         */
        System.out.println(ExecutionPlatform.getInstance());

        ArrayList<JvmInfo> jvmCmds = JITLoader.getInstance().loadJvms();
        BenchmarkInfo benchmarkInfo = JITLoader.getInstance().loadBenchmark();

        /**
         * JVM
         */
        for (JvmInfo jvmCmd : jvmCmds) {
            System.out.println(jvmCmd);
        }
        /**
         * Benchmark
         */
        System.out.println(benchmarkInfo);
    }
}
