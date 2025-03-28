#!/bin/bash

# ../../kafka 디렉토리 내 docker-compose 종료
(cd ../../kafka && docker-compose down)

# ./docker-compose/ 디렉토리 내 docker-compose 종료
(cd ./docker-compose && docker-compose down)

# ./monitoring 디렉토리 내 docker-compose 종료
(cd ./monitoring && docker-compose down)

(cd ./zipkin && docker-compose down)

echo "All services have been stopped."