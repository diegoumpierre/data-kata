#!/bin/bash

echo "🛑 Stopping MySQL Observability Demo Stack..."
echo ""

docker-compose down

echo ""
echo "✅ All services stopped!"
echo ""
echo "💡 To remove all data volumes, run:"
echo "   docker-compose down -v"
echo ""
