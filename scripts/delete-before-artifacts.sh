#!/bin/bash
# Check if the application is running and stop it
if pgrep -f "java" > /dev/null; then
    sudo systemctl stop java
fi

# Remove the old artifact
if [ -f /home/ec2-user/pump ]; then
    rm /home/ec2-user/pump
fi
