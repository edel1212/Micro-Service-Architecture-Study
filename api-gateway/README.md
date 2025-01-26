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

### 2 - 1 ) 설정

#### dependencies
```groovy
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway-mvc'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
}
```

#### application.yml
- 핵심 설정은 `cloud` 부분 설정이다. 연결에 필요한 URI를 매칭힘 
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
          url: http://loalhost:8081/
          predicates:
            - Path=/first-service/**
        - id: second-service
          url: http://loalhost:8082/
          predicates:
            - Path=/second-service/**


eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka
```