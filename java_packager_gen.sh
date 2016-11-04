#!/bin/bash
ROOT_DIR=./

if [ ! -z $1 ]; then
	ROOT_DIR=$1
fi

APP_NAME=MiLiveView
APP_CLASS=de.mirb.milivi.MiLiveViewApp
SRC_DIR=$ROOT_DIR/target/scala-2.11
SRC_FILE=MiLiVi-assembly-0.1.jar
OUT_DIR=$ROOT_DIR/target/
OUT_FILE=app-result

echo "Create from root dir '$ROOT_DIR' into '$OUT_DIR' with name '$OUT_FILE'"

#${JAVA_HOME}/bin/javapackager -createjar -nocss2bin -appclass $APP_CLASS -srcdir $SRC_DIR -outdir $OUT_DIR -outfile $OUT_FILE

${JAVA_HOME}/bin/javapackager -deploy -native image -name $APP_NAME -appclass $APP_CLASS -srcdir $SRC_DIR -srcfiles $SRC_FILE -outdir $OUT_DIR -outfile $OUT_FILE
