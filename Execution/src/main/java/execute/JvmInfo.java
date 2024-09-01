package execute;

import config.ExecutionPlatform;
import vmoptions.VMOptions;

import java.util.Collections;

public class JvmInfo {

    private String jvmId;
    private String jvmName;
    private String rootPath;
    private String javaCmd;
    private String version;
    private VMOptions vmOptions = null;

    public JvmInfo(String javaCmd) {
        this.javaCmd = javaCmd;
    }

    public JvmInfo(String rootPath, String folderName, String jvmName, String version, String javaCmd) {
        this.rootPath = rootPath;
        this.jvmName = jvmName;
        this.version = version;
        this.javaCmd = javaCmd;
        this.jvmId = version + "@" + jvmName + "@" + folderName;
    }

    public void setVmOptions(VMOptions vmOptions){
        this.vmOptions = vmOptions;
    }

    public VMOptions getVmOptions(){
        return vmOptions;
    }

    public String getVersion() {
        return version;
    }

    public String getJvmId() {
        return jvmId;
    }

    public String getJvmName() {
        return jvmName;
    }

    public void setJvmName(String jvmName) {
        this.jvmName = jvmName;
    }

    public String getRootPath() {
        return rootPath;
    }

    public String getJavaCmd() {
        return javaCmd;
    }

    @Override
    public String toString() {
        String titile = String.join("", Collections.nCopies(50,"=")) +
                " JVM Implementation " + String.join("", Collections.nCopies(50,"="));
        return  titile + ExecutionPlatform.LINE_SEPARATOR +
                "JVM root path: " + rootPath + ExecutionPlatform.LINE_SEPARATOR +
                "     JVM Impl: " + jvmName + ExecutionPlatform.LINE_SEPARATOR +
                "     Java Cmd: " + javaCmd + ExecutionPlatform.LINE_SEPARATOR +
                "   VM Options: " + (vmOptions != null ? vmOptions.getOptions().size() : "0") + ExecutionPlatform.LINE_SEPARATOR +
                "  JIT Options: " + (vmOptions != null ? vmOptions.getOptionsByComponent("JIT", true).size() : "0") + ExecutionPlatform.LINE_SEPARATOR +
                String.join("", Collections.nCopies(titile.length(),"="));
    }
}
