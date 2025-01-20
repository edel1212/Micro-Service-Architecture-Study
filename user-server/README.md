# Eureka Client
- Discovery Server( Eureka Server )에 등록 할 각각의 서버를 의미
## 1 ) 설정 방법

### 1 - 1 ) build.gradle
- 각각의 마이크로 서비스들은 RESTFul API로 통신하기에 `boot-starter-web`는 필수
```groovy
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
	annotationProcessor 'org.projectlombok:lombok'
}
```

### 1 - 2 ) application.yml
- application name은 Discovery Server 내에서 식별하는 이름으로 보여지기에 꼭 작성해 주자
- eureka 내 client 설정 필요
  - register-with-eureka :  현재 애플리케이션을 Eureka 서버에 등록 여부를 설정
  - fetch-registry : 다른 MSA Server의 정보를 받아욜지 여부 설정
  - defaultZone : EurekaServer 도메인 지정
```yaml
server:
  port: ${SERVER_PORT}

spring:
  application:
    name: user-server

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
      defaultZone: http://127.0.0.1:8761/eureka

  # HeaderBeat 주기 주정
  instance:
    lease-renewal-interval-in-seconds: 5   # Heartbeat 주기 (기본값: 30초)
    lease-expiration-duration-in-seconds: 10 # Heartbeat 없을 때 제거까지 걸리는 시간 (기본값: 90초)
```
### 1 - 3 ) application.java
- SpringBoot 2.x 버전에서는 명시적으로`@EurekaClient` 지정이 필요했으나 3.x 버전부터는 불필요
```java
@SpringBootApplication
public class UserServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserServerApplication.class, args);
	}
}
```
