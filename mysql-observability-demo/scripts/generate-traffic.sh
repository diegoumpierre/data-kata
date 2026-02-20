#!/bin/bash

echo "🔥 Generating traffic to test observability..."
echo ""

BASE_URL="http://localhost:8080/api"

# Check if application is running
if ! curl -s "${BASE_URL}/users/count" > /dev/null; then
    echo "❌ Application is not running on port 8080"
    echo "   Please start the application first: ./mvnw spring-boot:run"
    exit 1
fi

echo "✅ Application is running"
echo ""

# Function to make random API calls
make_requests() {
    local count=$1
    local delay=$2

    for i in $(seq 1 $count); do
        # Random user operations
        case $((RANDOM % 5)) in
            0)
                echo "📊 GET /users"
                curl -s "${BASE_URL}/users" > /dev/null
                ;;
            1)
                USER_ID=$((RANDOM % 10 + 1))
                echo "📊 GET /users/${USER_ID}"
                curl -s "${BASE_URL}/users/${USER_ID}" > /dev/null
                ;;
            2)
                CITIES=("New York" "Los Angeles" "Chicago" "Houston" "Phoenix")
                CITY=${CITIES[$((RANDOM % ${#CITIES[@]}))]}
                echo "📊 GET /users/city/${CITY// /%20}"
                curl -s "${BASE_URL}/users/city/${CITY// /%20}" > /dev/null
                ;;
            3)
                echo "📊 GET /orders"
                curl -s "${BASE_URL}/orders" > /dev/null
                ;;
            4)
                USER_ID=$((RANDOM % 10 + 1))
                echo "📊 GET /orders/user/${USER_ID}"
                curl -s "${BASE_URL}/orders/user/${USER_ID}" > /dev/null
                ;;
        esac

        sleep $delay
    done
}

echo "🎯 Phase 1: Light load (10 requests)"
make_requests 10 0.5

echo ""
echo "🎯 Phase 2: Medium load (20 requests)"
make_requests 20 0.3

echo ""
echo "🎯 Phase 3: Heavy load (30 requests)"
make_requests 30 0.1

echo ""
echo "🎯 Phase 4: Mixed operations (15 requests)"
for i in $(seq 1 15); do
    # Create new user
    NAME="User_${RANDOM}"
    EMAIL="user${RANDOM}@example.com"
    CITY="TestCity"

    echo "📊 POST /users (creating ${NAME})"
    USER_RESPONSE=$(curl -s -X POST "${BASE_URL}/users" \
        -H "Content-Type: application/json" \
        -d "{\"name\":\"${NAME}\",\"email\":\"${EMAIL}\",\"city\":\"${CITY}\"}")

    # Create order for the user
    if [ ! -z "$USER_RESPONSE" ]; then
        USER_ID=$(echo $USER_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
        if [ ! -z "$USER_ID" ]; then
            echo "📊 POST /orders (for user ${USER_ID})"
            curl -s -X POST "${BASE_URL}/orders" \
                -H "Content-Type: application/json" \
                -d "{\"userId\":${USER_ID},\"product\":\"Test Product\",\"amount\":99.99,\"status\":\"PENDING\"}" > /dev/null
        fi
    fi

    sleep 0.2
done

echo ""
echo "=========================================================="
echo "✅ Traffic generation complete!"
echo "=========================================================="
echo ""
echo "📊 Check the results:"
echo "   - Grafana Dashboard: http://localhost:3000"
echo "   - Prometheus Metrics: http://localhost:9090"
echo "   - Application Metrics: http://localhost:8080/actuator/prometheus"
echo ""
echo "🔍 You should now see:"
echo "   ✓ Query rate graphs populated"
echo "   ✓ Latency percentiles showing data"
echo "   ✓ Top query locations table filled"
echo "   ✓ Logs in Loki with query details"
echo ""
