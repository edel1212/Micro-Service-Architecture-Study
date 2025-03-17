# CircuitBreaker
- 개요
  - 마이크로서비스 아키텍처(MSA) 환경에서 **장애가 발생했을 때 시스템 전체에 영향을 주지 않도록 보호하는 패턴**이다.
    - 특정 서비스가 오류가 발생하거나 응답 속도가 느려질 경우 자동으로 요청을 차단하여 장애 전파를 방지
    - 일정 시간 동안 요청을 차단한 후, 서비스가 정상적으로 복구되었는지 확인하는 Half-Open 상태를 제공
- Circuit Breaker 3가지 주요 상태

| 상태       | 설명 |
|------------|------------------------------------------------|
| **Closed** | ✅ **정상 상태**로, 모든 요청을 허용 |
| **Open**   | ❌ **오류 비율이 임계값을 초과하여 모든 요청 차단** |
| **Half-Open** | 🔄 **일정 시간이 지난 후 일부 요청을 허용**하여 **서비스 복구 여부 확인** |

- Circuit Breaker 기능

| 기능 | 설명 |
|------------|---------------------------------------------|
| **오류 발생 시 연결 차단** (Circuit Breaking) |  특정 서비스의 장애 감지 시, **해당 서비스로의 요청을 차단하여 장애 전파 방지** |
| **Fallback (대체 처리 제공)** | 특정 서비스가 실패 시, **미리 정의한 대체 응답(Fallback)을 반환** |
| **Rate Limiting (요청 제한)** | 특정 서비스가 과부하 상태가 되면, **일정량 이상의 요청을 제한** |
| **Bulkhead (동시 요청 제한)** |  특정 서비스가 **한 번에 너무 많은 요청을 처리하지 않도록 제한** |
| **Timeout 처리** | 특정 서비스의 응답이 **너무 오래 걸릴 경우 자동으로 요청을 중단** |


## 1 ) Resilience4j
- Java 애플리케이션에서 마이크로서비스의 **회복력(Resilience)을 향상시키기 위해 사용**되는 경량 라이브러리
  - 가볍고 모듈화된 구조 (필요한 기능만 선택하여 사용 가능)
  - Spring Boot와 쉽게 통합 가능 (resilience4j-spring-boot 제공)

## 2 ) 기본 설정으로 사용

### 2 - 1 ) build.gradle
```groovy
dependencies {
	implementation 'org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j'
}
```

### 2 - 2 ) BusinessLogic - ServiceImpl
- CircuitBreaker 객체를 생성 시 `.create("아무거나 지정 가능")` 값은 다른 설정을 적용하고 관리하기 위해서 사용된다.
  - `aplication.yml`에서 설정을 정의할 수 있으며, **같은 이름을 가진 서킷 브레이커는 같은 설정을 공유**
- `.run()`을 사용 시 첫번째 argument는 `Supplier<T>`이며, 두번쨰는 `Function<Throwable, R>`이다.
```java
@RequiredArgsConstructor
@Log4j2
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrderServiceClient orderServiceClient;
    // ✅ CircuitBreakerFactory 의존성 주입
    private final CircuitBreakerFactory circuitBreakerFactory;

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        UserDto userDto = mapper.convertValue(userEntity, UserDto.class);

        // 1 . CircuitBreaker 객체를 생성
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
        // 2 . circuitBreaker의 run()를 사용해 필요한 값과 실패 했을 경우 대체 처리 방법을 지정
        List<ResponseOrder> orders  = circuitBreaker.run( () -> orderServiceClient.getOrders(userId)
                                        , trouble -> new ArrayList<>() );
        userDto.setOrders(orders);

        return userDto;
    }

}
```
## 3 ) Resilience4j Config 
- `Customizer`를 통해 모든 서킷 브레이커(CircuitBreaker) 에 동일한 설정이 적용됨
  - `private final CircuitBreakerFactory circuitBreakerFactory;`
- ℹ️ Reactive 형식으로 진행하고 싶다면 `Customizer<ReactiveResilience4JCircuitBreakerFactory>`로 설정 필요
```java
@Configuration
public class Resilience4JConfig {
    @Bean
    Customizer< Resilience4JCircuitBreakerFactory > globalCustomConfiguration(){

        /**
         * CircuitBreaker 설정
         * - failureRateThreshold(4): 실패율이 4%를 초과하면 서킷을 열어 요청 차단
         * - waitDurationInOpenState(1000ms): 서킷이 열린 후 1초 동안 요청 차단 후 반개 상태로 전환
         * - slidingWindowType(COUNT_BASED): 실패율을 계산할 때 고정된 요청 개수 기준 사용
         * - slidingWindowSize(2): 최근 2개의 요청을 기준으로 서킷 브레이커 동작 결정
         */
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(4)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(2)
                .build();

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                // timeoutDuration(4초): 요청이 4초 이상 걸리면 자동으로 타임아웃 발생
                .timeoutDuration(Duration.ofSeconds(4))
                .build();


        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(timeLimiterConfig)
                .circuitBreakerConfig(circuitBreakerConfig)
                .build()
        );
    }
}
```
## 4 ) CircuitBreaker 상태 확인

- ### application.yml
  - Actuator 설정 내 circuitbreakers 활성화
```yaml
management:
  # Actuator 설정
  endpoints:
    web:
      exposure:
        # /actuator/** 로 사용할 기능 설정
        include:  circuitbreakers

  # Zipkin 설정
  tracing:
    sampling:
      probability: 1.0 # 샘플링할 비율 - 100% 샘플링 (모든 요청을 추적)
    propagation:
      consume: B3
      produce: B3_MULTI
  zipkin:
    tracing:
      endpoint: "http://localhost:9411/api/v2/spans" # Zipkin 서버 주소
```

- ### Link
  - `도메인/actuator/circuitbreakers`을 통해 상태확인 가능
    ```javascript
    {
      "circuitBreakers": {
        "order-service": {
          "failureRate": "-1.0%",
          "slowCallRate": "-1.0%",
          "failureRateThreshold": "4.0%",
          "slowCallRateThreshold": "100.0%",
          "bufferedCalls": 2,
          "failedCalls": 1,
          "slowCalls": 0,
          "slowFailedCalls": 0,
          "notPermittedCalls": 0,
          "state": "HALF_OPEN"
        }
      }
    }      
    ```






