#!/bin/bash

# 현재 스크립트가 실행 중인 디렉토리를 기준으로 상대 경로 설정
BASE_DIR=$(dirname "$0")

# ../../kafka 디렉토리에서 docker-compose 실행
echo "Starting Kafka services..."
(cd "$BASE_DIR/../../kafka" && docker-compose up -d)

# ./docker-compose 디렉토리에서 docker-compose 실행
echo "Starting services in docker-compose directory..."
(cd "$BASE_DIR/docker-compose" && docker-compose up -d)

# ./monitoring 디렉토리에서 docker-compose 실행
echo "Starting monitoring services..."
(cd "$BASE_DIR/monitoring" && docker-compose up -d)

echo "Starting zipkin services..."
(cd "$BASE_DIR/zipkin" && docker-compose up -d)

echo "All services started successfully!"