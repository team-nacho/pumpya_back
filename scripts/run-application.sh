#!/bin/bash

# Log directories
BASE_PATH=/home/ec2-user/pump
LOG_DIR="$BASE_PATH/log"
APP_LOG="$LOG_DIR/app.log"
ERROR_LOG="$LOG_DIR/app-error.log"

# Ensure log directory exists
mkdir -p $LOG_DIR

*

# export variables
export REDIS_CLOUD_HOST REDIS_CLOUD_PORT REDIS_CLOUD_PASSWORD REDIS_CLOUD_NAME AWS_RDS_URL AWS_RDS_NAME AWS_RDS_PASSWORD

# Start Spring Boot application
nohup java -jar -Dspring.profiles.active=$IDLE_PROFILE $IDLE_APPLICATION_PATH >> $APP_LOG 2>> $ERROR_LOG &
