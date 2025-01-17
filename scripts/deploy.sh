#!/bin/bash

echo "> build 파일 복사"
DEPLOY_PATH=$BASE_PATH/jar/
cp $BUILD_PATH $DEPLOY_PATH

echo "> 현재 구동중인 port 확인"
CURRENT_PROFILE=$(curl -s http://*)

# 사용되고 있지 않은 진영 찾기
if [ "$CURRENT_PROFILE" == "green" ]
then
  export IDLE_PROFILE=blue
  IDLE_PORT=8081
elif [ "$CURRENT_PROFILE" == "blue" ]
then
  export IDLE_PROFILE=green
  IDLE_PORT=8082
else
  echo "> No Profile. Profile: $CURRENT_PROFILE"
  echo "> set profile to blue | IDLE_PROFILE: blue"
  export IDLE_PROFILE=blue
  IDLE_PORT=8081
fi

# 진영 교체
IDLE_APPLICATION=$IDLE_PROFILE-pump.jar
export IDLE_APPLICATION_PATH=$DEPLOY_PATH$IDLE_APPLICATION

ln -Tfs $DEPLOY_PATH$JAR_NAME $IDLE_APPLICATION_PATH

echo "> $IDLE_PROFILE 에서 구동중인 애플리케이션 pid 확인"
IDLE_PID=$(pgrep -f $IDLE_APPLICATION)

if [ -z "$IDLE_PID" ]
then
  echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
  echo "> kill -15 $IDLE_PID"
  sudo kill -15 $IDLE_PID

  echo "> 애플리케이션 종료 대기 중..."
    while kill -0 $IDLE_PID > /dev/null 2>&1; do
      echo "> 여전히 종료 중입니다. 잠시 대기합니다..."
      sleep 1
    done
    echo "> 애플리케이션이 종료되었습니다."
fi

echo "> $IDLE_PROFILE 배포"
$BASE_PATH/scripts/run-application.sh

echo "> $IDLE_PROFILE 10초 후 Health check 시작"
echo "> curl -s http://localhost:$IDLE_PORT/actuator/health "
sleep 10

for retry_count in {1..10}
do
  response=$(curl -s http://localhost:$IDLE_PORT/actuator/health)
  up_count=$(echo $response | grep 'UP' | wc -l)

  if [ $up_count -ge 1 ]
  then # $up_count >= 1 ("UP" 문자열이 있는지 검증)
      echo "> Health check 성공"
      break
  else
      echo "> Health check의 응답을 알 수 없거나 혹은 status가 UP이 아닙니다."
      echo "> Health check: ${response}"
  fi

  if [ $retry_count -eq 10 ]
  then
    echo "> Health check 실패. "
    echo "> Nginx에 연결하지 않고 배포를 종료합니다."
    exit 1
  fi

  echo "> Health check 연결 실패. 재시도..."
  sleep 10
done

echo "> 스위칭"
sleep 10
$BASE_PATH/scripts/switch.sh
