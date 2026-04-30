@echo off
chcp 65001 > nul
SET PROFILE=%1
IF "%PROFILE%"=="" SET PROFILE=dev

:: .env 파일에서 환경 변수 로드
IF EXIST .env (
    FOR /f "tokens=1,* delims==" %%a IN (.env) DO (
        IF NOT "%%a"=="" IF NOT "%%a:~0,1"=="#" SET %%a=%%b
    )
)

echo [INFO] Running server with '%PROFILE%' profile.
call .\mvnw spring-boot:run "-Dspring-boot.run.profiles=%PROFILE%"
