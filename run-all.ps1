Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "Starting Waffor Application Services..." -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

# 0. Start MySQL Database if not running
Write-Host "[0/4] Checking MySQL Database on port 3307..." -ForegroundColor Green
$connection = Get-NetTCPConnection -LocalPort 3307 -ErrorAction SilentlyContinue
if ($null -eq $connection) {
    Write-Host "MySQL is not running on port 3307. Starting it automatically..." -ForegroundColor Yellow
    if (Test-Path "C:\wamp\bin\mysql\mysql5.6.12\bin\mysqld.exe") {
        Start-Process "C:\wamp\bin\mysql\mysql5.6.12\bin\mysqld.exe" -ArgumentList "--port=3307" -WindowStyle Hidden
        Start-Sleep -Seconds 4
    } else {
        Write-Host "[WARNING] MySQL executable not found. Please start your MySQL server on port 3307 manually." -ForegroundColor Red
    }
} else {
    Write-Host "MySQL is already running on port 3307." -ForegroundColor Green
}

# 1. Start ActiveMQ
Write-Host "[1/4] Starting ActiveMQ Broker..." -ForegroundColor Green
Start-Process cmd.exe -ArgumentList "/k activemq-dist\apache-activemq-6.1.2\bin\activemq.bat start"

# Wait for ActiveMQ
Write-Host "Waiting for ActiveMQ to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# 2. Start Backend Microservices
Write-Host "[2/4] Starting Backend Spring Boot Microservices..." -ForegroundColor Green
Start-Process cmd.exe -ArgumentList "/k .\mvnw.cmd spring-boot:run -pl order-service"
Start-Process cmd.exe -ArgumentList "/k .\mvnw.cmd spring-boot:run -pl payment-service"
Start-Process cmd.exe -ArgumentList "/k .\mvnw.cmd spring-boot:run -pl kitchen-service"
Start-Process cmd.exe -ArgumentList "/k .\mvnw.cmd spring-boot:run -pl delivery-service"

# 3. Start React Frontend
Write-Host "[3/4] Starting React Frontend..." -ForegroundColor Green
Start-Process cmd.exe -ArgumentList "/k cd react-frontend && npm run dev"

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "All services have been launched in separate console windows!" -ForegroundColor Cyan
Write-Host "Make sure your local MySQL server is running on port 3307." -ForegroundColor Yellow
Write-Host "===================================================" -ForegroundColor Cyan
