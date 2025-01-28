# API Gateway

![img.png](img.png)

## 1 ) 주요 기능
- 인증 및 권한 부여
- 서비스 검색 및 통합
- 응답 캐싱
- 정책, 회로 차단기 및 Qos 다시 시도
- 속도 제한
- 부하 분산
- 로깅, 추적, 상관 관계
- 헤더, 쿼리 문자열 및 청구 변환
- IP 허용 목록으로 접근 제어

## 2 ) Spring Cloud Gateway
- **비동기 처리** 지원
```properties
ℹ️ Zuul Project는 Deprecated되어 더이상 사용이 불가능
   - Spring에서도 Spring Cloud Gateway 사용을 권장👍
```

### 2 - 1 ) 설정 - application.yml 사용

#### dependencies
```properties
# ✅ Spring Cloud Gateway는 비동기, 논블로킹 방식으로 동작하는 API Gateway를 제공
#    - 이는 Netty 기반의 비동기 웹 서버를 사용하여 높은 성능과 확장성을 제공함
```
- 😅 살질... 
  - `gateway-mvc`를 사용해서 적용하면 gateway-route가 정상 작동하지 않음 그냥 `gateway`를 사용해야함
  - 이유
    - spring-cloud-starter-gateway는 **Reactive 환경(WebFlux)을 기본**으로 하며, 대부분의 기능은 이 환경에서만 완전하게 동작
      - 특별한 이유가 없는 한 **Reactive 기반**의 Spring Cloud Gateway를 **사용하는 것이 권장**
    - `spring-cloud-starter-gateway-mvc`는 WebFlux 기반의 Spring Cloud Gateway의 설정 방식을 지원하지 않음
      - **application.yml에 작성한 설정이 무시됨**
      - Spring MVC의 @RestController와 @RequestMapping을 사용하여 **라우팅을 구성해야 함**
        - 대상 서비스로 **요청을 포워딩하는 방식**
    - spring-cloud-starter-gateway는 비동기, 논블로킹 방식의 고성능 API Gateway를 제공
```groovy
dependencies {
    // ❌ implementation 'org.springframework.cloud:spring-cloud-starter-gateway-mvc'
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
}
```



#### application.yml
- 핵심 설정은 `cloud` 부분 설정임
- 연결에 필요한 **URI**를 통해 매칭 **URL ❌**
- id 값은 사용하려는 micro service와 달라도 **문제가 없음**  
```yaml
server:
  port: 8000

spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      routes:
        # 라우팅 설정 for first-service
        - id: first-servic
          uri: http://localhost:8081
          predicates:
            - Path=/first-service/**
        # 라우팅 설정 for second-service
        - id: second-serviceㄴ
          uri: http://localhost:8082
          predicates:
            - Path=/second-service/**


eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  service-url:
    defaultZone: http://localhost:8761/eureka/
```

### 2 - 1 ) 설정 - @Configuration 사용
```properties
# ✅ dependencies 설정은 동일
#    - 기존의 설정은 동일하나 application 설정 부분을 Java로 변경
```
#### application.yml
- port 및 eureka 설정 부분 skip 
```yaml
spring:
#  cloud:
#    gateway:
#      routes:
#        - id: first-service
#          uri: http://localhost:8081
#          predicates:
#            - Path=/first-service/**
#        - id: second-service
#          uri: http://localhost:8082
#          predicates:
#            - Path=/second-service/**
```
#### Config class
- Builder Pattern을 사용해서 등록
- path    : Gateway에 들어오는 요청 중 어떠한 path를 route 할지 지정
- filters : 원하는 filter 등록
- uri     : 요청을 넘길  service uri 등록 
```java
@Configuration
public class FilterConfig {
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder routeLocatorBuilder){
        return routeLocatorBuilder.routes()
                .route( r
                            // gateway에 해당 path 요청이 들어올 경우
                            -> r.path("/first-service/**")
                            // 필터 사용
                            .filters( f -> f.addRequestHeader("first-request", "first-request-header")
                                            .addResponseHeader("first-response", "first-response-header"))
                            // 해당 uri로 이동
                            .uri("http://localhost:8081"))
                .route( r -> r.path("/second-service/**")
                        .filters( f -> f.addRequestHeader("second-request", "second-request-header")
                                .addResponseHeader("second-response", "second-response-header"))
                        .uri("http://localhost:8082"))
                .build();
    }
}
```