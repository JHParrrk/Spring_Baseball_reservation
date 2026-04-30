#!/bin/bash

# .env 파일에서 환경 변수 로드
if [ -f .env ]; then
    export $(grep -v '^#' .env | grep -v '^$' | xargs)
fi

PROFILE=${1:-dev}

if [ "$1" == "" ]; then
    echo "[INFO] 프로필이 지정되지 않아 기본값(dev)으로 실행합니다."
else
    echo "[INFO] '${PROFILE}' 프로필 환경으로 서버를 실행합니다."
fi

./mvnw spring-boot:run "-Dspring-boot.run.profiles=$PROFILE"
