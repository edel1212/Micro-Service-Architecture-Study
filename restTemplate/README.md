# RestTemplate

## 1 ) Config Class
- Spring Boot 2.3 이상에서는 RestTemplate가 spring-boot-starter-web에 포함되어 있기에 dependency를 추가할 필요가 없음
```java
@Configuration
public class AppConfig {

    // ⭐️ 해당 어노테이션 사용 시 application.yml에서 불러오는 도메인 주소가 Eureka에 등록된 값으로 불러올 수 있음
    //   - 단 Gateway를 거치지 않음
    @LoadBalanced
    @Bean
    public RestTemplate getRestTemplate() {
        int TIMEOUT = 5000;
        RestTemplate restTemplate = new RestTemplateBuilder()
                .connectTimeout(Duration.ofMillis(TIMEOUT))
                .readTimeout(Duration.ofMillis(TIMEOUT))
                .build();
        return restTemplate;
    }
}
```
### 1 - 1 ) @LoadBalanced
- 도메인의 주소를 ip:port 형식이 아닌 **서비스 이름(URL)을 직접 사용**할 수 있음
  - 단 해당 서버와 접근 대상 서버가 **Eureka Server에 등록 되어 있는 상태**여야 함
    - http://service-name 형태의 서비스 디스커버리 기반 요청 가능
    - Eureka 서비스 레지스트리를 사용하면 해당 서비스의 인스턴스 중 하나로 자동 라우팅됨
- Gateway를 거치지 않고 직접 접근 가능함
  - 서비스 이름(http://user-service)을 기반으로 Eureka에서 직접 해당 서비스의 인스턴스를 찾아 연결하기 떄문임

| @LoadBalanced 사용 여부 | @LoadBalanced 사용 O | @LoadBalanced 사용 X |
|----------------------|----------------------|----------------------|
| **서비스 이름 사용 가능** | ✅ 가능 (`http://service-name`) | ❌ 불가능 (IP or 도메인 직접 입력 필요) |
| **로드 밸런싱 적용** | ✅ 서비스 인스턴스 간 부하 분산 | ❌ 항상 같은 URL로 요청 |
| **Eureka 등 서비스 디스커버리 연동** | ✅ 가능 | ❌ 불가능 |


### 1 - 1 - A ) Config Server - yml
```yaml
order_service:
#  url: http://127.0.0.1:8000/order-service/%s/orders  << @LoadBalance를 사용하지 않고 Gateway 도매인 적용
   url: http://ORDER-SERVICE/order-service/%s/orders  # @LoadBalance 사용 Eureka에서 찾아 라우팅
```

## 2 ) 사용 코드

### 2 - 1 ) Collection 반환 시
- `String.format` 사용해 url 내 Path 적용
- `new ParameterizedTypeReference<>()`를 사용해 **List<T>, Map<K, V> 같은 제네릭 타입**을 반환할 때 **형태를 유지**
  - 컴파일 시 **List로만 인식되어 타입 정보를 잃어버리므로** 이를 유지하기 위함
```java
class foo{
    public void getUserByUserId(String userId) {
        // config server에서 env 정보를 읽어옴
        String orderUrl = String.format(env.getProperty("order_service.url"), userId);
        ResponseEntity<List<ResponseOrder>> orderListResponse =
                restTemplate.exchange(orderUrl, HttpMethod.GET, null,
                        // List<T>, Map<K, V> 같은 제네릭 타입을 반환할 때 형태를 유지하기 위해 사용함
                        new ParameterizedTypeReference<>() {
                        });
        List<ResponseOrder> orders = orderListResponse.getBody();
        return userDto;
    }
}
```

### 2 - 2 ) 단일 객체 반환 시
```java
class foo{
    public void getUserByUserId(String userId) {
        // config server에서 env 정보를 읽어옴
        String orderUrl = String.format(env.getProperty("order_service.url"), userId);
        ResponseEntity<ResponseOrder> orderResponse =
                restTemplate.exchange(orderUrl, HttpMethod.GET, null, ResponseOrder.class);
        ResponseOrder order = orderResponse.getBody();
        return userDto;
    }
}
```