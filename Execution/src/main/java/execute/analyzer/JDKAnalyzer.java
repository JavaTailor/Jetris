package execute.analyzer;

import execute.executor.JIT.JvmOutput;
import java.util.*;

public class JDKAnalyzer extends Analyzer{

    public static JDKAnalyzer jdkAnalyzer;

    public static int RESULT_LEVEL1 = -1;
    public static int RESULT_LEVEL2 = 0;
    public static int RESULT_LEVEL3 = 1;

    public boolean discardFlag = false;
    public int resultState = 0;

    public static JDKAnalyzer getInstance(){

        if (jdkAnalyzer == null){
            jdkAnalyzer = new JDKAnalyzer();
        }
        return jdkAnalyzer;
    }

    /**
     * Return value state:
     *          -1  Exception and consistent
     *           0  Normal execute without exception
     *           1  Difference found (both exception and normal execute)
     * @param results
     * @return
     */
    @Override
    public DiffCore analysis(String className, HashMap<String, JvmOutput> results){

        FilterChain filterChain = new FilterChain();

        filterChain.addFilter(new JunitFilter());
        filterChain.addFilter(new StdErrFilter());
        filterChain.addFilter(new DefErrFilter());

        filterChain.startFilter(results);

        return checkIfOutputConsistentWithDiffCore(results);
    }

    public boolean getDiscardFlag() {
        return discardFlag;
    }

    public int getResultState() {
        return resultState;
    }

    public static boolean exitValueConsistent(HashMap<String, JvmOutput> results){

        int normalExecCount = 0;
        for (Map.Entry<String, JvmOutput> result : results.entrySet()) {
            if (result.getValue().getExitValue() == 0){
                normalExecCount++;
            }
        }
        if (normalExecCount < results.keySet().size()){
            return false;
        } else if (normalExecCount == results.keySet().size()){
            return true;
        } else {
            throw new RuntimeException("This should not happen: normalExecCount (" +
                    normalExecCount +
                    ") is greater than results size (" +
                    results.keySet().size() +
                    ")");
        }
    }

    public DiffCore checkIfOutputConsistentWithDiffCore(HashMap<String, JvmOutput> results){

        DiffCore diffCore = null;
        discardFlag = false;
        if (!exitValueConsistent(results)){

            discardFlag = true;
            //Error, Exception, Failure
            ArrayList<String> keys = new ArrayList<>(results.keySet());
            for (int i = 0; i < keys.size(); i++) {

                JvmOutput object1 = results.get(keys.get(i));
                for (int j = i + 1; j < keys.size(); j++){

                    JvmOutput object2 = results.get(keys.get(j));

                    //01 Errors
                    Set<String> object1Set = new HashSet<>(object1.getErrors());
                    Set<String> object2Set = new HashSet<>(object2.getErrors());
                    if (object1Set.size() != object2Set.size()){
                        resultState = RESULT_LEVEL3;
                        diffCore = new DiffCore(0,discardFlag,"Error Size Inconsistent");
                        return diffCore;
                    }

                    //02 Exceptions
                    object1Set = new HashSet<>(object1.getExceptions());
                    object2Set = new HashSet<>(object2.getExceptions());
                    if (object1Set.size() != object2Set.size()){
                        resultState = RESULT_LEVEL3;
                        diffCore = new DiffCore(1,discardFlag,"Exception Inconsistent");
                        return diffCore;
                    }

                    //03 Failures
                    object1Set = new HashSet<>(object1.getFailures());
                    object2Set = new HashSet<>(object2.getFailures());
                    if (object1Set.size() != object2Set.size()){
                        resultState = RESULT_LEVEL3;
                        diffCore = new DiffCore(2,discardFlag,"Failure Inconsistent");
                        return diffCore;
                    }
                }
            }
            resultState = RESULT_LEVEL1;
        } else {

            //Normal
            ArrayList<String> keys = new ArrayList<>(results.keySet());
            for (int i = 0; i < keys.size(); i++) {

                JvmOutput object1 = results.get(keys.get(i));
                for (int j = i + 1; j < keys.size(); j++){

                    JvmOutput object2 = results.get(keys.get(j));

                    if (!object1.getStdout().equals(object2.getStdout())){

                        discardFlag = true;
                        resultState = RESULT_LEVEL3;
                        diffCore = new DiffCore(3,discardFlag,"Normal Output Inconsistent");
                        return diffCore;
                    }
                }
            }
            resultState = RESULT_LEVEL2;
        }
        return null;
    }
}
