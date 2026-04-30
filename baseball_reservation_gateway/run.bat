@echo off
chcp 65001 > nul

:: .env 파일에서 환경 변수 로드
IF EXIST .env (
    FOR /f "tokens=1,* delims==" %%a IN (.env) DO (
        IF NOT "%%a"=="" IF NOT "%%a:~0,1"=="#" SET %%a=%%b
    )
)

echo [INFO] API Gateway 시작 중... (port: %GATEWAY_PORT%)
.\mvnw spring-boot:run
