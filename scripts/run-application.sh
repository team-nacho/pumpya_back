#!/bin/bash

fuser -k -n tcp 3000

rm -rf ~/log/pump_back.log
rm -rf ~/log/pump_back-error.log
cp /home/ec2-user/pump.jar /opt/pump/app.jar

# Restart the application
nohup java -jar /path/to/your/app.jar >> /home/ec2-user/log/app.log 2>> /home/ec2-user/log/app-error.log &
