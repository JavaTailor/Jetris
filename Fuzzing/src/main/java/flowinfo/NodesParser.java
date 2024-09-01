package flowinfo;

import soot.SootClass;
import soot.SootMethod;
import utils.CFGGenerator;

import java.util.List;

public class NodesParser {

    public static ClassNodeMap parseClass(SootClass sootClass) {

        ClassNodeMap classNodeMap = new ClassNodeMap(sootClass.getName());
        List<SootMethod> sootMethods = sootClass.getMethods();
        for (SootMethod sootMethod : sootMethods) {

            if (!sootMethod.isConstructor()
                    && !sootMethod.isStaticInitializer()
                    && !sootMethod.isNative()
                    && !sootMethod.isAbstract()) {

                FlowExtraction.initialize(sootMethod.retrieveActiveBody());
                NodeSequence nodeSequence = FlowExtraction.abstractCurrentMethod();
                NodesContainer.addNodeSequence(nodeSequence);
                classNodeMap.addNodeSequence(sootMethod.getName(), nodeSequence);
            }
        }
        return classNodeMap;
    }
    public static void parseMethod(SootMethod sootMethod) {

        FlowExtraction.initialize(sootMethod.retrieveActiveBody());
        NodeSequence nodeSequence = FlowExtraction.abstractCurrentMethod();
        NodesContainer.addNodeSequence(nodeSequence);
    }
}
