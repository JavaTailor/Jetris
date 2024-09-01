#!/bin/bash

function clean_up_currentDir {
	find . -maxdepth 1 \
       ! -name cleanProj.sh \
       ! -name cleanResults.sh \
       ! -name Executor.sh \
       ! -name Jetris.sh \
       ! -name Jetris \
       ! -name .git \
       ! -name .gitignore \
       ! -name .idea \
       ! -name 01JVMS \
       ! -name 02Benchmarks \
       ! -name 03results \
       ! -name 04cfgOutput \
       ! -name Compare \
       ! -name Execution \
       ! -name Fuzzing \
       ! -name Main \
       ! -name Options \
       ! -name pom.xml \
       ! -name scripts \
       ! -name sootOutput \
       ! -name src \
       ! -name lib \
       ! -name Jetris.properties \
       ! -name CleanDir.sh \
       ! -name run.sh \
       ! -name dedup.sh \
       ! -name replay.sh \
       ! -name results \
       ! -name target \
       ! -name Execution.iml \
       ! -name Jetris.iml \
       ! -name testcases \
       ! -name testcases.txt \
       ! -name output.txt \
       ! -name README.md -exec rm -rf {} \;
}

currentDir=$(pwd)
Jetris="Jetris"

if [ ${currentDir:0-${#Jetris}} = Jetris ]; then
  echo "cleaning $currentDir ..."
  clean_up_currentDir
else
  echo "WARNING:Please do not clean up folders outside the project!"
fi
