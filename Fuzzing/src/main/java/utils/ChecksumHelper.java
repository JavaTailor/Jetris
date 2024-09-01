package utils;

import codegen.providers.NameProvider;
import config.ExecutionPlatform;
import org.junit.Test;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;

import java.io.*;
import java.util.*;

public class ChecksumHelper {

    // checksum 字节码
    private static final byte[] bytes = new byte[]{
            -54,-2,-70,-66,0,0,0,52,0,116,10,0,27,0,48,7,0,49,8,0,50,8,0,51,8,0,52,8,0,53,8,0,54,8,0,55,8,0,56,9,0,57,0,58,10,0,2,0,59,10,
            0,2,0,60,7,0,61,7,0,62,10,0,63,0,64,10,0,27,0,65,10,0,2,0,66,10,0,67,0,68,10,0,69,0,70,10,0,71,0,72,10,0,73,0,74,10,0,75,0,76,
            10,0,77,0,78,10,0,79,0,80,10,0,81,0,82,7,0,83,7,0,84,1,0,6,60,105,110,105,116,62,1,0,3,40,41,86,1,0,4,67,111,100,101,1,0,15,76,105,110,101,
            78,117,109,98,101,114,84,97,98,108,101,1,0,8,99,104,101,99,107,115,117,109,1,0,22,40,73,76,106,97,118,97,47,108,97,110,103,47,79,98,106,101,99,116,59,41,73,1,0,13,
            83,116,97,99,107,77,97,112,84,97,98,108,101,7,0,84,7,0,85,7,0,62,1,0,5,40,73,66,41,73,1,0,5,40,73,83,41,73,1,0,5,40,73,73,41,73,1,0,5,40,
            73,74,41,73,1,0,5,40,73,70,41,73,1,0,5,40,73,68,41,73,1,0,5,40,73,67,41,73,1,0,5,40,73,90,41,73,1,0,10,83,111,117,114,99,101,70,105,108,101,1,
            0,10,67,104,101,99,107,46,106,97,118,97,12,0,28,0,29,1,0,16,106,97,118,97,47,108,97,110,103,47,83,116,114,105,110,103,1,0,4,116,105,109,101,1,0,9,101,120,99,101,
            112,116,105,111,110,1,0,5,101,114,114,111,114,1,0,7,102,97,105,108,117,114,101,1,0,3,106,100,107,1,0,3,106,114,101,1,0,6,115,121,115,116,101,109,7,0,86,12,0,87,
            0,88,12,0,89,0,90,12,0,91,0,92,1,0,18,106,97,118,97,47,108,97,110,103,47,82,117,110,110,97,98,108,101,1,0,19,106,97,118,97,47,108,97,110,103,47,69,120,99,101,
            112,116,105,111,110,7,0,93,12,0,94,0,95,12,0,96,0,97,12,0,98,0,99,7,0,100,12,0,98,0,101,7,0,102,12,0,98,0,103,7,0,104,12,0,98,0,105,7,0,106,
            12,0,98,0,107,7,0,108,12,0,98,0,109,7,0,110,12,0,98,0,111,7,0,112,12,0,98,0,113,7,0,114,12,0,98,0,115,1,0,16,74,73,84,70,117,122,122,105,110,103,
            47,67,104,101,99,107,1,0,16,106,97,118,97,47,108,97,110,103,47,79,98,106,101,99,116,1,0,19,91,76,106,97,118,97,47,108,97,110,103,47,83,116,114,105,110,103,59,1,0,16,
            106,97,118,97,47,117,116,105,108,47,76,111,99,97,108,101,1,0,4,82,79,79,84,1,0,18,76,106,97,118,97,47,117,116,105,108,47,76,111,99,97,108,101,59,1,0,11,116,111,76,
            111,119,101,114,67,97,115,101,1,0,38,40,76,106,97,118,97,47,117,116,105,108,47,76,111,99,97,108,101,59,41,76,106,97,118,97,47,108,97,110,103,47,83,116,114,105,110,103,59,1,
            0,8,99,111,110,116,97,105,110,115,1,0,27,40,76,106,97,118,97,47,108,97,110,103,47,67,104,97,114,83,101,113,117,101,110,99,101,59,41,90,1,0,30,99,111,109,47,97,108,105,
            98,97,98,97,47,102,97,115,116,106,115,111,110,47,74,83,79,78,65,114,114,97,121,1,0,6,116,111,74,83,79,78,1,0,38,40,76,106,97,118,97,47,108,97,110,103,47,79,98,106,
            101,99,116,59,41,76,106,97,118,97,47,108,97,110,103,47,79,98,106,101,99,116,59,1,0,8,116,111,83,116,114,105,110,103,1,0,20,40,41,76,106,97,118,97,47,108,97,110,103,47,
            83,116,114,105,110,103,59,1,0,8,104,97,115,104,67,111,100,101,1,0,3,40,41,73,1,0,17,106,97,118,97,47,108,97,110,103,47,73,110,116,101,103,101,114,1,0,4,40,73,41,
            73,1,0,14,106,97,118,97,47,108,97,110,103,47,66,121,116,101,1,0,4,40,66,41,73,1,0,15,106,97,118,97,47,108,97,110,103,47,83,104,111,114,116,1,0,4,40,83,41,73,
            1,0,14,106,97,118,97,47,108,97,110,103,47,76,111,110,103,1,0,4,40,74,41,73,1,0,15,106,97,118,97,47,108,97,110,103,47,70,108,111,97,116,1,0,4,40,70,41,73,1,
            0,16,106,97,118,97,47,108,97,110,103,47,68,111,117,98,108,101,1,0,4,40,68,41,73,1,0,19,106,97,118,97,47,108,97,110,103,47,67,104,97,114,97,99,116,101,114,1,0,4,
            40,67,41,73,1,0,17,106,97,118,97,47,108,97,110,103,47,66,111,111,108,101,97,110,1,0,4,40,90,41,73,0,33,0,26,0,27,0,0,0,0,0,10,0,1,0,28,0,29,0,
            1,0,30,0,0,0,33,0,1,0,1,0,0,0,5,42,-73,0,1,-79,0,0,0,1,0,31,0,0,0,10,0,2,0,0,0,7,0,4,0,8,0,9,0,32,0,33,0,1,0,30,
            0,0,1,2,0,4,0,7,0,0,0,-126,43,-63,0,2,-103,0,93,16,7,-67,0,2,89,3,18,3,83,89,4,18,4,83,89,5,18,5,83,89,6,18,6,83,89,7,18,7,83,89,
            8,18,8,83,89,16,6,18,9,83,77,44,78,44,-66,54,4,3,54,5,21,5,21,4,-94,0,35,45,21,5,50,58,6,43,-64,0,2,-78,0,10,-74,0,11,25,6,-74,0,12,-103,0,
            5,3,-84,-124,5,1,-89,-1,-36,43,-63,0,13,-102,0,27,43,-63,0,14,-102,0,20,43,-72,0,15,-74,0,16,-74,0,17,-72,0,18,-84,77,3,-84,3,-84,0,1,0,111,0,124,0,125,
            0,14,0,2,0,31,0,0,0,58,0,14,0,0,0,11,0,7,0,12,0,49,0,13,0,51,0,14,0,55,0,16,0,65,0,17,0,71,0,18,0,89,0,19,0,91,0,16,0,97,
            0,24,0,111,0,26,0,125,0,27,0,126,0,28,0,-128,0,31,0,34,0,0,0,38,0,5,-1,0,58,0,6,1,7,0,35,7,0,36,7,0,36,1,1,0,0,32,-1,0,5,0,
            2,1,7,0,35,0,0,91,7,0,37,2,0,9,0,32,0,38,0,1,0,30,0,0,0,40,0,2,0,2,0,0,0,12,26,27,-72,0,19,96,59,26,-72,0,18,-84,0,0,0,1,
            0,31,0,0,0,10,0,2,0,0,0,36,0,7,0,37,0,9,0,32,0,39,0,1,0,30,0,0,0,40,0,2,0,2,0,0,0,12,26,27,-72,0,20,96,59,26,-72,0,18,-84,
            0,0,0,1,0,31,0,0,0,10,0,2,0,0,0,41,0,7,0,42,0,9,0,32,0,40,0,1,0,30,0,0,0,40,0,2,0,2,0,0,0,12,26,27,-72,0,18,96,59,26,
            -72,0,18,-84,0,0,0,1,0,31,0,0,0,10,0,2,0,0,0,46,0,7,0,47,0,9,0,32,0,41,0,1,0,30,0,0,0,40,0,3,0,3,0,0,0,12,26,31,-72,0,
            21,96,59,26,-72,0,18,-84,0,0,0,1,0,31,0,0,0,10,0,2,0,0,0,51,0,7,0,52,0,9,0,32,0,42,0,1,0,30,0,0,0,40,0,2,0,2,0,0,0,12,
            26,35,-72,0,22,96,59,26,-72,0,18,-84,0,0,0,1,0,31,0,0,0,10,0,2,0,0,0,56,0,7,0,57,0,9,0,32,0,43,0,1,0,30,0,0,0,40,0,3,0,3,
            0,0,0,12,26,39,-72,0,23,96,59,26,-72,0,18,-84,0,0,0,1,0,31,0,0,0,10,0,2,0,0,0,61,0,7,0,62,0,9,0,32,0,44,0,1,0,30,0,0,0,40,
            0,2,0,2,0,0,0,12,26,27,-72,0,24,96,59,26,-72,0,18,-84,0,0,0,1,0,31,0,0,0,10,0,2,0,0,0,66,0,7,0,67,0,9,0,32,0,45,0,1,0,30,
            0,0,0,40,0,2,0,2,0,0,0,12,26,27,-72,0,25,96,59,26,-72,0,18,-84,0,0,0,1,0,31,0,0,0,10,0,2,0,0,0,71,0,7,0,72,0,1,0,46,0,0,
            0,2,0,47
    };
    private static List<String> skipClass = new ArrayList<>();

    @Test
    public void transformChecksumClass() throws IOException {
        int i= 0;
        for (byte b : readFromByteFile("D:\\我的文件\\研究生\\JitFuzzing\\JITFuzzing\\Check.class")) {
            System.out.print(b+",");
            i++;
            if(i == 50){
                i=0;
                System.out.println();
            }
        }
    }

    private static boolean isSkipClass(String name){
        if(skipClass.isEmpty()){
            return false;
        }
        for (String aClass : skipClass) {
            if(aClass.contains(name)){
                return true;
            }
        }
        return false;
    }

    /**
     * 创建 check_sum.class 文件
     * @param pathname
     * @throws IOException
     */
    public static void createChecksumFile(String pathname) {
        try {
            File pac = new File(pathname + ExecutionPlatform.FILE_SEPARATOR + "JITFuzzing");
            if (!pac.exists()) {
                pac.mkdirs();
            }
            File filename = new File(pathname + ExecutionPlatform.FILE_SEPARATOR + "JITFuzzing" + ExecutionPlatform.FILE_SEPARATOR + "Check.class");
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
            out.write(bytes);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 对一个 sootclass 添加 checksum 相关指令
     * @param sootClass
     * @return
     */
    public static void checksumForClass(SootClass sootClass){

        try {
            if(isSkipClass(sootClass.getName())){
                return;
            }
            SootMethod clinit = null;
            for (SootMethod method : sootClass.getMethods()) {
                if (method.isStaticInitializer()) {
                    clinit = method;
                    break;
                }
            }
            if (clinit == null) {

                SootMethod method = new SootMethod("<clinit>", new ArrayList<>(), VoidType.v(),8);
                JimpleBody body = Jimple.v().newBody(method);
                body.getUnits().add(new JReturnVoidStmt());
                sootClass.addMethod(method);
                method.setActiveBody(body);
            }

            // 插入全局变量
            clinit = sootClass.getMethod("void <clinit>()");
            insertGlobalVarStmt(sootClass, clinit.retrieveActiveBody());

            for (SootMethod method : sootClass.getMethods()) {

                if (!method.isStaticInitializer() && !method.isConstructor()
                        && method.hasActiveBody()) {
                    Body body = method.retrieveActiveBody();
                    List<Local> locals = new ArrayList<>(body.getLocals());
                    // 插入checksum语句
                    for (Local local : locals) {
//                    if (local.getType() instanceof ShortType ||
//                            local.getType() instanceof CharType) {
//                        continue;
//                    }
                        insertCheckSumStmtAfterLastWrite(sootClass,method.retrieveActiveBody(), local, getLastWriteMap(body));
                    }
                    // 插入输出语句
                    if(method.toString().contains("main")){
                        insertPrintStmtBeforeReturn(sootClass, body);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 对新添加的 local 变量进行 checksum
     * @param sootClass 类
     * @param body 函数体
     * @param locals 新 local
     */
    public static void updateCheckSumStmtAfterLastWrite(SootClass sootClass,Body body,List<Local> locals){

        if(isSkipClass(sootClass.getName())){
            return;
        }
        Map<String,Unit> map = getLastWriteMap(body);
        for (Local local : locals) {
//            if (local.getType() instanceof ShortType ||
//                    local.getType() instanceof CharType) {
//                continue;
//            }
            Unit targetUnit = map.get(local.getName());
            if(targetUnit instanceof JIdentityStmt){
                return;
            }
            List<Stmt> stmts = genCheckSumStmt(sootClass,body,local);
            if(stmts != null){
                if(targetUnit != null) {
                    body.getUnits().insertAfter(stmts, targetUnit);
                }
            }
        }
    }

    /**
     * 读字节码文件用的
     * @param pathname
     * @return
     * @throws IOException
     */
    private static byte[] readFromByteFile(String pathname) throws IOException {

        File filename = new File(pathname);
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        byte[] temp = new byte[1024];
        int size = 0;
        while((size = in.read(temp)) != -1){
            out.write(temp, 0, size);
        }
        in.close();
        byte[] content = out.toByteArray();
        return content;
    }

    /**
     * 获得所有 local 变量 最后一次被 write 时的语句映射
     * @param body
     * @return
     */
    private static Map<String,Unit> getLastWriteMap(Body body){

        Map<String,List<Unit>> varWriteMaps = new HashMap<>(); // 存储还未被使用的局部变量

        // 只分析赋值语句和调用语句
        for (Unit unit : body.getUnits()) {
            if (unit instanceof JAssignStmt || unit instanceof JInvokeStmt) {
                // 获取语句中的local
                for (ValueBox box : unit.getUseAndDefBoxes()) {
                    if (box.getValue() instanceof Local) {
                        String localName = ((Local) box.getValue()).getName();
                        if (varWriteMaps.containsKey(localName)) {
                            varWriteMaps.get(localName).add(unit);
                        } else {
                            ArrayList<Unit> tmp = new ArrayList<>();
                            tmp.add(unit);
                            varWriteMaps.put(localName, tmp);
                        }
                    }
                }
            }
        }
        Map<String,Unit> ret = new HashMap<>();
        for (String s : varWriteMaps.keySet()) {
            if (varWriteMaps.get(s).size() != 1) {
                ret.put(s, varWriteMaps.get(s).get(varWriteMaps.get(s).size() - 1));
            }
        }
        return ret;
    }

    /**
     * 在对于 Value 的最后一次赋值时，插入checksum
     * @param sootClass
     * @param body
     * @param value
     */
    private static void insertCheckSumStmtAfterLastWrite(SootClass sootClass, Body body, Value value, Map<String, Unit> map){

        Unit targetUnit = map.get(value.toString());
        if(targetUnit instanceof JIdentityStmt){
            return;
        }

        if(targetUnit != null) {
            List<Stmt> stmts = genCheckSumStmt(sootClass, body, value);
            if (stmts != null)
                body.getUnits().insertAfter(stmts, targetUnit);
        }
    }

    /**
     * 在 Return 语句之前 插入输出语句
     * @param sootClass
     * @param body
     */
    private static void insertPrintStmtBeforeReturn(SootClass sootClass, Body body){

        List<Unit> units = new ArrayList<>(body.getUnits());
        for (Unit unit : units) {
            if(unit instanceof JReturnVoidStmt || unit instanceof JReturnStmt){
                List<Stmt> stmts = genPrintStmt(sootClass, body);
                unit.redirectJumpsToThisTo(stmts.get(0));
                body.getUnits().insertBefore(stmts, unit);
            }
        }
    }

    /**
     * 插入全局变量的语句序列
     * @param sootClass 要进行插入的类
     * @return
     */
    private static List<Stmt> insertGlobalVarStmt(SootClass sootClass, Body body){

        List<Stmt> stmtList = new ArrayList<>();

        // 添加全局变量CHECKSUM
        SootField sootField= new SootField("CHECKSUM", IntType.v(),Modifier.PUBLIC | Modifier.STATIC);
        sootClass.addField(sootField);
        // 将CHECKSUM赋值为 0
        JAssignStmt initChecksum = (JAssignStmt) Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(sootField.makeRef()), IntConstant.v(0));
        stmtList.add(initChecksum);

        StaticFieldRef fieldRef = Jimple.v().newStaticFieldRef(sootField.makeRef());

        // 将CHECKSUM赋值给var1
        Local var1 = Jimple.v().newLocal(NameProvider.genVarName(), IntType.v());
        JAssignStmt assignVar1 = (JAssignStmt) Jimple.v().newAssignStmt(var1, fieldRef);
        stmtList.add(assignVar1);

        // 将my_check_sum赋值给var2
        Local var2 = Jimple.v().newLocal(NameProvider.genVarName(), IntType.v());
        JAssignStmt assignVar2 = (JAssignStmt) Jimple.v().newAssignStmt(var2, fieldRef);
        stmtList.add(assignVar2);

        //调用check_sum:int checksum(int,int)
        SootClass check_sum_class = ClassUtils.loadClass("JITFuzzing.Check");
        List<Local> args = new ArrayList<>();
        args.add(var1);
        args.add(var2);
        StaticInvokeExpr funcInvoke = Jimple.v().newStaticInvokeExpr(check_sum_class.getMethod("int checksum(int,int)").makeRef(), args);

        Local var3 = Jimple.v().newLocal(NameProvider.genVarName(), IntType.v());
        JAssignStmt assignVar3 = (JAssignStmt) Jimple.v().newAssignStmt(var3,funcInvoke);
        stmtList.add(assignVar3);

        // 将var3赋值给my_check_sum
        JAssignStmt assignBackToField = (JAssignStmt) Jimple.v().newAssignStmt(fieldRef, var3);
        stmtList.add(assignBackToField);

        body.getLocals().add(var1);
        body.getLocals().add(var2);
        body.getLocals().add(var3);
        //这里不需要使用 insert no direct 因为没有分支
        body.getUnits().insertBefore(stmtList, body.getUnits().getLast());
        return stmtList;
    }

    /**
     * 输出 check_sum的 语句序列
     * @param sootClass 要进行插入的类
     * @return
     */
    private static List<Stmt> genPrintStmt(SootClass sootClass, Body body){

        List<Stmt> stmtList = new ArrayList<>();
        //01 get System.out
        SootClass systemClass = ClassUtils.loadClass("java.lang.System");
        SootField sootField = systemClass.getField("java.io.PrintStream out");
        StaticFieldRef fieldRef = Jimple.v().newStaticFieldRef(sootField.makeRef());
        Local outLocal = Jimple.v().newLocal(NameProvider.genVarName(), sootField.getType());
        body.getLocals().add(outLocal);
        AssignStmt streamAssignment = Jimple.v().newAssignStmt(outLocal, fieldRef);
        stmtList.add(streamAssignment);

        //02 new StringBuilder
        SootClass builderClass = ClassUtils.loadClass("java.lang.StringBuilder");
        Local builderLocal = Jimple.v().newLocal(NameProvider.genVarName(), builderClass.getType());
        body.getLocals().add(builderLocal);
        AssignStmt init = Jimple.v().newAssignStmt(builderLocal, Jimple.v().newNewExpr(builderClass.getType()));
        stmtList.add(init);

        //03 init String builder
        InvokeStmt initBuilder = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(builderLocal, builderClass.getMethod("void <init>()").makeRef(), new ArrayList<>()));
        stmtList.add(initBuilder);

        //04 append CHECKSUM
        List<Value> args = new ArrayList<>();
        args.add(StringConstant.v("CHECKSUM: "));
        VirtualInvokeExpr builder = Jimple.v().newVirtualInvokeExpr(builderLocal, builderClass.getMethod("java.lang.StringBuilder append(java.lang.String)").makeRef(), args);
        stmtList.add(Jimple.v().newAssignStmt(builderLocal, builder));

        //05 get CHECKSUM value
        StaticFieldRef checksumRef = Jimple.v().newStaticFieldRef(sootClass.getField("CHECKSUM", IntType.v()).makeRef());
        Local checksumLocal = Jimple.v().newLocal(NameProvider.genVarName(), checksumRef.getType());
        body.getLocals().add(checksumLocal);
        stmtList.add(Jimple.v().newAssignStmt(checksumLocal, checksumRef));

        //06 append CHECKSUM value
        List<Local> nargs = new ArrayList<>();
        nargs.add(checksumLocal);
        VirtualInvokeExpr appendChecksumValue = Jimple.v().newVirtualInvokeExpr(builderLocal, builderClass.getMethod("java.lang.StringBuilder append(int)").makeRef(), nargs);
        stmtList.add(Jimple.v().newAssignStmt(builderLocal, appendChecksumValue));

        //07 string builder tostring
        SootMethod sootMethod = builderClass.getMethod("java.lang.String toString()");
        VirtualInvokeExpr getFullString = Jimple.v().newVirtualInvokeExpr(builderLocal, sootMethod.makeRef(), new ArrayList<>());
        Local stringLocal = Jimple.v().newLocal(NameProvider.genVarName(), sootMethod.getReturnType());
        body.getLocals().add(stringLocal);
        stmtList.add(Jimple.v().newAssignStmt(stringLocal, getFullString));

        //08 print string
        SootClass streamClass = ClassUtils.loadClass("java.io.PrintStream");
        InvokeStmt printInvoke = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(outLocal, streamClass.getMethod("void println(java.lang.String)").makeRef(), stringLocal));
        stmtList.add(printInvoke);

        return stmtList;
    }

    /**
     * 对变量 value 计算 check_sum 的语句
     * @param sootClass 要进行插入的类
     * @param target 要进行计算的 Local 变量
     * @return
     */
    private static List<Stmt> genCheckSumStmt(SootClass sootClass, Body body, Value target){

        List<Stmt> stmtList = new ArrayList<>();

        StaticFieldRef fieldRef = Jimple.v().newStaticFieldRef(sootClass.getField("CHECKSUM", IntType.v()).makeRef());

        // 取 CHECKSUM
        Local fieldLocal = Jimple.v().newLocal(NameProvider.genVarName(), IntType.v());
        body.getLocals().add(fieldLocal);
        JAssignStmt assignStmt1 = (JAssignStmt) Jimple.v().newAssignStmt(fieldLocal, fieldRef);
        stmtList.add(assignStmt1);

        // 调用checksum函数
        String type = "";
        switch (target.getType().toString()){
            case "byte"   : type = "byte";    break;
            case "short"  : type = "short";   break;
            case "int"    : type = "int";     break;
            case "long"   : type = "long";    break;
            case "float"  : type = "float";   break;
            case "double" : type = "double";  break;
            case "char"   : type = "char";    break;
            case "boolean": type = "boolean"; break;
            default       : return null;
//            default       : type ="java.lang.Object";
        }
        if (type.equals("short") || type.equals("char")) {
            return null;
        }

        SootClass check_sum_class = ClassUtils.loadClass("JITFuzzing.Check");
        List<Value> args = new ArrayList<>();
        args.add(fieldLocal);
        args.add(target);
        StaticInvokeExpr funcInvoke = Jimple.v().newStaticInvokeExpr(check_sum_class.getMethod("int checksum(int," + type + ")").makeRef(), args);

        Local retValue = Jimple.v().newLocal(NameProvider.genVarName(), IntType.v());
        body.getLocals().add(retValue);
        JAssignStmt saveFuncRetValue = (JAssignStmt) Jimple.v().newAssignStmt(retValue, funcInvoke);
        stmtList.add(saveFuncRetValue);

        // 覆盖 CHECKSUM
        JAssignStmt writeChecksum = (JAssignStmt) Jimple.v().newAssignStmt(fieldRef, retValue);
        stmtList.add(writeChecksum);

        return stmtList;
    }
}