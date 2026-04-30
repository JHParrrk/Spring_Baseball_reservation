@echo off
setlocal

:: UTF-8 인코딩 설정
chcp 65001 > nul

:: 1. Docker Infra 시작 (MariaDB, Redis, RabbitMQ)
echo [1/5] Starting Infra Services (Docker)...
cd spring_boot_reservation
docker-compose up -d
echo [INFO] 인프라 초기화 대기 (30초 - Kafka/ZooKeeper 포함)...
timeout /t 30 /nobreak > nul
cd ..

:: 2. Reservation Service (8080)
echo [2/5] Starting Reservation Service on port 8080...
start "Reservation Service" cmd /c "cd spring_boot_reservation && run.bat"

:: 3. Payment Service (8081)
echo [3/5] Starting Payment Service on port 8081...
start "Payment Service" cmd /c "cd spring_boot_payment && run.bat"

:: 4. Cloud Gateway (8082)
echo [4/5] Starting API Gateway on port 8082...
start "API Gateway" cmd /c "cd baseball_reservation_gateway && run.bat"

:: 5. Frontend (3000)
echo [5/5] Starting Frontend on port 3000...
start "Frontend" cmd /c "cd baseball_reservation_FE && npm run dev"

echo.
echo ======================================================
echo  모든 서비스가 백그라운드 터미널에서 실행 중입니다.
echo  Spring Boot 서비스는 기동에 30-60초 소요될 수 있습니다.
echo  - Frontend:      http://localhost:3000
echo  - Gateway:       http://localhost:8082
echo  - Swagger(Res):  http://localhost:8080/swagger-ui.html
echo  - Swagger(Pay):  http://localhost:8081/swagger-ui.html
echo  - RabbitMQ UI:   http://localhost:15672
echo ======================================================
pause
