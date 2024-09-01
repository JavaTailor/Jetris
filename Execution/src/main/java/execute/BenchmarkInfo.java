package execute;

import config.ExecutionPlatform;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;

public class BenchmarkInfo {

    private String benchmarkName;
    private String benchmarkRootPath;
    private String libPath;
    private String libsString;
    private String srcClassPath;
    private long totalSrcClassSize;
    private String testClassPath;
    private long totalTestClassSize;
    private String srcJavaPath;
    private long totalSrcJavaSize;
    private String testJavaPath;
    private long totalTestJavaSize;
    private String pClassPath;

    private ArrayList<String> applicationClasses;
    private ArrayList<String> junitClasses;

    private ArrayList<String> vmoptions;
    private ArrayList<String> projoptions;

    private Boolean hasPredefinedClass = false;
    private String predefinedClassPath = "";

    private ArrayList<String> predefinedClasses;

    public BenchmarkInfo() {
    }

    public BenchmarkInfo(String projectName, String benchmarkRootPath) {
        this.benchmarkName = projectName;
        this.benchmarkRootPath = benchmarkRootPath;
    }

    public BenchmarkInfo(String projectName, String benchmarkRootPath, String libPath) {
        this.benchmarkName = projectName;
        this.benchmarkRootPath = benchmarkRootPath;
        this.libPath = libPath;
    }

    public String getpClassPath(){

        if (pClassPath != null){
            return pClassPath;
        }
        ArrayList<String> pClassPathes = new ArrayList<>();
        if (libsString != null){
            pClassPathes.add(libsString);
        }
        if (srcClassPath != null){
            pClassPathes.add(srcClassPath);
        }
        if (testClassPath != null){
            pClassPathes.add(testClassPath);
        }
        if (srcClassPath == null && testClassPath == null && benchmarkRootPath != null){
            pClassPathes.add(benchmarkRootPath);
        }
        pClassPath = StringUtils.join(pClassPathes, ExecutionPlatform.PATH_SEPARATOR);
        return pClassPath;
    }

    public Boolean getHasPredefinedClass() {
        return hasPredefinedClass;
    }

    public void setHasPredefinedClass(Boolean hasPredefinedClass) {
        this.hasPredefinedClass = hasPredefinedClass;
    }

    public String getPredefinedClassPath() {
        return predefinedClassPath;
    }

    public void setPredefinedClassPath(String predefinedClassPath) {
        hasPredefinedClass = true;
        this.predefinedClassPath = predefinedClassPath;
    }

    public ArrayList<String> getPredefinedClasses() {
        return predefinedClasses;
    }

    public void setPredefinedClasses(ArrayList<String> predefinedClasses) {
        this.predefinedClasses = predefinedClasses;
    }

    public ArrayList<String> getApplicationClasses() {
        return applicationClasses;
    }

    public void setApplicationClasses(ArrayList<String> applicationClasses) {
        this.applicationClasses = applicationClasses;
    }

    public long getTotalSrcClassSize() {
        return totalSrcClassSize;
    }

    public void setTotalSrcClassSize(long totalSrcClassSize) {
        this.totalSrcClassSize = totalSrcClassSize;
    }

    public long getTotalTestClassSize() {
        return totalTestClassSize;
    }

    public void setTotalTestClassSize(long totalTestClassSize) {
        this.totalTestClassSize = totalTestClassSize;
    }

    public long getTotalSrcJavaSize() {
        return totalSrcJavaSize;
    }

    public void setTotalSrcJavaSize(long totalSrcJavaSize) {
        this.totalSrcJavaSize = totalSrcJavaSize;
    }

    public long getTotalTestJavaSize() {
        return totalTestJavaSize;
    }

    public void setTotalTestJavaSize(long totalTestJavaSize) {
        this.totalTestJavaSize = totalTestJavaSize;
    }

    public ArrayList<String> getVmoptions() {
        if (vmoptions == null) {
            return new ArrayList<>();
        }
        return vmoptions;
    }

    public void setVmoptions(ArrayList<String> vmoptions) {
        this.vmoptions = vmoptions;
    }

    public ArrayList<String> getProjoptions() {
        return projoptions;
    }

    public void setProjoptions(ArrayList<String> projoptions) {
        this.projoptions = projoptions;
    }

    public ArrayList<String> getJunitClasses() {
        return junitClasses;
    }

    public void setJunitClasses(ArrayList<String> junitClasses) {
        this.junitClasses = junitClasses;
    }

    public String getBenchmarkName() {
        return benchmarkName;
    }

    public void setBenchmarkName(String benchmarkName) {
        this.benchmarkName = benchmarkName;
    }

    public String getBenchmarkRootPath() {
        return benchmarkRootPath;
    }

    public void setBenchmarkRootPath(String benchmarkRootPath) {
        this.benchmarkRootPath = benchmarkRootPath;
    }

    public String getLibPath() {
        return libPath;
    }

    public void setLibPath(String libPath) {
        this.libPath = libPath;
    }

    public String getLibsString() {
        return libsString;
    }

    public void setLibsString(String libsString) {
        this.libsString = libsString;
    }

    public String getSrcClassPath() {
        return srcClassPath;
    }

    public void setSrcClassPath(String srcClassPath) {
        this.srcClassPath = srcClassPath;
    }

    public String getTestClassPath() {
        return testClassPath;
    }

    public void setTestClassPath(String testClassPath) {
        this.testClassPath = testClassPath;
    }

    public String getSrcJavaPath() {
        return srcJavaPath;
    }

    public void setSrcJavaPath(String srcJavaPath) {
        this.srcJavaPath = srcJavaPath;
    }

    public String getTestJavaPath() {
        return testJavaPath;
    }

    public void setTestJavaPath(String testJavaPath) {
        this.testJavaPath = testJavaPath;
    }

    @Override
    public String toString() {
        String titile = String.join("", Collections.nCopies(50,"=")) +
                " Project Information " + String.join("", Collections.nCopies(50,"="));
        return  titile + ExecutionPlatform.LINE_SEPARATOR +
                "Project Path: " + benchmarkRootPath + ExecutionPlatform.LINE_SEPARATOR +
                "Project Name: " + benchmarkName + ExecutionPlatform.LINE_SEPARATOR +
                "         lib: " + libPath + ExecutionPlatform.LINE_SEPARATOR +
                "         src: " + srcJavaPath + ExecutionPlatform.LINE_SEPARATOR +
                "   total src: " + totalSrcJavaSize + ExecutionPlatform.LINE_SEPARATOR +
                "        test: " + testJavaPath + ExecutionPlatform.LINE_SEPARATOR +
                "  total test: " + totalTestJavaSize + ExecutionPlatform.LINE_SEPARATOR +
                "   src class: " + (srcClassPath != null ? srcClassPath : benchmarkRootPath) + ExecutionPlatform.LINE_SEPARATOR +
                "  test class: " + testClassPath + ExecutionPlatform.LINE_SEPARATOR +
                " applicaiton: " + (applicationClasses != null ? applicationClasses.size() : 0) + "/" + totalSrcClassSize + ExecutionPlatform.LINE_SEPARATOR +
                " junit class: " + (junitClasses != null ? junitClasses.size() :  0) + "/" + totalTestClassSize + ExecutionPlatform.LINE_SEPARATOR +
                String.join("", Collections.nCopies(titile.length(), "="));
    }
}
