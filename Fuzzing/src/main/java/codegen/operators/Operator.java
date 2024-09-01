package codegen.operators;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import flowinfo.AbstractNode;
import soot.SootMethod;
import soot.jimple.Stmt;

import java.util.List;

/**
 * 操作符接口
 */
public interface Operator {
    BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets);
    BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets, AbstractNode nodeTemp);
}
