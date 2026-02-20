#!/bin/bash

echo "🚀 Starting MySQL Observability Demo Stack..."
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

echo "📦 Starting Docker Compose services..."
docker-compose up -d

echo ""
echo "⏳ Waiting for services to be ready..."
echo ""

# Wait for MySQL
echo "🔍 Waiting for MySQL..."
until docker-compose exec -T mysql mysqladmin ping -h localhost -u root -prootpassword --silent 2>/dev/null; do
    echo "   MySQL is starting..."
    sleep 2
done
echo "✅ MySQL is ready!"

# Wait for Prometheus
echo "🔍 Waiting for Prometheus..."
until curl -s http://localhost:9090/-/ready > /dev/null; do
    echo "   Prometheus is starting..."
    sleep 2
done
echo "✅ Prometheus is ready!"

# Wait for Loki
echo "🔍 Waiting for Loki..."
until curl -s http://localhost:3100/ready > /dev/null; do
    echo "   Loki is starting..."
    sleep 2
done
echo "✅ Loki is ready!"

# Wait for Tempo
echo "🔍 Waiting for Tempo..."
until curl -s http://localhost:3200/ready > /dev/null; do
    echo "   Tempo is starting..."
    sleep 2
done
echo "✅ Tempo is ready!"

# Wait for Grafana
echo "🔍 Waiting for Grafana..."
until curl -s http://localhost:3000/api/health > /dev/null; do
    echo "   Grafana is starting..."
    sleep 2
done
echo "✅ Grafana is ready!"

echo ""
echo "=========================================================="
echo "✅ All services are running!"
echo "=========================================================="
echo ""
echo "📊 Access URLs:"
echo "   - Grafana:    http://localhost:3000 (admin/admin)"
echo "   - Prometheus: http://localhost:9090"
echo "   - Loki:       http://localhost:3100"
echo "   - Tempo:      http://localhost:3200"
echo "   - MySQL:      localhost:3306 (demo_user/demo_password)"
echo ""
echo "📝 Next steps:"
echo "   1. Build the application: ./mvnw clean package"
echo "   2. Run the application: ./mvnw spring-boot:run"
echo "   3. Generate some traffic: ./scripts/generate-traffic.sh"
echo "   4. Open Grafana and view the dashboard!"
echo ""
echo "=========================================================="
