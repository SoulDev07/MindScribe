$ProjectRoot = $PSScriptRoot
if (-not $ProjectRoot) {
    $ProjectRoot = (Get-Location).Path
}

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Starting MindScribe Microservices Platform..." -ForegroundColor Cyan
Write-Host "Project Root: $ProjectRoot" -ForegroundColor Gray
Write-Host "=============================================" -ForegroundColor Cyan

# 1. MongoDB Setup
$mongoInUse = Get-NetTCPConnection -LocalPort 27017 -ErrorAction SilentlyContinue
if ($mongoInUse) {
    Write-Host "MongoDB is already running on port 27017." -ForegroundColor Gray
} else {
    Write-Host "Starting MongoDB..." -ForegroundColor Yellow
    $DbPath = Join-Path $ProjectRoot "data\db"
    if (!(Test-Path $DbPath)) {
        New-Item -ItemType Directory -Force -Path $DbPath | Out-Null
    }
    Start-Process mongod -ArgumentList "--dbpath `"$DbPath`"" -WindowStyle Minimized
}

# 2. Redis Setup
$redisInUse = Get-NetTCPConnection -LocalPort 6379 -ErrorAction SilentlyContinue
if ($redisInUse) {
    Write-Host "Redis is already running on port 6379." -ForegroundColor Gray
} else {
    Write-Host "Starting Redis..." -ForegroundColor Yellow
    Start-Process redis-server -WindowStyle Minimized
}

# 3. Kafka Container Setup
$kafkaStatus = docker ps -a --filter "name=kafka" --format "{{.Status}}" 2>$null
if ($kafkaStatus -like "*Up*") {
    Write-Host "Kafka container is already running." -ForegroundColor Gray
} elseif ($kafkaStatus) {
    Write-Host "Starting existing Kafka container..." -ForegroundColor Yellow
    docker start kafka | Out-Null
} else {
    Write-Host "Creating and starting new Kafka container..." -ForegroundColor Yellow
    docker run -d --name kafka -p 9092:9092 -e KAFKA_NODE_ID=1 -e KAFKA_PROCESS_ROLES=broker,controller -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 -e CLUSTER_ID=MkU3OEVBNTcwNTJENDM2Qk apache/kafka:3.7.0 | Out-Null
}

Write-Host "Waiting 5 seconds for infrastructure to stabilize..." -ForegroundColor Gray
Start-Sleep -Seconds 5

# 4. Launch Microservices
$SentimentDir = Join-Path $ProjectRoot "sentiment-service"

Write-Host "Starting Eureka Server (Port 8761)..." -ForegroundColor Yellow
Start-Process powershell -WorkingDirectory $ProjectRoot -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run -pl eureka-server" -WindowStyle Normal

Write-Host "Waiting 12 seconds for Eureka to start up..." -ForegroundColor Gray
Start-Sleep -Seconds 12

Write-Host "Starting Python Sentiment Service (Port 8083)..." -ForegroundColor Yellow
Start-Process powershell -WorkingDirectory $SentimentDir -ArgumentList "-NoExit", "-Command", "uv run uvicorn app.main:app --port 8083 --host 0.0.0.0" -WindowStyle Normal

Write-Host "Starting API Gateway (Port 8080)..." -ForegroundColor Yellow
Start-Process powershell -WorkingDirectory $ProjectRoot -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run -pl api-gateway" -WindowStyle Normal

Write-Host "Starting Auth Service (Port 8081)..." -ForegroundColor Yellow
Start-Process powershell -WorkingDirectory $ProjectRoot -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run -pl auth-service" -WindowStyle Normal

Write-Host "Starting Journal Service (Port 8082)..." -ForegroundColor Yellow
Start-Process powershell -WorkingDirectory $ProjectRoot -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run -pl journal-service" -WindowStyle Normal

Write-Host "Starting Notification Service (Port 8084)..." -ForegroundColor Yellow
Start-Process powershell -WorkingDirectory $ProjectRoot -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run -pl notification-service" -WindowStyle Normal

Write-Host "Starting Audit Service (Port 8085)..." -ForegroundColor Yellow
Start-Process powershell -WorkingDirectory $ProjectRoot -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run -pl audit-service" -WindowStyle Normal

Write-Host "---------------------------------------------" -ForegroundColor Green
Write-Host "All startup commands successfully dispatched!" -ForegroundColor Green
Write-Host "Please check individual windows for logs." -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green
