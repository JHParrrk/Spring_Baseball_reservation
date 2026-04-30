@echo off
chcp 65001 > nul

:: .env 파일에서 환경 변수 로드 (run.bat과 동일 방식)
IF EXIST .env (
    FOR /f "tokens=1,* delims==" %%a IN (.env) DO (
        IF NOT "%%a"=="" IF NOT "%%a:~0,1"=="#" SET %%a=%%b
    )
)

:: 실행할 테스트 클래스 지정 (기본: 전체 테스트)
SET TEST_CLASS=%1
IF "%TEST_CLASS%"=="" SET TEST_CLASS=*

echo [INFO] Running test: %TEST_CLASS%
call .\mvnw test "-Dtest=%TEST_CLASS%" "-Dspring.profiles.active=dev"
