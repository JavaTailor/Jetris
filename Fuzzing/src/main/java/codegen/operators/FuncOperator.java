package codegen.operators;

import codegen.Generator;
import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.blocks.FuncBlock;
import codegen.providers.*;
import config.FuzzingConfig;

import flowinfo.AbstractNode;
import soot.*;
import soot.jimple.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class FuncOperator extends Generic{
    protected static FuncOperator fop;

    public static FuncOperator getInstance() {
        if (fop == null) {
            fop = new FuncOperator();
        }
        return fop;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {

        if (clazz.getMethodMaps().size() >= FuzzingConfig.MAX_FUNS_NUM) return new FuncBlock(null);
        SootMethod method = MethodProvider.createNewMethod(clazz);
        FuncBlock block = new FuncBlock(method);
        block.addStmt((Stmt)method.getActiveBody().getUnits().getLast());
        block.setInserationTarget((Stmt)method.getActiveBody().getUnits().getLast()); //插入点是return语句的前面
        block.setContents(block.getStmts());
        return block;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets, AbstractNode nodeTemp) {

        if (clazz.getMethodMaps().size() >= FuzzingConfig.MAX_FUNS_NUM) return new FuncBlock(null);

        HashSet<Type> defSet = nodeTemp.getProperty().getDefSet();
        ArrayList<Type> refTypes = new ArrayList<>();
        for (Type type : defSet) {
            if (type instanceof RefType) {
                refTypes.add(type);
            }
        }
        SootMethod method = MethodProvider.createNewMethodWithType(clazz, refTypes);
        FuncBlock block = new FuncBlock(method);
        block.addStmt((Stmt)method.getActiveBody().getUnits().getLast());
        block.setInserationTarget((Stmt)method.getActiveBody().getUnits().getLast()); //插入点是return语句的前面
        block.setContents(block.getStmts());
        return block;
    }
}
