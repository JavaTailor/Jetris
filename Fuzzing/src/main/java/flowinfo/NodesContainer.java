package flowinfo;

import soot.SootMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodesContainer {
    public static List<NodeSequence> nodeSequences = new ArrayList<>();
    public static Map<String, ClassNodeMap> classNodeMaps = new HashMap<>();
    public static void addNodeSequence(NodeSequence ns) {
        nodeSequences.add(ns);
    }
    public static void addAllNodeSequences(List<NodeSequence> nss) {
        nodeSequences.addAll(nss);
    }
    public static void addClassNode(String className, ClassNodeMap cnm) {
        classNodeMaps.put(className, cnm);
    }
}
