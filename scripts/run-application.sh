#!/bin/bash

# Log directories


# Ensure log directory exists
mkdir -p $LOG_DIR

REDIS_CLOUD_HOST=$(aws ssm get-parameter --name "REDIS_CLOUD_HOST" --with-decryption --query "Parameter.Value" --output text)
REDIS_CLOUD_PORT=$(aws ssm get-parameter --name "REDIS_CLOUD_PORT" --with-decryption --query "Parameter.Value" --output text)
REDIS_CLOUD_PASSWORD=$(aws ssm get-parameter --name "REDIS_CLOUD_PASSWORD" --with-decryption --query "Parameter.Value" --output text)
REDIS_CLOUD_NAME=$(aws ssm get-parameter --name "REDIS_CLOUD_NAME" --with-decryption --query "Parameter.Value" --output text)
AWS_RDS_URL=$(aws ssm get-parameter --name "RDS_URL" --with-decryption --query "Parameter.Value" --output text)
AWS_RDS_NAME=$(aws ssm get-parameter --name "RDS_NAME" --with-decryption --query "Parameter.Value" --output text)
AWS_RDS_PASSWORD=$(aws ssm get-parameter --name "RDS_PASSWORD" --with-decryption --query "Parameter.Value" --output text)

# export variables
export REDIS_CLOUD_HOST REDIS_CLOUD_PORT REDIS_CLOUD_PASSWORD REDIS_CLOUD_NAME AWS_RDS_URL AWS_RDS_NAME AWS_RDS_PASSWORD

# Start Spring Boot application
nohup java -jar /home/ec2-user/pump/build/libs/pumpya-0.0.1-SNAPSHOT.jar
