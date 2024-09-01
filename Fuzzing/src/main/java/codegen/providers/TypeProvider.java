package codegen.providers;

import config.FuzzingConfig;
import config.FuzzingRandom;
import soot.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TypeProvider {

    /**
     * Primitive type
     */
    public static IntType intType;
    public static BooleanType booleanType;
    public static FloatType floatType;
    public static DoubleType doubleType;
    public static LongType longType;
    public static CharType charType;
    public static ShortType shortType;

    public static RefType refType;

    public static NullType nullType;
    /**
     * Array type
     */
    public static ArrayType arrayType;

    public static ArrayList<Type> primTypes = new ArrayList<>();
    public static ArrayList<Type> refTypes = new ArrayList<>();
    public static HashMap<Type, Integer> types = new HashMap();

    public static void initializeBasicType(){

        intType = IntType.v();
        booleanType = BooleanType.v();
        floatType = FloatType.v();
        doubleType = DoubleType.v();
        longType = LongType.v();
        charType = CharType.v();
        shortType = ShortType.v();
        refType = RefType.v();
        nullType = NullType.v();

        arrayType = ArrayType.v(NullType.v(), 1);

        primTypes.clear();
        types.clear();

        primTypes.add(intType);
        primTypes.add(booleanType);
        primTypes.add(floatType);
        primTypes.add(doubleType);
        primTypes.add(longType);
        primTypes.add(charType);
        primTypes.add(shortType);
        primTypes.add(nullType);

        types.put(intType, FuzzingConfig.PROB_INT_VALUE);
        types.put(booleanType, FuzzingConfig.PROB_BOOL_VALUE);
        types.put(floatType, FuzzingConfig.PROB_FLOAT_VALUE);
        types.put(doubleType, FuzzingConfig.PROB_DOUBLE_VALUE);
        types.put(longType, FuzzingConfig.PROB_LONG_VALUE);
        types.put(charType, FuzzingConfig.PROB_CHAR_VALUE);
        types.put(shortType, FuzzingConfig.PROB_SHORT_VALUE);
        types.put(refType, FuzzingConfig.PROB_OBJECT_VALUE);
    }

    public static void loadRefTypes() {

        initializeBasicType();
        refTypes.clear();
        Scene.v().loadNecessaryClasses();
        for (SootClass clazz: Scene.v().getClasses()) {
            if (clazz.isAbstract()) continue;
            if (!clazz.isPublic()) continue;
            if (FuzzingConfig.INVALID_REFERENCE_TYPE.contains(clazz.getName()) || !clazz.getName().startsWith("java.")) continue;
            for (SootMethod method : clazz.getMethods()) {
                if (method.isPublic() && method.isConstructor()) {
                    refTypes.add(clazz.getType());
                    break;
                }
            }
        }
    }

    public static Type anyType() {
        Type candidate = FuzzingRandom.randomUpTo(types);
        if (candidate instanceof RefType){
            candidate = refTypes.get(FuzzingRandom.nextChoice(refTypes.size()));
        }
        return candidate;
    }

    public static Type anyNonArrayType() {
        Type candidate = FuzzingRandom.randomUpTo(types);
        if (candidate instanceof RefType){
            candidate = refTypes.get(FuzzingRandom.nextChoice(refTypes.size()));
        }
        return candidate;
    }

    public static Type anyRefType() {
        if (refTypes.size() == 0) {
            reloadRef();
        }
        return refTypes.get(FuzzingRandom.nextChoice(refTypes.size()));
    }

    public static Type anyPrimType() {
        Type type = FuzzingRandom.randomUpTo(types);
        while (type instanceof RefType) type = FuzzingRandom.randomUpTo(types);
        return type;
    }

    public static void reloadRef(){
        for (SootClass clazz: Scene.v().getClasses()) {
            if (clazz.isAbstract()) continue;
            if (!clazz.isPublic()) continue;
            if (FuzzingConfig.INVALID_REFERENCE_TYPE.contains(clazz.getName()) || !clazz.getName().startsWith("java.")) continue;
            for (SootMethod method : clazz.getMethods()) {
                if (method.isPublic() && method.isConstructor()) {
                    refTypes.add(clazz.getType());
                    break;
                }
            }
        }
    }
}
