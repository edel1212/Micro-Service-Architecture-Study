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

## 2 ) Spring Cloud Gateway 란?
```properties
# ℹ️ Zuul Project는 Deprecated되어 더 이상 사용 불가능
#   - 따라서 Spring Cloud Gateway 사용을 권장 (Spring 공식에서도 권장 함)👍
```
- ✅ Spring Cloud Gateway는 **비동기**, 논블로킹 방식으로 동작하는 API Gateway를 제공
  - **비동기 처리** 지원
  - 이는 Netty 기반의 비동기 웹 서버를 사용하여 높은 성능과 확장성을 제공함
- gateway를 통해 호출 될 경우 강제로 header 값 추가가 가능
  - 라우팅 대상 서버에서 특정 header 를 강제하는 방식을 활용해서 차단 가능하다.
    - 단) 망분리가 잘되어있다면 크게 의미가 있지 않음

## 3 ) 설정 방법

### 3 - 1 ) dependencies
```groovy
dependencies {
    // ❌ implementation 'org.springframework.cloud:spring-cloud-starter-gateway-mvc'
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
}
```

#### 😅 살질.. 
- 원인 
  - `gateway-mvc` 적용 시 gateway-route가 정상 작동하지 않음
  - spring-cloud-starter-gateway는 **Reactive 환경(WebFlux)을 기본**으로 하며, 대부분의 기능은 이 환경에서만 완전하게 동작함
    - 특별한 이유가 없는 한 **Reactive 기반**의 Spring Cloud Gateway를 **사용하는 것이 권장**
  - `gateway-mvc`는 WebFlux 기반의 Spring Cloud Gateway의 설정 방식을 **지원하지 않음**
    - **application.yml에 작성한 설정이 무시됨**
    - Spring MVC의 @RestController와 @RequestMapping을 사용하여 **라우팅을 구성해야 함**
      - 대상 서비스로 **요청을 포워딩하는 방식**
    - spring-cloud-starter-gateway는 비동기, 논블로킹 방식의 고성능 API Gateway를 제공
- 해결 방법
  - `starter-gateway`사용

### 3 - 2 )  application.yml 방법 
```properties
# ✨ Gate-way 와 연결된 Service의  predicates의 Path를 같게 맞춰줘야 Routing이 가능하다
```
- 핵심 설정은 `spring.cloud.gateway` 하위 설정
- id : **"-"를 사용 배열 방식 등록 함** 대상의 `route id(식별할 Id)` 지정
  - `route id`는 대상 application service name 과 달라도 **문제가 없음**  
- uri :  요청을 넘길 service uri
  - 연결에 필요한 **URI**를 통해 매칭  ( 주의 사항 : ☠️  **URL❌** -> **URI🙆** )
- predicates : gateway가 전달 route 할 path 방식 설정
- filters : gateway에서 필터링할 방법 지정
```yaml
server:
  port: 8000

spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      routes:
        - id: first-service
          uri: http://localhost:8081
          predicates:
            - Path=/first-service/**
          filters:
            - AddRequestHeader=first-request, first-request-header-using-yml-file
            - AddResponseHeader=first-response, first-response-header-using-yml-file
        - id: second-service
          uri: http://localhost:8082
          predicates:
            - Path=/second-service/**
          filters:
            - AddRequestHeader=second-request, second-request-header-using-yml-file
            - AddResponseHeader=second-response, second-response-header-using-yml-file
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  service-url:
    defaultZone: http://localhost:8761/eureka/
```

### 3 - 3 ) Java 설정 방식 - @Configuration 사용
```properties
# ✅ dependencies 설정은 동일
#    - 기존의 설정은 동일하나 application 설정 부분을 Java로 변경
```
#### application.yml
- port 및 eureka 설정 부분은 사용하지 않으므로 삭제 또는 주석 처리 진행
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

## 4 ) Gateway CustomFilter
```properties
# ℹ️ 해당 필터는 원하는 라우터 정보에 "개벌적"으로 등록해 줘야함
```
- `AbstractGatewayFilterFactory`를 상속하여 진행함
  - 추상 클래스이므로 `GatewayFilter apply`**구현을 강제함**
- 람다식으로 구성되며 `(exchange, chain)`는 **핵심 컴포넌트**이다.
  - exchange (ServerWebExchange)
    - 타입: ServerWebExchange
    - 의미: 현재 HTTP **요청과 응답에 대한 모든 정보 및 세션 정보**를 담고 있는 객체 
    - 요청/응답 수정: 요청 헤더, 응답 헤더 등을 **수정할 수 있음**
  - chain (GatewayFilterChain)
    - 타입: GatewayFilterChain
    - 의미: **현재 필터 체인을 나타내는 객체**입니다.
      - 필터 체인은 여러 필터가 순차적으로 실행되는 구조를 의미함
    - 다음 필터 호출: chain.filter(exchange)를 호출하여 **다음 필터로 요청을 전달**
    - 필터 체인 종료: chain.filter(exchange)를 호출하지 않으면 **필터 체인이 종료**
- 체인형식을 **return 전**에는 PreFilter 로직 작성할 수 있고 **return 할때**는 PostFilter 로직을 작성 할 수 있다.
  - return 시 비동기 통신을 하기에 **Mono or Flux**를 사용해 반환

### 4 - 1 ) CustomFilter Class
- Filter에서 사용될 name을 변경을 원할 경우 `public String name()`를 Override 구현 필요
  - Default Name은 **해당 설정 Class의 이름을 따름**
```java
@Log4j2
@Component
public class CustomFilter extends AbstractGatewayFilterFactory{

//  @Override
//  public String name() {
//    return "해당 문자열 내 변경 이름 작성";
//  }

  @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            // PreFilter Business Logic 적용 가능
            ServerHttpRequest serverHttpRequest   = exchange.getRequest();
            ServerHttpResponse serverHttpResponse = exchange.getResponse();
            log.info("Pre Filter - Request Id is ? ::: {}", serverHttpRequest.getId());

            return chain.filter(exchange).then(Mono.fromRunnable(()->{
                // PostFilter Business Logic 적용 가능
                log.info("Post Filter - Http Status ? :: {}", serverHttpResponse.getStatusCode());
            }));
        } ;
    }
}
```

### 4 - 2 ) Application.yml - 설정 방법
- 이전 설정과 전부 동일 **filters** 부분 내 Custom Class Name만 추가 
  - Default Custom Class Name은 **설정한 Class Name 과 같음**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: first-service
          uri: http://localhost:8081
          predicates:
            - Path=/first-service/**
          filters:
            - AddRequestHeader=first-request, first-request-header-using-yml-file
            - AddResponseHeader=first-response, first-response-header-using-yml-file
            - CustomFilter
        - id: second-service
          uri: http://localhost:8082
          predicates:
            - Path=/second-service/**
          filters:
            - AddRequestHeader=second-request, second-request-header-using-yml-file
            - AddResponseHeader=second-response, second-response-header-using-yml-file
            - CustomFilter
```

### 4 - 3 ) ConfigFilter Class - 설정 방법
- CustomFilter를 의존성 주입 후 사용하면 된다.
- apply() 메서드의 매개변수로 `new Object()`가 아닌 실제 필요한 구성 객체를 전달하고 싶다면 해당 Custom Filter class 내 제네릭 지정 생성자 등록 필요

```java
@RequiredArgsConstructor
@Configuration
public class FilterConfig {

    private final CustomFilter customFilter;

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder routeLocatorBuilder){
        return routeLocatorBuilder.routes()
                .route( r
                            -> r.path("/first-service/**")
                            .filters( f -> f.addRequestHeader("first-request", "first-request-header")
                                            .addResponseHeader("first-response", "first-response-header")
                                    .filter(customFilter.apply(new Object())))
                            .uri("http://localhost:8081"))
                .build();
    }
}
```

## 5 ) Gateway GlobalFilter
```properties
# ℹ️ Custom Filter와 비교해서 "가장 먼저" 시작하고 "가장 마지막"에 종료한다.
```

### 5 - 1 ) GlobalFilter Class
- 기존 CustomFilter와 구조는 크게 **다르지 않음**
- `apply(Object object)` -> `apply(GlobalFilter.Config config)`가 다름
  - 실제 **필요한 구성 객체를 전달**하는 방법을 사용
- #### 적용 방법 
  - 1 . `AbstractGatewayFilterFactory<T>` 해당 Generic에 구성체 Class 지정
  - 2 . 생성자 사용하여 부모 추상 클래스에 구성 객체 Class를 `super()`로 주입
  - 3 . 해당 구성채 Class의 값 주입은 **application.yml 혹은 Java Code 통해 주입 가능**
```java
@Log4j2
@Component
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config>{

    public GlobalFilter(){ super(GlobalFilter.Config.class); }

    @Override
    public GatewayFilter apply(GlobalFilter.Config config) {
        return (exchange, chain) -> {
            // PreFilter Business Logic 적용 가능
            ServerHttpRequest serverHttpRequest   = exchange.getRequest();
            ServerHttpResponse serverHttpResponse = exchange.getResponse();
            log.info("Global Pre Filter - baseMessage ? ::: {}", config.getBaseMessage());
            if(config.isPreLogger()){
                log.info("Global Filter Start -> request Id ? ::: {}",serverHttpRequest.getId());
            }
            return chain.filter(exchange).then(Mono.fromRunnable(()->{
                if(config.isPostLogger()){
                    log.info("Global Post Filter End - Http Status ? :: {}", serverHttpResponse.getStatusCode());;
                }
            }));
        } ;
    }

    @Data
    public static class Config{
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
```

### 5 - 0 ) default-filters 와 global-filter 비교
| 특성                     | `default-filters`                        | `GlobalFilter`                            |
|------------------------|-----------------------------------------|------------------------------------------|
| **설정 위치**             | `application.yml` 또는 `application.properties`에서 설정 | Java 코드에서 `@Bean`으로 설정           |
| **적용 범위**             | 모든 라우트에 자동 적용                 | 모든 라우트에 전역적으로 적용           |
| **설정 방식**             | YAML 파일에서 선언                     | Java 클래스에서 `@Bean`을 통해 선언     |
| **우선순위**               | 우선순위를 명시적으로 설정할 수 없음    | `getOrder()` 메서드를 통해 우선순위 설정 가능 |


### 5 - 1 ) Application.yml - 설정 방법
- `default-filters`를 통해 지정
  - `args`를 사용하여 필요 구성 객체 내 필드 값 주입
```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - name: GlobalFilter
          args:
            baseMessage: Spring Cloud Global Filter Setting - using yml
            preLogger: true
            postLogger: true
      routes:
        - id: first-service
          uri: http://localhost:8081
          predicates:
            - Path=/first-service/**
          filters:
            - CustomFilter
```

### 5 - 2 ) ConfigFilter Class - 설정 방법
- `GlobalFilter`를 사용해 지정
  - default-filters와 순서가 다르게 진행 CustomFilter -> GlobalFilter 순서로 진행
  - 변경이 필요할 경우 순서를 바꿔서 진행하도록 함
    - [참고](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway/global-filters.html)
```java
@RequiredArgsConstructor
@Configuration
public class FilterConfig {

  private final CustomFilter customFilter;

  @Bean
  public RouteLocator gatewayRoutes(RouteLocatorBuilder routeLocatorBuilder){
    return routeLocatorBuilder.routes()
            .route( r
                    // gateway에 해당 path 요청이 들어올 경우
                    -> r.path("/first-service/**")
                    // 필터 사용
                    .filters( f -> f.addRequestHeader("first-request", "first-request-header")
                            .addResponseHeader("first-response", "first-response-header")
                            .filter(customFilter.apply(new Object())))
                    // 해당 uri로 이동
                    .uri("http://localhost:8081"))
            .route( r -> r.path("/second-service/**")
                    .filters( f -> f.addRequestHeader("second-request", "second-request-header")
                            .addResponseHeader("second-response", "second-response-header")
                            .filter(customFilter.apply(new Object()))) // CustomFilter 적용
                    .uri("http://localhost:8082"))
            .build();
  }

  @Bean
  public GlobalFilter globalFilterConfig() {
    return (exchange, chain) -> {
      // PreFilter Business Logic 적용 가능
      ServerHttpRequest serverHttpRequest   = exchange.getRequest();
      ServerHttpResponse serverHttpResponse = exchange.getResponse();
      log.info("Global Pre Filter - baseMessage ? ::: {}");
      return chain.filter(exchange).then(Mono.fromRunnable(()->{
        log.info("Global Post Filter End - Http Status ? :: {}", serverHttpResponse.getStatusCode());;
      }));
    } ;
  }
  
}
```

## 6 ) OrderedGatewayFilter - 순서 지정 필터
- GatewayFilter는 Interface이며 해당 OrderedGatewayFilter는 해당 **Interface를 구현한 class**다
  - 생성자 메서드 내 요구 파타미터 `new OrderedGatewayFilter( GlobalFilter, int )`
    - 첫번째 인자 값 구현 메서드 : `Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain);`
    - 두번째 인자 값 : 인트
- 생성자 마지막 메서드 마지막 인자의 값에 따라 **Filter 순서를 지정** 할 수 있다.
  - global-filter 보다도 먼저 작동 하게 설정 가능하다

### 6 - 1 )  OrderedGatewayFilter Class
```java
@Log4j2
@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config>{

    public LoggingFilter(){
        super(LoggingFilter.Config.class);
    }

    @Override
    public GatewayFilter apply(LoggingFilter.Config config) {
        // 1 . 첫번째 인자는 Interface에서 요구하는 함수 인 filter()를 주입
        GatewayFilter gatewayFilter = new OrderedGatewayFilter( (exchange, chain) -> {
            ServerHttpRequest serverHttpRequest   = exchange.getRequest();
            ServerHttpResponse serverHttpResponse = exchange.getResponse();
            log.info("Logging Pre Filter - baseMessage ? ::: {}", config.getBaseMessage());
            if(config.isPreLogger()){
                log.info("Logging Filter  -> request Id ? ::: {}",serverHttpRequest.getId());
            }
            return chain.filter(exchange).then(Mono.fromRunnable(()->{
                if(config.isPostLogger()){
                    log.info("Logging Post Filter - Http Status ? :: {}", serverHttpResponse.getStatusCode());;
                }
            }));
            // 2 . 순서 지정
        }, Ordered.HIGHEST_PRECEDENCE );

        return gatewayFilter;
    }

    @Data
    public static class Config{
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
```

### 6 - 2 ) application.yml 사용 방식
- args를 사용해서 값을 전달 하기 위해서는 `name:`를 **꼭 사용해서 Bean 이름을 지정**해 줘야함
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: second-service
          uri: http://localhost:8082
          predicates:
            - Path=/second-service/**
          filters:
            # args를 적용하기 위햐서는 "name:"을 사용해줘야 함
            - name: CustomFilter
            - name: LoggingFilter
              args:
                baseMessage: Hi~~~~ Logging Filter Setting - using yml
                preLogger: true
                postLogger: true
```

## 7 ) 필터 활용 - cookie 및 middle path 제외 
```properties
# ✅ Gateway에서 다른 Service를 route를 하기 위해서 필요한 중간 Path를 사용해야 하지만 filter 중 "RewritePath" 를 사용하면 제외가 가능하다. 
#    - "변경 전" 설정 일부
#        - id: user-service
#          uri: lb://USER-SERVICE
#          predicates:
#            - Path=/user-service/**
```
### 7 - 1 ) [gateway service]  application.yml 적용
- 흐름
  - Gateway에 지정된 Path 와 http Method 요청이 들어온 온다.
    - **predicates 내** Path 확인
  - RemoveRequestHeader 대상인 Cookie **제거**
  - RewritePath 를 통해 **지정 중간 Path를 제거**
    - ex) `/user-service/user-info` -> `/user-info`
  - routes 내 uri 대상으로 해당 **변경된 URI 전달**
```yaml
spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            # 지정 Path
            - Path=/user-service/login
            # POST - login 요청이 들어 욜 경우
            - Method=POST
          # Cookie 삭제 및 user-service 제거
          filters:
            - RemoveRequestHeader=Cookie
            - RewritePath=/user-service/(?<segment>.*), /$\{segment}
```

### 7 - 2 ) [ route target service ] -  User Application Controller
- gateway에서 middle path를 제외하고 route 하기에 `@RequestMapping`가 **불필요**

```java
import org.springframework.web.bind.annotation.RequestMapping;
 
// ❌ 불필요 -> @RequestMapping("/user-service")
@RestController
public class UserController {
  @GetMapping("/health-check")
  public ResponseEntity<String> status() {
    return ResponseEntity.ok("It's Working in User Service");
  }
}
```

## 8 ) Load Balancing - Eureka 연동 
```yaml
# ℹ️ 기본적으로 Eureka Discover Server가 기동되어 있어야 함
#    - Gateway server 및 rote 대상 Server들은 Eureka Client 사용 설정이 되어 있어야 한다.
```
- Route 대상이 되는 **Port를 Random**으로 해도 Gateway에서는 해당 Service를 Discovery Service에서 할당한 Name으로 찾기에 문제가 없다.
- Gateway Server 자체적으로 로드 밸런싱을 지원하며, 같은 이름의 Service가 여러개의 포트로 연결 되었을 경우 **라운드 로빈 방식으로 분산**한다.
  - 무중단 배포를 원할 경우 **heartbeat 주기를 짧게** 잡아 **Discovery Service에서 제외** 시켜줘야 한다.

### 8 - 1 ) [ route target services ] - application.yml
- Random port 적용
  - 그에 따른 Discovery Service 적용을 위함 instance-id 변경
```yaml
server:
  port: 0

spring:
  application:
    name: yoo-first-service

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
  service-url:
    defaultZone: http://localhost:8761/eureka
  instance:
    # instance-id 설정
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}
```

###  8 - 2 ) [ gateway server ] - application.yml
- uri **지정 방식이 변경** 된다.
  - 기존 http://ip:port -> lb://**대상서버의 application name**  
```yaml
spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      routes:
        - id: first-service
          uri: lb://YOO-FIRST-SERVICE
          predicates:
            - Path=/first-service/**
          filters:
            - CustomFilter

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
  service-url:
    defaultZone: http://localhost:8761/eureka
```