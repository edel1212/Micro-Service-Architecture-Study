# Kafka
- Docker Compose 사용하여 진행
```yaml
services:
  zookeeper:
    image: bitnami/zookeeper:latest
    container_name: zookeeper
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes
    ports:
      - "2181:2181"
  kafka:
    image: bitnami/kafka:latest 
    container_name: kafka
    environment:
      - KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181
      - ALLOW_PLAINTEXT_LISTENER=yes
    ports:
      - "9092:9092"
```

## 1 ) 기본 명령어

### 1 - 1 ) 메세지 생성
- 최신 버전(Kafka 2.0 이상)에서는 `--broker-list` 대신 `--bootstrap-server`를 **사용해야 함**
  - Kafka에서 클라이언트(프로듀서/컨슈머)가 Kafka **브로커(서버)와 연결할 때 사용하는 옵션**
    - Kafka 클러스터에 있는 **하나 이상의 브로커(Broker) 주소를 지정해서 클라이언트가 클러스터에 접속**할 수 있도록 함
```shell
kafka-topics.sh --create --topic [ 생성할 topic명 ] --bootstrap-server [ Kafka Broker 도메인 ]
```

### 1 - 2 ) 프로듀스 모드 진입 및 메세지 발송
```shell
kafka-console-producer.sh --bootstrap-server [ Kafka Broker 도메인 ] --topic [ 생성 했던 topic명 ]
```