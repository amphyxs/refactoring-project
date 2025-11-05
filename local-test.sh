#!/bin/bash

set -e

docker compose down -v
docker compose up -d db

echo "Waiting for DB to be ready..."
sleep 10

mvn clean test

docker compose down -v
