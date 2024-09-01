# Program Ingredients Abstraction and Instantiation for Synthesis-based JVM Testing

## Jetris
This is the implementation of our CCS 2024 paper: Program Ingredients Abstraction and Instantiation for Synthesis-based JVM Testing.

## 1. Step to reproduce

### (1) Install the runtime environment
Jetris developed based on Java 8. Please install OpenJDK 8 before running Jetris.
```shell
sudo apt-get install openjdk-8-jdk
```

### (2) Download the latest release of JVMs
Taking OpenJDK8 as an example:
Download Hotspot:
```shell
wget https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u392-b08/OpenJDK8U-jre_x64_linux_hotspot_8u392b08.tar.gz
```
Download OpenJ9:
```shell
wget https://github.com/ibmruntimes/semeru8-binaries/releases/download/jdk8u382-b05_openj9-0.40.0/ibm-semeru-open-jre_x64_linux_8u382b05_openj9-0.40.0.tar.gz
```

### (3) Create the sootOuput directory
Create the sootOutput directory and copy the seed programs from the 02Benchmarks directory to the sootOutput directory.
```shell
cp -r ./02Benchmakrs ./sootOutput
```

### (4) Set the fuzzing properties
Open the [Jetris.properties](Jetris.properties) file in the current directory.

First, set the JVM to be tested.
Each tested.jvm.jdkhome item specifies the JVM to be tested. The key represents the OpenJDK version followed by the JVM implementation, separated by @. The value should be the absolute path of the corresponding JVM java command.
For example, openjdk8@openj9 indicates the JDK version is OpenJDK 8 with the JVM implementation as OpenJ9. 
The Value is the absolute path of the corresponding JVM java cmd, please change the path to the path of the JVM you downloaded in the second step.

Second, set the seed programs (i.e., tested.benchmark.name), which used to provide context for the instantiation of the program ingredients.
Default is HotspotTests-Java, change it if you want to test other programs.

Finally, set the original seed program root directory (tested.benchmark.home) and the runtime directory (tested.sootoutput.home), both are absolute paths.

```properties
################## Execution properties ##########################
tested.jvm.jdkhome = \
  openjdk8@openj9  = pathToOpenJ9/bin/java;\
  openjdk8@hotspot = pathToHotspot/bin/java;
tested.benchmark.name = HotspotTests-Java
tested.benchmark.home = pathToBenchmarks/02Benchmarks
tested.sootoutput.home = pathToSootOutput/sootOutput
class.max.runtime = 60
option.max.size = 9
################## Execution properties ##########################
```

### (5) Start fuzzing
Run the Jetris.sh script with the Main parameter, which specifies the main class to start the fuzzing process.
```shell
./Jetris.sh Main
```
By default, the timeout is set to 24 hours. If you want to change the timeout, modify the hour variable in the Jetris.sh script.
Open the [Jetris.sh](Jetris.sh) file in the current directory and locate the following line:
```sehll
# default timeout
hour=24
```

The results of the fuzzing process will be saved in the 03results directory (created automatically).