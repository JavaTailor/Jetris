import codegen.blocks.*;
import codegen.operands.OperandGenerator;
import codegen.operators.*;
import codegen.providers.StaticMethodProvider;
import codegen.providers.TypeProvider;
import config.*;
import core.SeedInfo;
import execute.BenchmarkInfo;
import execute.JvmInfo;
import execute.executor.JIT.JITExecutor;
import execute.loader.JITLoader;
import flowinfo.AbstractNode;
import flowinfo.NodeSequence;
import flowinfo.NodesContainer;
import flowinfo.NodesParser;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.options.Options;
import utils.ChecksumHelper;
import utils.ClassUtils;
import utils.MainHelper;
import utils.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static codegen.providers.ElementsProvider.getTargets;

public class Main {

    public static List<SeedInfo> seeds;
    public static ArrayList<JvmInfo> jvmCmds;
    public static BenchmarkInfo historyProject;
    public static BenchmarkInfo originProject;
    public static BenchmarkInfo targetProject;
    public static void main(String[] args) {

        ExecutionConfig.useVMOptions = Status.useVMOptions;
        initialize(args);
        fuzzing();
    }
    public static void initialize(String[] args) {
        // load config
        Status.argsParser(args);
        // create folder for results
        ExecutionGlobal.setDiffLogger(Status.timeStamp + ExecutionPlatform.FILE_SEPARATOR + ExecutionConfig.TESTED_BENCHMARK, "diff");
        ExecutionGlobal.setDataLogger(Status.timeStamp + ExecutionPlatform.FILE_SEPARATOR + ExecutionConfig.TESTED_BENCHMARK, "status");
        Status.mutationHistoryPath = "." + ExecutionPlatform.FILE_SEPARATOR + "03results" +
                ExecutionPlatform.FILE_SEPARATOR + Status.timeStamp +
                ExecutionPlatform.FILE_SEPARATOR + "classHistory";
        Status.diffClassPath = "." + ExecutionPlatform.FILE_SEPARATOR + "03results" +
                ExecutionPlatform.FILE_SEPARATOR + Status.timeStamp +
                ExecutionPlatform.FILE_SEPARATOR + "diffClasses";
        Status.printGlobalStatus();
        /**
         * Testing platform
         */
        System.out.println(ExecutionPlatform.getInstance());
        // load JVMs
        jvmCmds = JITLoader.getInstance().loadJvms();
        // load projects
        JITLoader.getInstance().disablePredefinedClasses();
        historyProject = JITLoader.getInstance().loadBenchmarkWithGivenPath(ExecutionConfig.TESTED_BENCHMARK_HOME, "HotspotTests-Java", null);
        JITLoader.getInstance().enablePredefinedClasses();
        originProject = JITLoader.getInstance().loadBenchmarkWithGivenPath(ExecutionConfig.TESTED_BENCHMARK_HOME, ExecutionConfig.TESTED_BENCHMARK, null);
        targetProject = JITLoader.getInstance().loadBenchmarkWithGivenPath(ExecutionConfig.TESTED_SOOTOUTPUT_HOME, ExecutionConfig.TESTED_BENCHMARK, null);
        List<String> seedClasses = originProject.getApplicationClasses();
        MainHelper.restoreBadClasses(seedClasses, originProject, targetProject);
        targetProject.setApplicationClasses(originProject.getApplicationClasses());
        seeds = MainHelper.initialSeedsWithType(originProject.getApplicationClasses(), targetProject.getSrcClassPath(), false, Status.mutationHistoryPath);
        /**
         * JVM
         */
        if (jvmCmds.size() == 0) {
            throw new RuntimeException("No Available JVMs!");
        } else {
            jvmCmds.forEach(System.out::println);
        }
        /**
         * Project
         */
        System.out.println("History Project: ");
        System.out.println(historyProject);
        System.out.println("Fuzzing Projects: ");
        System.out.println(originProject);
        System.out.println(targetProject);
        // load soot
        ClassUtils.initSootEnvWithClassPath(historyProject.getpClassPath());
        ClassUtils.set_output_path(targetProject.getSrcClassPath());
        // set random seed
        RandomManager.setRandomSeed(Status.initialSeed);
        JITExecutor.getInstance().enableDebugMode();
        // initialize checksum
        initChecksumClass(originProject, targetProject);
        // initialize fuzzing
        TypeProvider.loadRefTypes();
        StaticMethodProvider.loadStaticMethods();

        // initialize node templates
        int validCount = 0;
        ArrayList<String> historyClasses = historyProject.getApplicationClasses();
        for (String historyClass : historyClasses) {

            SootClass sootClass = ClassUtils.loadClass(historyClass);
            if (sootClass != null) {
                validCount++;
                NodesParser.parseClass(sootClass);
            }
        }
        NodesContainer.nodeSequences = NodesContainer.nodeSequences.stream().filter(nodeSequence -> nodeSequence.getNodeLength() != -1
                && nodeSequence.getNodeLength() < 100).collect(Collectors.toList());

        if (targetProject.getApplicationClasses().size() <= 1) {
            ExecutionConfig.MAX_SYNTHESIS_TIME = 1000;
        }

        soot.G.reset();
        ClassUtils.initSootEnvWithClassPath(targetProject.getpClassPath());
        ClassUtils.set_output_path(targetProject.getSrcClassPath());
        // initialize fuzzing
        TypeProvider.loadRefTypes();
        StaticMethodProvider.loadStaticMethods();
    }

    public static void fuzzing() {

        while (true) {

            SeedInfo seed = seeds.get(MainRandom.nextChoice(seeds.size()));
            try {
                if (seed.isOriginClass() && !seed.hasCovered()) {
                    seeds.remove(seed);
                    seed = instrumentChecksumStmt(seed, Status.mutationHistoryPath);
                    seeds.add(seed);
                }
                String classFileFolder = Status.mutationHistoryPath + ExecutionPlatform.FILE_SEPARATOR + seed.getOriginClassName();
                ClassInfo clazz = null;
                if (seed.isOriginClass() && !seed.hasCovered()) {
                    clazz = ClassUtils.loadClassInfo(seed.getClassName());
                } else {
                    seed.storeToCoverOriginClass();
                    clazz = ClassUtils.loadClassInfo(seed.getOriginClassName());
                }
                if (clazz == null || seed.getMutationTimes() > ExecutionConfig.MAX_SYNTHESIS_TIME) {
                    seeds.remove(seed);
                    continue;
                }
                List<SootMethod> sootMethods = clazz.getSootClass().getMethods();
                for (SootMethod sootMethod : sootMethods) {
                    if (!sootMethod.isAbstract()) {
                        sootMethod.retrieveActiveBody();
                    }
                }
                Status.currentSeed = MainRandom.getRandom().nextLong();
                RandomManager.updateSeed(Status.currentSeed);
                List<SootMethod> candidates;
                if (ExecutionConfig.TESTED_BENCHMARK.equals("Templates")) {
                    candidates = sootMethods.stream().filter(m -> m.getName().contains("opt")).collect(Collectors.toList());
                } else {
                    candidates = sootMethods.stream().filter(m -> !m.isStaticInitializer() && !m.isConstructor() && !m.isAbstract()).collect(Collectors.toList());
                }
                if (candidates.size() == 0) {
                    continue;
                }
                SootMethod method = candidates.get(MainRandom.nextChoice(candidates.size()));
                List<Local> originLocals = new ArrayList<>(method.retrieveActiveBody().getLocals());
                try {
                    guidedCodeGeneration(clazz, method);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // local
                List<Local> newLocals = new ArrayList<>(method.retrieveActiveBody().getLocals());
                newLocals.removeAll(originLocals);
                // checksum
                ChecksumHelper.updateCheckSumStmtAfterLastWrite(clazz.getSootClass(), method.retrieveActiveBody(), newLocals);
                if (ClassUtils.saveClass(clazz.getSootClass(), Options.output_format_class)) {
                    seed.mutationTimesIncrease();
                    SeedInfo newClass = new SeedInfo(seed.getOriginClassName(),
                            seed.getOriginClassPath(),
                            seed.generateMutateClassFilename(),
                            classFileFolder,
                            seed.isJunit(),
                            seed.getMutationOrder() + 1,
                            0);
                    //save to history
                    newClass.saveSootClassToFile(clazz.getSootClass());
                    //successful saved
                    if (!JITExecutor.getInstance().dtSingleClassInProj(jvmCmds, targetProject, newClass.getOriginClassName(), newClass.getClassName())) {
                        seeds.add(newClass);
                        Status.updateStatus(seed.getClassName(), newClass.getClassName(), "Normal&Consistent");
                    } else {
                        if (JITExecutor.getInstance().isDiffFound()) {
                            String diffClassFolder = Status.diffClassPath + ExecutionPlatform.FILE_SEPARATOR + newClass.getOriginClassName();
                            MainHelper.createFolderIfNotExist(diffClassFolder);
                            //save to diffClasses
                            MainHelper.saveSootClassToTargetPath(clazz.getSootClass(), diffClassFolder + ExecutionPlatform.FILE_SEPARATOR + newClass.getClassName());
                            Status.updateStatus(seed.getClassName(), newClass.getClassName(), "DifferenceFound");
                        } else {
                            Status.updateStatus(seed.getClassName(), newClass.getClassName(), "Broken&Consistent");
                        }
                    }
                } else {
                    Status.updateStatus(seed.getClassName(), "GenerationFailed", "GenerationFailed");
                }
                Scene.v().removeClass(clazz.getSootClass());
            } catch (Exception e) {
                e.printStackTrace();
                Status.updateStatus(seed.getClassName(), "GenerationFailed", "GenerationFailed");
                seeds.remove(seed);
            }
        }
    }

    public static void guidedCodeGeneration(ClassInfo clazz, SootMethod sootMethod){

        int index = FuzzingRandom.nextChoice(NodesContainer.nodeSequences.size());
        NodeSequence flowTemplate = NodesContainer.nodeSequences.get(index);
        List<Stmt> targets = getTargets(clazz, sootMethod.getSignature());
        for (AbstractNode node : flowTemplate.getNodes()) {
            System.out.println(node);
            recurseCodeGeneration(clazz, sootMethod, node, targets,false);
        }
    }

    public static void recurseCodeGeneration(ClassInfo clazz, SootMethod sootMethod, AbstractNode node, List<Stmt> targets, boolean nested) {

        Operator operator = mapNodeTypeToOperator(node);
        BasicBlock block = operator.nextBlock(clazz, sootMethod.getSignature(), targets);
        if (block == null) return;
        blockInsertion(clazz, sootMethod, block, nested);
        if (node.getSuccors() != null) {
            for (AbstractNode succor : node.getSuccors()) {
                System.out.println("  " + succor);
                recurseCodeGeneration(clazz, sootMethod, succor, block.getContents(),true);
            }
        }
    }

    public static Operator mapNodeTypeToOperator(AbstractNode node) {

//        COND, LOOP, SWITCH, SEQ, TRAP
        switch (node.type) {
            case COND:
                return IfOperator.getInstance();
            case SWITCH:
                return SwitchOperator.getInstance();
            case LOOP:
                return LoopOperator.getInstance();
            case TRAP:
                return TrapOperator.getInstance();
            case SEQ:
                int seqIndex = FuzzingRandom.nextChoice(5);
                switch (seqIndex) {
                    case 0:
                        return ApiOperator.getInstance();
                    case 1:
                        return ArrayOperator.getInstance();
                    case 2:
                        return ArithOperator.getInstance();
                    case 3:
                        return FuncOperator.getInstance();
                    case 4:
                        return OperandGenerator.getInstance();
                }
            default:
                throw new RuntimeException("Unknown node type");
        }
    }

    public static void blockInsertion(ClassInfo clazz, SootMethod sootMethod, BasicBlock block, boolean nested) {

        if (block == null) return;
        block.insertBlock(clazz, sootMethod);
    }

    public static void initChecksumClass(BenchmarkInfo originProject, BenchmarkInfo targetProject) {
        ChecksumHelper.createChecksumFile(originProject.getSrcClassPath());
        ChecksumHelper.createChecksumFile(targetProject.getSrcClassPath());
        SootClass check_sum_class = ClassUtils.loadClass("JITFuzzing.Check");
    }

    public static SeedInfo instrumentChecksumStmt(SeedInfo seed, String mutationHistoryPath){

        String classFileFolder = mutationHistoryPath + ExecutionPlatform.FILE_SEPARATOR + seed.getOriginClassName();
        ClassInfo clazz = ClassUtils.loadClassInfo(seed.getClassName());
        SootClass sootClass = clazz.getSootClass();
        for (SootMethod method : sootClass.getMethods()) {
            if(!method.isAbstract()){
                method.retrieveActiveBody();
            }
        }
        // initialize checksum
        ChecksumHelper.checksumForClass(sootClass);
        if (ClassUtils.saveClass(sootClass, Options.output_format_class)) {

            seed.mutationTimesIncrease();
            String name = seed.generateMutateClassFilename();
            seed = new SeedInfo(seed.getOriginClassName(),
                    seed.getOriginClassPath(),
                    name,
                    classFileFolder,
                    seed.isJunit(),
                    seed.getMutationOrder(),
                    0);
            seed.saveSootClassToFile(sootClass);
        }
        Scene.v().removeClass(clazz.getSootClass());
        return seed;
    }
}
