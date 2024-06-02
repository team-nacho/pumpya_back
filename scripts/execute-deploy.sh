#!/bin/bash

BASE_PATH=/home/ec2-user/pump
BUILD_PATH=$(ls $BASE_PATH/build/libs/*.jar)
JAR_NAME=$(basename $BUILD_PATH)
DEV_LOG=$BASE_PATH/log/deploy.log

echo "> build 파일명: $JAR_NAME"
export BASE_PATH
export BUILD_PATH
export JAR_NAME

if [ -f $DEV_LOG ]; then
  rm $DEV_LOG
fi

touch $DEV_LOG
nohup /home/ec2-user/pump/scripts/deploy.sh > $DEV_LOG 2>&1 &