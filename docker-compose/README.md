# Docker Compose 배포

- 참고 : docker network 사용 : `micro-service-network`
  - `docker network create micro-service-network`
  - **각기 다른 docker-compose 파일을 사용**해서 구동하기에 network를 미리 만들어서 사용

## 1 ) 전체 디렉토리 구조
```text
root/
│
├── kafka/
│   └── docker-compose.yml
│   └── jar/ # kafka-connect에 사용될 jar 모음
│
│ ✅ 해당 dir 부터 시작 ./Project/Micro-Service-Architecture-Study
│
├── docker-compose/           
│   └── docker-compose.yml
│   └── mariadb-volume/
│   └── rabbitmq-volume/
│
├── zipkin/
│   └── docker-compose.yml
│
├── monitoring/
│   └── docker-compose.yml
│   └── prometheus.yml
│
├── api-gateway/
│   └── gateway-server/
│       └── Dockerfile
│       └── src/
│
├── config-service/
│   └── Dockerfile
│   └── src/
│
├── user-service/
│   └── Dockerfile
│   └── src/
│
├── order-service/
│   └── Dockerfile
│   └── src/
│
├── catalog-service/
│   └── Dockerfile
│   └── src/
```

## 2 ) 진행간 이슈 내용

### 2 - 2 ) docker-network 관련
- 💬 이슈 내용 : 한개의 docker compose를 나눠서 서버 기동 시 network 연결 관련
- ✅ 원인 및 해결
  - docker network를 미리 생성 후 compose 내에서 등록 되어있는 docker-network 사용
    - ```yaml
      networks:
        micro-service-network:
        external: true  # 이미 생성된 네트워크 사용    
      ```

### 2 - 2 ) eureka 등록 관련
- 💬 이슈 내용 : eureka 등록 시 container로 지정했지만 계속해서 **등록 요청을 localhost로 보냈던 이슈**
- 😩 예상 원인 : 처음에는 application 분리로 인한 이슈로 예상 했으나, 분리를 해도 크게 상관 없다. (개발 방식에만 맞게 진행)
  - **( 지정 target-?.yml -> target.yml -> application.yml)** 순서로 적용 하기 떄문 **override 함**
- ✅ 원인 및 해결
  - **위치 지정**을 잘해주면 됨 ( ☠️ **"user-service" 내 Eureka 등록 시 삽질**)
    - `eureka.client.service-url.defaultZone : ${eureka-server-uri}/eureka` 형식이다. 개행 및 **suffix** 주의하자!
      - ☠️ 잘못 지정 시 에러가 아닌 default 값으로 지정되어 삽질함.. 
        - **계속 localhost:8761/eureka로 등록 요청을 보냄**

### 2 - 3 ) config-service 사용 관련
- 💬 이슈 내용 : config service를 사용하는 service에서도 정상적으로 config service에 요청을 보낼 수 있으나 **설정 값을 받아오지 못하는 이슈**
  - container 내에서 curl를 통해 해당 config service에 post 요청 시 정상 응답을 받음
- 😩 예상 원인 : 처음에는 application 분리로 인한 이슈로 예상  
- ✅ 원인 및 해결
  - config service uri 등록 시 prefix를 빼먹음.. 꼭 붙여주자
    - `spring.config.import: optional:configserver:${config-service uri}` 형식
  - 서버가 완벽하게 기동하기 전 요청을 보냈기 때문임 docker-compose 내 `depends_on:`로 지정해도 **순서만 보장**할 뿐 요청을 서버가 온전한 상태를 기다려주 않음 
    - config-service를 사용하려는 서버에서 `refresh 또는 busrefresh`를 사용해 재요청을 통해 값을 정상적으로 받아올 수 있음
⭐️ 참고 : 대상 서버에 지정 profile은 config-service에 요청하는 profile에 같은 값이 보내지므로 주의하자.
    - 요약 : application.yml 과 application-*.yml에 profiles.active를 2번 지정 불가능함

### 2 - 4 ) kafka 연결 관련
- 💬 이슈 내용 : 같은 docker-network를 공유 중인데도 **연결이 불가능했던 이슈**
- ✅ 원인 및 해결
  - kafka를 사용하려는 service에서 요청 uri를 `외부용 port -> 내부용 port`로 변경 (9092 -> 19092)
    - 참고 : `KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:19092,EXTERNAL://${DOCKER_HOST_IP:-127.0.0.1}:9092`
    - `advertised.listeners`를 통해 “내가 메시지를 주고받을 브로커 주소는 여기야” 라고 알려주는 메커니즘이다.
