#!/usr/bin/env bash

if [ -n "$1" ] ;then
    export mainClass="$1"
    shift
else
    export mainClass="ExecutionPreview"
fi

# default timeout
hour=24
randomSeed=1
while getopts "t:p:s:" opt ; do
    case $opt in
        t)
            export hour="$OPTARG"
            ;;
        p)
            export projectName="$OPTARG"
            ;;
        s)
            export randomSeed="$OPTARG"
            ;;
        \?)
          echo "Invalid option: -$OPTARG"
          exit 1
          ;;
        :)
          echo "Option -$OPTARG requires an argument."
          exit 1
          ;;
    esac
done

# running elements
ProjectPath="."
ClassPath="${ProjectPath}/Jetris/*"
LibPath="${ProjectPath}/lib/*"
OpenJDK8Lib="/usr/lib/jvm/java-8-openjdk-amd64/lib/*:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/*"
ClassPaths="${ClassPath}:${LibPath}:${OpenJDK8Lib}"
ClassPaths=${ClassPaths// }

# setting timeout for experiments
timeout=$((3600*hour))
# create 03results folder for saving results
timeStamp=$(date +%s)
ResultRootPath="03results/$timeStamp"
mkdir -p "$ResultRootPath"

# log file path
logFile="${ResultRootPath}/output"
# running options
options=""
if [ -n "${projectName}" ]; then
  options="${options} -p ${projectName}"
fi

echo "Executing command: timeout ${timeout} java -Xms10g -Xmx10g -ea -cp ${ClassPaths} ${mainClass} -t ${timeStamp} -s ${randomSeed} ${options} 2>&1 | tee ${logFile}.out ..."
timeout ${timeout} java -Xms10g -Xmx10g -ea -cp ${ClassPaths} ${mainClass} -t ${timeStamp} -s ${randomSeed} ${options} 2>&1 | tee ${logFile}.out

mv ./hs_err_* ./03results
mv ./javacore* ./03results