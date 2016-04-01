#!/bin/bash
export set JAVA_OPTS="-XX:+AggressiveHeap"
export CLASSPATH="src:bin:lib/commons-math3-3.2/*:src:bin:lib/*"
#find . -name "*.java" -print | xargs javac
javac src/ec/app/izhikevich/plot/*.java src/ec/app/izhikevich/evaluator/*.java src/ec/app/izhikevich/inputprocess/*.java src/ec/app/izhikevich/inputprocess/labels/*.java src/ec/app/izhikevich/model/*.java src/ec/app/izhikevich/spike/*.java src/ec/app/izhikevich/util/*.java src/ec/app/izhikevich/ModelEvaluatorWrapper.java src/ec/app/izhikevich/starter/*.java
nohup java ec.app.izhikevich.plot.FitnessLandscapePlotter2 input.dat &
