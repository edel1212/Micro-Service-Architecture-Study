# Feign

## 1 ) Feign 란?
```properties
# ✅ RestTemplate 보다 좀 더 간결하게 코드양을 줄일 수 있는 장점이 있다.
#    - HTTP 기반의 RESTful 서비스를 호출하는 클라이언트 라이브러리
#
# ✏️ 확인 사항
#    - 코드가 간결해진 만큼 라우팅 되는 Service의 구조를 알아야한다.
#    - Feign의 사용 방법을 알아야한다.
#    - EurekaClient를 사용하여 요청할 경우 라우팅 시 Gateway를 사용하지 않고 바로 통신한다.  
```

## 2 ) 설정 방법
### 2 - 1 ) build.gradle
- Eureka와의 통합은 선택 사항이며, Eureka 없이도 OpenFeign을 사용할 수 있다
```groovy
dependencies {
	// Open Feign
	implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'
}
```

### 2 - 2 ) 호출 Interface
- `@FeignClient("도메인 주소")`를 사용해 연계하려는 대상을 지정
  - "Eureka Server에 지정된 주소"를 사용할 경우 Discover Server에서 해당 도메인을 찾아 진행
  - 일반적인 "ip:port" 형식으로도 접근 가능하다.
    - gateway를 지정해서 구축하고 싶을 경우 사용하자
```java
@FeignClient(name = "order-service")
public interface OrderServiceClient {

    @GetMapping("/order-service/{userId}/orders")
    List<ResponseOrder> getOrders(@PathVariable String userId);

    // 👉 Header 값을 추가하여 전달을 원할 경우
    @GetMapping("/order-service/{userId}/orders")
    List<ResponseOrder> getOrdersWidthHeader(@PathVariable String userId, @RequestHeader("foo") String foo);
}
```

## 3 ) BusinessLogic 내 Feign 사용
- "2 )"에서 생성한 Interface를 주입하여 사용
```java
@RequiredArgsConstructor
@Log4j2
@Service
public class UserServiceImpl implements UserService {
    private final OrderServiceClient orderServiceClient;
    
    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        UserDto userDto       = mapper.convertValue(userEntity, UserDto.class);
        
        // feign 사용해서 값을 받아옴
        List<ResponseOrder> orders = orderServiceClient.getOrders(userId);
        userDto.setOrders(orders);
        
        return userDto;
    }
}
```

## 4 ) Log 설정
- 기본적으로 Feign을 사용할 경우 연계되는 로그를 볼 수 없게 설정되어 있기에 추가적은 설정이 필요하다.
- profile을 분리하여 설정 필수

### 4 - 1 ) application.yml
- `level` 하위 주수는 `@FeignClient`을 사용하는 package 위치 작성
```yaml
logging:
  level:
    com.yoo.user_service.feignClient: DEBUG
```

### 4 - 2 ) config class
```java
@Configuration
public class AppConfig {
    /**
     * Feign Log Setting
     * */
    @Bean
    public Logger.Level logLevel(){
        return Logger.Level.FULL;
    }
}
```

## 5 ) 예외 처리

### 5 - 1 ) Component Class
```yaml
# ✅ 요청 받은 response.status()을 통한 status을 통해 분기가 가능하지만 이외의 에러는 catch 하지 않음
#    - ex) @FeignClient(name = "틀린 정보")를 넣을 경우 404가 아닌 Internal Server Error 반환 
```
- ErrorDecoder를 구현한 Class를 Bean 스캔 대상으로 등록해준다.
- `Environment`를 불러오는 것은 **필수가 아니다** config server를 통해 에러 Message를 **공통되게 처리를 원할 경우 사용**하면 된다. 
```java
@RequiredArgsConstructor
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final Environment env;

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        switch(status) {
            case 400:
                break;
            case 404:
                if (methodKey.contains("getOrders")) {
                    return new ResponseStatusException(HttpStatus.valueOf(status),
                            env.getProperty("order_service.exception_not_found"));
                }// if
                break;
            default:
                return new Exception(response.reason());
        }
        return null;
    }
}
```

### 5 - 2 ) BusinessLogic
- "3 )"와 같은 방식으로 진행 추가적은 코드 필요 없음

#### 5 - 2 - A ) FeignClient
```java
@FeignClient(name = "order-service")
public interface OrderServiceClient {
    // 404 Error
    @GetMapping("/order-service/{userId}/orders-404")
    List<ResponseOrder> getOrders404Error(@PathVariable String userId);

    // 응답 값이 매칭이 안될 경우
    @GetMapping("/order-service/{userId}/orders")
    List<FailResponseOrder> getOrdersNotMatchResponse(@PathVariable String userId);
}
```
#### 5 - 2 - B ) BusinessLogic 사용
- ordersNotMatch에서는 필드가 매칭 되지 않더라도 해당 코드에서는 **예러를 반환하지 않고 매칭되지 않는 필드는 null로 반환**한다.
  - **실제 응답값의 Size가 2개**일 경우 **ordersNotMatch의 Size는 2개**이며, 필드는 null로 되어있다.
    - `[{zero:null}, {zero:null}]`
```java
@RequiredArgsConstructor
@Log4j2
@Service
public class UserServiceImpl implements UserService {
    private final OrderServiceClient orderServiceClient;
    
    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        UserDto userDto       = mapper.convertValue(userEntity, UserDto.class);

        // 👉 FailResponseOrder는 반환 필드가 완전히 다른 Class
        List<FailResponseOrder> ordersNotMatch = orderServiceClient.getOrdersNotMatchResponse(userId);
        
        // feign 사용해서 값을 받아옴
        List<ResponseOrder> orders404 = orderServiceClient.getOrders404Error(userId);
        userDto.setOrders(orders);
        
        return userDto;
    }
}
```