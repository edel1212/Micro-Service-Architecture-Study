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
# kafka-console-producer.sh --bootstrap-server 127.0.01:9092 --topic test
```

## 2 ) Kafka Connect
> 카프카를 사용하여 외부 시스템과 데이터를 주고 받기 위한 오픈소스 프레임워
- 데이터베이스, 키-값 저장소, 검색 인덱스 및 파일 시스템 간의 **간단한 데이터 통합을 위한 중앙 집중식 데이터 허브 역할** 한다.
  -  Kafka와 다른 데이터 시스템 간에 데이터를 스트리밍하고 Kafka 안팎으로 대규모 데이터 셋을 이동시켜주는 커넥터를 빠르게 생성 가능
- 프로듀서와 컨슈머를 직접 개발해 원하는 동작을 실행하고 처리할 수 있지만, 개발하고 운영하는데 들어가는 리소스나 비용이 부담이 되는 경우 카프카 커넥트를 사용
  - **카프카 커넥트에서 제공하는 REST API를 통해** 빠르고 간단하게 커넥트의 설정을 조정하며 상황에 맞게 **유연하게 대응 가능**

### 2 - 1 ) Kafka Connect Docker
- Volume 설정 시 필요로 하는 자료를 미리 준비해야한다.
  - Mariadb Client ( gradle or maven에서 받은 jar를 복사 후 사용 )
  - confluentinc Jdbc lib ( 공식 홈페이지에서 다운로드 후 lib 위치 jar 사용 )
```yaml
services:
  zookeeper:
    image: bitnami/zookeeper:latest 
    container_name: zookeeper
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes
    ports:
      - "2181:2181"
    networks:
      - ecommerce-network

  kafka:
    image: bitnami/kafka:latest 
    container_name: kafka
    environment:
      - KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181
      - ALLOW_PLAINTEXT_LISTENER=yes
    ports:
      - "9092:9092"
    networks:
      - ecommerce-network

  kafka-connector-mariadb:
    image: confluentinc/cp-kafka-connect:latest
    ports:
      - 8083:8083
    links:
      - kafka
      - zookeeper
    environment:
      CONNECT_BOOTSTRAP_SERVERS: kafka:9092
      CONNECT_REST_PORT: 8083
      CONNECT_GROUP_ID: "quickstart-avro"
      CONNECT_CONFIG_STORAGE_TOPIC: "quickstart-avro-config"
      CONNECT_OFFSET_STORAGE_TOPIC: "quickstart-avro-offsets"
      CONNECT_STATUS_STORAGE_TOPIC: "quickstart-avro-status"
      CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR: 1
      CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR: 1
      CONNECT_STATUS_STORAGE_REPLICATION_FACTOR: 1
      CONNECT_KEY_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_VALUE_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_INTERNAL_KEY_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_INTERNAL_VALUE_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_REST_ADVERTISED_HOST_NAME: "localhost"
      CONNECT_LOG4J_ROOT_LOGLEVEL: DEBUG
      CONNECT_PLUGIN_PATH: "/usr/share/java,/etc/kafka-connect/jars"
    volumes:
      - ./jars:/etc/kafka-connect/jars #jars파일들 volume을 통하여 사용
    networks:
      - ecommerce-network

networks:
  ecommerce-network:
    driver: bridge
```

### 2 - 1 ) Kafka Source Connect 

#### 2 - 1 - A ) Source Connect 등록 
- Http Method : POST
- Header : Content-Type - application/json
- Body
```javascript
{
    "name":"order-sink-connect", // 지정 이름
    "config":{
        "connector.class":"io.confluent.connect.jdbc.JdbcSinkConnector",
        "connection.url":"jdbc:mariadb://mariadb:3306/mydb",
        "connection.user":"root",
        "connection.password":"123",
        "mode": "incrementing",
        "incrementing.column.name" : "id",
        "table.whitelist":"users",
        "topics": "my-topic",
        "topic.prefix" : "my_topic_",
        "tasks.max" : "1"
    }
}
```