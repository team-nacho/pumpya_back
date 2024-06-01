#!/bin/bash

# PID 변수 생성
PID=$(pgrep -f "java")

if [ -n "$PID" ]; then
  kill $PID
  sleep 5

  if ps -p $PID > /dev/null; then
    kill -9 $PID
fi

# Remove the old artifact
if [ -f /home/ec2-user/pump ]; then
    rm /home/ec2-user/pump
fi
