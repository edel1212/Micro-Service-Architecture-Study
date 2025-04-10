# Service Discovery

```properties
# ℹ️ Spring Cloud Netflix Eureka를 사용하여 구축함
```

## 1 ) Service Discovery
- 마이크로 서비스 위치 정보를 등록 및 검색 용도로 사용함
  - 쉬운 예시 : 전화번호 책
  - Key, Value 형식으로 되어있음

## 2 ) Flow
-  1 . 사용자 요청
-  2 . Load Balancer를 통해 Spring Cloud 접근
-  3 . 사용자의 요청애 맞는 Service Discovery를 찾고 데이터 요청
-  4 . 사용자는 대상 서버에서 데이터를 전달 받음 
![img.png](img.png)

## 3 ) Discovery Server(Eureka Server) 설정 방법

### 3 - 1 ) build.gradle
- 다른 dependencies 추가 필요 없이 **eureka** 하나만 추가해주면 된다. 
```groovy
dependencies {
	implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-server'
}
```

### 3 - 2 ) application.yml
- Eureka Server 그 자체이기에 자기 자신의 위치 정보를 자기자신에 등록할 필요가 없기에 `register-with-eureka`, `fetch-registry`는 **false**
  - 기본값 : true 
```yaml
server:
    port: 8761

spring:
  application:
    name: discoveryservice

# ℹ️ eurek 설정 - Discovery Server 이기에 자기 자신을 등록할 필요가 없기에 false 처리 ( 기본 값 : true )
eureka:
  client:
      register-with-eureka: false
      fetch-registry: false
```

### 3 - 3 ) Application.java
- `@EnableEurekaServer`를 사용해서 해당 Server는 **Eureka Server로 사용 지정**
```java
@EnableEurekaServer
@SpringBootApplication
public class EcoomerceApplication {
	public static void main(String[] args) {
		SpringApplication.run(EcoomerceApplication.class, args);
	}
}
```

## 4 ) Discovery Client(Eureka Client) 설정 방법
[참고](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/eureka-client)

## 5 ) Discover Service 이중화

### 5 - 1 ) Discover Service -  application.yml
- `serviceUrl.defaultZone`를 통해 하위 서버들은 이어져 있게 설정 필요
  - Eureka 클러스터를 구성할 땐 반드시 localhost 대신 실제 IP를 사용해야 함 다만 현제는 불가능 하기에 테스트로 진행
    - 아래의 설정 파일로 기동 시 **서로를 인식 하지만 Replica는 되지 않음**
    - `defaultZone: http://localhost:8761/eureka, http://localhost:8762/eureka, http://localhost:8763/eureka`
```yaml
server:
  port: 8761

spring:
  application:
    name: discoveryservice

# ℹ️ eurek 설정 - Discovery Server 이기에 자기 자신을 등록할 필요가 없기에 false 처리 ( 기본 값 : true )
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
    serviceUrl:
      defaultZone: http://localhost:8761/eureka, http://localhost:8762/eureka, http://localhost:8763/eureka

---
spring:
  config:
    activate:
      on-profile: eureka1

server:
  port: 8762

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka, http://localhost:8762/eureka, http://localhost:8763/eureka
  instance:
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}

---
spring:
  config:
    activate:
      on-profile: eureka2

server:
  port: 8763

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka, http://localhost:8762/eureka, http://localhost:8763/eureka
  instance:
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}

```
- 서버 기동
```shell
# 서버 기동 지정 포트 (8085)
./gradlew bootRun --args='--spring.profiles.active=eureka1'
```
- Discover Service 이중화를 통해 하나의 Discover 서버가 죽더라도 대응이 가능하다.
  - **가용성 증가**
### 5 - 2 ) Discovery Client 적용 
- 사용 가능한 Discovery Service들 등록
```yaml
server:
  port: 7876

spring:
  application:
    name: discover-client

eureka:
  # Eureka와 관련된 설정을 정의합니다.
  client:
    # 현재 애플리케이션을 Eureka 서버에 등록할지 여부를 설정합니다.
    register-with-eureka: true
    # Eureka 서버에서 서비스 정보를 가져올지 여부를 설정합니다.
    fetch-registry: true
    # Eureka 서버의 URL을 설정합니다.
    service-url:
      # Eureka 서버의 기본 주소를 설정합니다.
      defaultZone: http://localhost:8761/eureka, http://localhost:8761/eureka, http://localhost:8763/eureka

  # HeaderBeat 주기 주정
  instance:
    lease-renewal-interval-in-seconds: 5   # Heartbeat 주기 (기본값: 30초)
    lease-expiration-duration-in-seconds: 10 # Heartbeat 없을 때 제거까지 걸리는 시간 (기본값: 90초)
```

