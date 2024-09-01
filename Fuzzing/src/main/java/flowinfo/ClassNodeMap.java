package flowinfo;

import java.util.Deque;
import java.util.HashMap;

public class ClassNodeMap {

    protected String className;
    protected HashMap<String, NodeSequence> methodsNodeMap;

    public ClassNodeMap(String className) {
        this.className = className;
        methodsNodeMap = new HashMap<>();
    }

    public ClassNodeMap(String className, HashMap<String, NodeSequence> methodsNodeMap) {
        this.className = className;
        this.methodsNodeMap = methodsNodeMap;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void addNodeSequence(String methodName, NodeSequence nodeSequence){
        this.methodsNodeMap.put(methodName, nodeSequence);
    }

    public HashMap<String, NodeSequence> getMethodsNodeMap() {
        return methodsNodeMap;
    }

    public void setMethodsNodeMap(HashMap<String, NodeSequence> methodsNodeMap) {
        this.methodsNodeMap = methodsNodeMap;
    }
}
