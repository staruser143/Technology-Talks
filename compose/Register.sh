#!/bin/bash
set -e

echo "⏳ Waiting for Kafka Connect to be ready..."
until curl -s localhost:8083/connectors > /dev/null; do
  sleep 3
done

echo "✅ Kafka Connect is ready. Registering MongoDB connector..."

curl -s -X POST -H "Content-Type: application/json" \
  --data @/etc/kafka-connect/register-mongo-connector.json \
  http://localhost:8083/connectors \
  | jq .