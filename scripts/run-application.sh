#!/bin/bash

# Log directories
LOG_DIR="/home/ec2-user/log"
APP_LOG="$LOG_DIR/app.log"
ERROR_LOG="$LOG_DIR/app-error.log"

# Ensure log directory exists
mkdir -p $LOG_DIR

# Start Spring Boot application
nohup java -jar /home/ec2-user/pump/build/libs/pumpya-0.0.1-SNAPSHOT.jar >> $APP_LOG 2>> $ERROR_LOG &
