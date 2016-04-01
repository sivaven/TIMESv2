#!/bin/bash
#$ -N EcjIzh
#$ -pe shared 16
#$ -l mf=1G
#$ -cwd
export set JAVA_OPTS="-XX:+AggressiveHeap"
export CLASSPATH="src:bin:lib/*:lib/commons-math3-3.6/*"
#find . -name "*.java" -print | xargs javac
javac src/ecjapp/*.java src/ecjapp/eval/*.java src/ecjapp/eval/problem/*.java src/ecjapp/eval/problem/objective/*.java src/ecjapp/statistics/*.java src/ecjapp/util/*.java \
        src/ec/app/izhikevich/*.java \
	        src/ec/app/izhikevich/evaluator/*.java \
	        src/ec/app/izhikevich/inputprocess/*.java \
	        	src/ec/app/izhikevich/inputprocess/labels/*.java \
	    	src/ec/app/izhikevich/model/*.java \
	        src/ec/app/izhikevich/spike/*.java \
	        src/ec/app/izhikevich/util/*.java \
	        src/ec/app/izhikevich/starter/*.java

java ec.app.izhikevich.starter.ECJStarterV3 10_2_3_p4_n2-carl_mc
