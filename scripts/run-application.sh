#!/bin/bash

fuser -k -n tcp 3000

rm -rf ~/log/pump_back.log
rm -rf ~/log/pump_back-error.log
cp /home/ec2-user/pump.jar /opt/pump/app.jar

# Restart the application
sudo systemctl start pump >> /home/ec2-user/log/pump_back.log 2>> /home/ec2-user/log/pump_back-error.log &
