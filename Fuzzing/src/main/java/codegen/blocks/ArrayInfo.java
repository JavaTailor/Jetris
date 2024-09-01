package codegen.blocks;

import soot.Value;
import soot.util.Numberable;

import java.util.ArrayList;
import java.util.List;

public class ArrayInfo {

    protected Numberable array;
    protected int dimension;
    protected List<Value> arraySize;
    protected boolean initialized;
    protected String methodRef;

    public ArrayInfo() {
    }

    public ArrayInfo(Numberable array) {
        this.array = array;
        dimension = 0;
        arraySize = null;
        initialized = false;
    }

    public ArrayInfo(Numberable array, int dimension, Value arraySize) {
        this.array = array;
        this.dimension = dimension;
        this.arraySize = new ArrayList<>();
        this.arraySize.add(arraySize);
        this.initialized = true;
    }

    public ArrayInfo(Numberable array, int dimension, List<Value> arraySize) {
        this.array = array;
        this.dimension = dimension;
        this.arraySize = arraySize;
        this.initialized = true;
    }

    public ArrayInfo(Numberable array, int dimension, Value arraySize, String methodRef) {
        this.array = array;
        this.dimension = dimension;
        this.arraySize = new ArrayList<>();
        this.arraySize.add(arraySize);
        this.initialized = true;
        this.methodRef = methodRef;
    }

    public ArrayInfo(Numberable array, int dimension, List<Value> arraySize, String methodRef) {
        this.array = array;
        this.dimension = dimension;
        this.arraySize = arraySize;
        this.methodRef = methodRef;
        this.initialized = true;
    }

    public Numberable getArray() {
        return array;
    }

    public void setArray(Numberable array) {
        this.array = array;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public List<Value> getArraySize() {
        return arraySize;
    }

    public void setArraySize(List<Value> arraySize) {
        this.arraySize = arraySize;
    }

    public void setArraySize(Value arraySize) {

        if (this.arraySize == null) {
            this.arraySize = new ArrayList<>();
        }
        this.arraySize.add(arraySize);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public String getMethodRef() {
        return methodRef;
    }

    public void setMethodRef(String methodRef) {
        this.methodRef = methodRef;
    }

    public String toString() {
        return this.array.toString();
    }
}
