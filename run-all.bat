@echo off
echo ===================================================
echo Starting Waffor Application Services...
echo ===================================================

:: 0. Start MySQL Database if not running
echo [0/4] Checking MySQL Database on port 3307...
netstat -ano | findstr ":3307" >nul
if %errorlevel% neq 0 (
    echo MySQL is not running on port 3307. Starting it automatically...
    if exist C:\wamp\bin\mysql\mysql5.6.12\bin\mysqld.exe (
        start "MySQL Database Server (3307)" /min "C:\wamp\bin\mysql\mysql5.6.12\bin\mysqld.exe" --port=3307
        timeout /t 4 >nul
    ) else (
        echo [WARNING] MySQL executable not found. Please start your MySQL server on port 3307 manually.
    )
) else (
    echo MySQL is already running on port 3307.
)

:: 1. Start ActiveMQ Message Broker
echo [1/4] Starting ActiveMQ Broker...
start "ActiveMQ Broker" cmd /c "activemq-dist\apache-activemq-6.1.2\bin\activemq.bat start"

:: Wait a few seconds for ActiveMQ to initialize
echo Waiting for ActiveMQ to initialize...
timeout /t 5 >nul

:: 2. Start Backend Microservices
echo [2/4] Starting Backend Spring Boot Microservices...
start "Order Service (8081)" cmd /c "mvnw.cmd spring-boot:run -pl order-service"
start "Payment Service (8082)" cmd /c "mvnw.cmd spring-boot:run -pl payment-service"
start "Kitchen Service (8083)" cmd /c "mvnw.cmd spring-boot:run -pl kitchen-service"
start "Delivery Service (8084)" cmd /c "mvnw.cmd spring-boot:run -pl delivery-service"

:: 3. Start React Frontend
echo [3/4] Starting React Frontend...
start "React Frontend" cmd /c "cd react-frontend && npm run dev"

echo ===================================================
echo All services have been launched in separate windows!
echo Make sure your local MySQL server is running on port 3307.
echo ===================================================
pause
