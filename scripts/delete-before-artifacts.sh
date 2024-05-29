#!/bin/bash
# Check if the application is running and stop it
if pgrep -f "pump.jar" > /dev/null; then
    sudo systemctl stop my-spring-app
fi

# Remove the old artifact
if [ -f /home/ec2-user/pump ]; then
    rm /home/ec2-user/pump
fi
