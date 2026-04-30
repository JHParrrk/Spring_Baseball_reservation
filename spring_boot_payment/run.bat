@echo off
chcp 65001 > nul

:: .env 파일에서 환경 변수 로드
IF EXIST .env (
    FOR /f "tokens=1,* delims==" %%a IN (.env) DO (
        IF NOT "%%a"=="" IF NOT "%%a:~0,1"=="#" SET %%a=%%b
    )
)

echo [INFO] Running payment service on port 8081.
call .\mvnw spring-boot:run
