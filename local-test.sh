#!/bin/bash

set -e

docker compose down
docker compose up -d db

echo "Waiting for DB to be ready..."
sleep 10

mvn clean test -Dtest=com.par.parapp.controller.GameControllerIntegrationTest

docker compose down
