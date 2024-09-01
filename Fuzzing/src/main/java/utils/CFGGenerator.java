package utils;

import config.ExecutionPlatform;
import soot.*;
import soot.toolkits.graph.DirectedGraph;
import soot.util.cfgcmd.CFGGraphType;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

public class CFGGenerator {

    public static CFGGraphType graphtype = CFGGraphType.getGraphType("ZonedBlockGraph");
    public static CFGToDotGraph drawer = new CFGToDotGraph();

    public static void printCFGPng(Body body) {

        DirectedGraph<Unit> graph = graphtype.buildGraph(body);
        DotGraph canvas = graphtype.drawGraph(drawer, graph, body);

        String methodname = body.getMethod().getSubSignature().replace(" ","");
        String classname = body.getMethod().getDeclaringClass().getName().replaceAll("\\$", "\\.");
        String filename = SourceLocator.v().getOutputDir();
        if (filename.length() > 0) {
            filename = filename + File.separator;
        }

        filename = filename + classname + methodname.replace(File.separatorChar, '.') + DotGraph.DOT_EXTENSION;
        System.out.println(filename);
        canvas.plot(filename);

        GraphViz.convertDotFileToPng(new File(filename));
    }

    public static void printCFG(SootClass sootClass, File outDir){

        String defaultGraph = "ZonedBlockGraph";
        graphtype = CFGGraphType.getGraphType(defaultGraph);

        List<SootMethod> methods = sootClass.getMethods();

        try {
            for (SootMethod method : methods) {
                if (!method.isConstructor()){
                    System.out.println("Convert " + method.getName() + " to dot file...");
                    Body methodBody = method.retrieveActiveBody();
                    print_cfg(methodBody, outDir);
                }
            }
        } catch (Exception e) {
            //do nothing
//            e.printStackTrace();
        }
        File[] dotFiles = outDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile() && pathname.getName().endsWith(".dot")){ return true; }
                return false;
            }
        });

        for (File dotFile : dotFiles) {
            GraphViz.convertDotFileToPng(dotFile);
        }
    }

    public static void printCFG(SootClass sootClass){

        String defaultGraph = "ZonedBlockGraph";
        graphtype = CFGGraphType.getGraphType(defaultGraph);

        for (SootMethod method : sootClass.getMethods()) {
            if (!method.isConstructor()){

                System.out.println("Convert " + method.getName() + " to dot file...");
                Body methodBody = method.retrieveActiveBody();
                print_cfg(methodBody);
            }
        }
        //covert dot file to png
        File sootOutput = new File("./sootOutput");

        File[] dotFiles = sootOutput.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile() && pathname.getName().endsWith(".dot")){ return true; }
                return false;
            }
        });

        for (File dotFile : dotFiles) {
            GraphViz.convertDotFileToPng(dotFile);
        }
    }

    public static void main(String[] args) {

        String mainclass = "templates.Paper";

//        String defaultGraph = "BriefUnitGraph";
        String defaultGraph = "ZonedBlockGraph";
        graphtype = CFGGraphType.getGraphType(defaultGraph);

        String claspath = System.getProperty("java.class.path");
        Scene.v().setSootClassPath(claspath);
        Scene.v().loadNecessaryClasses();

        SootClass sootClass = Scene.v().forceResolve(mainclass, SootClass.BODIES);

        for (SootMethod method : sootClass.getMethods()) {
            if (!method.isConstructor()){

                System.out.println("Convert " + method.getName() + " to dot file...");
                Body methodBody = method.retrieveActiveBody();
                print_cfg(methodBody);
            }
        }
        //covert dot file to png
        File sootOutput = new File("./sootOutput");

        File[] dotFiles = sootOutput.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile() && pathname.getName().endsWith(".dot")){ return true; }
                return false;
            }
        });

        for (File dotFile : dotFiles) {
            GraphViz.convertDotFileToPng(dotFile);
        }
    }

    public static void print_cfg(Body body, File outDir) {

        DirectedGraph<Unit> graph = graphtype.buildGraph(body);
        DotGraph canvas = graphtype.drawGraph(drawer, graph, body);

        String methodname = body.getMethod().getSubSignature().replace(" ","");
        String classname = body.getMethod().getDeclaringClass().getName().replaceAll("\\$", "\\.");

        String outPath = outDir.getAbsolutePath() + ExecutionPlatform.FILE_SEPARATOR + classname + methodname.replace(File.separatorChar, '.') + DotGraph.DOT_EXTENSION;

        System.out.println(outPath);
        canvas.plot(outPath);
    }

    public static void print_cfg(Body body) {

        DirectedGraph<Unit> graph = graphtype.buildGraph(body);
        DotGraph canvas = graphtype.drawGraph(drawer, graph, body);

        String methodname = body.getMethod().getSubSignature().replace(" ","");
        String classname = body.getMethod().getDeclaringClass().getName().replaceAll("\\$", "\\.");
        String filename = SourceLocator.v().getOutputDir();
        if (filename.length() > 0) {
            filename = filename + File.separator;
        }

        filename = filename + classname + methodname.replace(File.separatorChar, '.') + DotGraph.DOT_EXTENSION;
        System.out.println(filename);
        canvas.plot(filename);
    }
}