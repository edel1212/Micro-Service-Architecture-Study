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
- ☠️ 삽질
  - 버전 호환 문제
    - kafka 및 kafka-connect **버전 다운그레이드**
  > The issue was resolved by downgrading Kafka to version 3.8.x. According to the release notes for Kafka 3.9.0, the SystemTime class was changed to a singleton,   
  > and instead of creating an instance with new SystemTime();, it was modified to obtain the instance by calling the SystemTime.getSystemTime(); method.
  - kafka 외부 접근 불가 문제
    - KAFKA_CFG_LISTENERS - 설정 필요
    - KAFKA_CFG_ADVERTISED_LISTENERS - 설정 필요정
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
    image: bitnami/kafka:3.8.0  # ✅ kafka connector JDBC 이슈로인한 버전 다운
    container_name: kafka
    environment:
      - KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181
      - ALLOW_PLAINTEXT_LISTENER=yes
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9092
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092
    ports:
      - "9092:9092"
    networks:
      - ecommerce-network

  kafka-connector-mariadb:
    image: confluentinc/cp-kafka-connect:7.7.0 # ✅ 호환 문제로 인한 버전 다운
    container_name: kafka-connector
    ports:
      - 8083:8083
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

### 2 - 2 ) Kafka Source Connect
- 외부 데이터베이스나 시스템에서 데이터를 읽어 Kafka 토픽에 실시간으로 전송하는 역할을 하는 Kafka Connect의 구성 요소
  - 데이터베이스, 파일 시스템 등 다양한 소스에서 Kafka로 데이터를 쉽게 가져올 수 있음
  
#### 2 - 2 - A ) Source Connect 등록 
```yaml
# ☠️ 삽질 : config.connector.class 설정 시 "JdbcSourceConnector"로 해줘야한다.. 
#         - "jdbc.JdbcSinkConnector" 로 설정하여 삽질함 사용처가 다름 ... ( 에러가 없어 더 찾기 오래 걸림 )
#         - JdbcSourceConnector : 데이터베이스에서 Kafka로 데이터 읽기 | JdbcSinkConnector : Kafka에서 데이터베이스로 데이터 쓰기
```
- Http Method : POST
- Header : Content-Type - application/json
- Uri : `localhost:8083/connectors`
- Body
```javascript
{
    "name": "my-source-connect",
    "config": {
        "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
        "connection.url": "jdbc:mariadb://local-db:3306/mydb",
        "connection.user": "root",
        "connection.password": "123",
        "mode": "incrementing",
        "incrementing.column.name": "id",
        "table.whitelist": "users",
        "topic.prefix": "my_topic_",
        "tasks.max": "1",
        "poll.interval.ms" : 10000,
        "topic.creation.default.replication.factor":1,
        "topic.creation.default.partitions" : 1
    }
}
```

#### 2 - 2 - B ) Connect 목록 확인 
- Http Method : GET
- Uri : `localhost:8083/connectors/list`

#### 2 - 2 - C ) 지정 Connect 상태 확인
- Http Method : GET
- Uri : `localhost:8083/connectors/{{지정 sourse name}}/status`
- 
#### 2 - 2 - D ) Source Connect 테스트
- 1 . kafka consumer.sh에서 설정한 topic 내용 보기
  - `kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic {{지정 토픽}} --from--beginning`
- 2 . connector로 지정한 Table에 Data Insert
  - `insert into users(user_id, name) values("foo","jeoungho")`
- 3 . topic log 확인


### 2 - 3 ) Kafka Sink Connect
- Kafka 토픽에서 데이터를 읽어 외부 시스템이나 데이터베이스로 전송하는 역할을 하는 Kafka Connect의 구성
  - Kafka에 저장된 데이터를 다양한 목적지 시스템, 예를 들어 데이터베이스, 파일 시스템, 검색 엔진 등에 실시간으로 저장하거나 처리할 수 있음

#### 2 - 3 - A ) Sink Connect 등록
```yaml
# ✅ JdbcSinkConnector 를 등록 해야한다.
#    - "topic" -> "topics" 로 등록해야한다. 
#      - sink 등록 시 해당 topics로 지정된 "Table이 생성"된다.
#
# > 지정한 topics인 "my_topic_users" 테이블이 생성된다. 
#   - source connector에 등록된 topic을 sink connector로 감지해서 해당 값을 그대로 table로 동기화 해주는 것이다
```
- Http Method : POST
- Header : Content-Type - application/json
- Uri : `localhost:8083/connectors`
- Body
```javascript
{
  "name": "my-sink-connect",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector", 
    "connection.url": "jdbc:mariadb://local-db:3306/mydb",
    "connection.user": "root",
    "connection.password": "123",
    "auto.create": "true",
    "auto.evolve": "true",
    "delete.enabled": "false",
    "tasks.max": "1",
    "topics": "my_topic_users"  
  }
}
```

#### 2 - 3 - B ) Sink Connect 테스트
- 1 . sink connector로 생성된 Table에 조회
  - source connector 지정 table과 동기화 되어있는것을 확인 가능  
- 2 . source connector 지정 table에 data insert
  - source connector 지정 table과 동기화 되어있는것을 확인 가능
- 3 . kafka-console-consumer.sh 에서 해당 topic log를 사용해서 `kafka-console-producer`에 주입
  ```javascript
  {
    "type": "string",
    "optional": true,
    "field": "user_id"
  },
  {
    "type": "string",
    "optional": true,
    "field": "pwd"
  },
  {
    "type": "string",
    "optional": true,
    "field": "name"
  },
  {
    "type": "int64",
    "optional": true,
    "name": "org.apache.kafka.connect.data.Timestamp",
    "version": 1,
    "field": "created_at"
  }
  ],
  "optional": false,
  "name": "users"
  },
  "payload": {
    "id": 9,
    "user_id": "add-userd-producer",
    "pwd": null,
    "name": "kafka-producer",
    "created_at": 1741437467000
  }
  ```
- 4 . sink connector로 생성된 Table에 조회 시 해당 값이 추가 되어 있음
  - ✅ **sink table에는 추가되어 있지만 source table에는 추가 되어 있지 않음** => 당연한 결과이다. 
    - sink table : kafka의 topic에서 넘어온 **JSON 기준으로 Table 데이터 생성**
    - source connector 지징 table : 원본 테이블

## 3 ) Kafka using spring-boot

### 3 - 1 ) kafka consumer

#### 3 - 1 - A ) build.gradle
```groovy
dependencies {
  // kafka
  implementation 'org.springframework.kafka:spring-kafka'
  testImplementation 'org.springframework.kafka:spring-kafka-test'
}
```

#### 3 - 1 - B ) kafka consumer config
- `@EnableKafka`를 사용하여 KafkaListener 활성화
  - **Kafka Listener를 자동으로 감지**하고 **관리**할 수 있도록 설정하는 역할을 함
- 사용하기에 StringDeserializer **설정 필수**
```java
@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Bean
    public ConsumerFactory<String, String> consumerFactory(){
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "consumerGroupId");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory
                = new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConsumerFactory(consumerFactory());

        return kafkaListenerContainerFactory;
    }
}
```
#### 3 - 1 - C ) kafka consumer (Message Receive)
- `@KafkaListener(topics = "전달받을 토픽 지정")`을 지정한 메서드의 argument에서 message를 받음
```java
@Service
@RequiredArgsConstructor
@Log4j2
public class KafkaConsumer {
    private final CatalogRepository catalogRepository;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "example-catalog-topic")
    public void updateQty(String kafkaMessage){
        log.info("kafka Message: -> {}", kafkaMessage);
        Map<Object, Object> map = new HashMap<>();
        // String -> JSON
        try {
            map = mapper.readValue(kafkaMessage, new TypeReference<>() {});
        } catch (Exception e){
            e.printStackTrace();;
        } // try - catch

        String productId = (String) map.get("productId");
        log.info("productId :: {}", productId);

        CatalogEntity entity = catalogRepository.findByProductId(productId);
        if(entity != null){
            // update to qty
            int qty = (Integer) map.get("qty");
            entity.setStock(entity.getStock() - qty);
            catalogRepository.save(entity);
        } // if

    }
}
```