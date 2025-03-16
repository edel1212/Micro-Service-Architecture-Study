# Zipkin
```yaml
# ✅ 분산 트레이싱 시스템으로 마이크로서비스 아키텍처(MSA)에서 요청(request)이 여러 서비스를 거칠 때, 각 서비스에서 요청의 흐름을 추적하고 분석할 수 있도록 함
#    - TraceId 와 SpanId을 사용하여 추적 ( TraceId는 유일 SpanId는 각각의 Transaction 마다 생성된다. )
```
![img.png](img.png)
- Zipkin의 주요 기능
  - 분산 트랜잭션 추적: 요청이 여러 서비스를 거칠 때, **어디서 얼마나 시간이 걸렸는지 추적**
  - 지연(latency) 분석: 특정 서비스 또는 API 호출에서 발생하는 **응답 지연 파악**
  - 오류 감지: 특정 서비스에서 발생한 **문제를 빠르게 찾을 수 있음**
  - B3 Propagation 지원: 서비스 간의 **요청 흐름을 추적**
    - **traceId, spanId 등의 컨텍스트 정보를 전파**

## 1 ) Zipkin Server
- Docker Compose를 이용해 구동
- 기본적으로 Memory를 이용해 정보를 저장하나 필요에 따라 Database를 연동해서 저장 가능
  - Database를 연동할 경우 Zipkin이라는 스키마가 존재해야 함
```yaml
services:
  zipkin:
    image: openzipkin/zipkin:latest
    container_name: zipkin
    ports:
      - "9411:9411"
    environment:
      # Storage 저장 방식
      - STORAGE_TYPE=mem
    restart: always
```

## 2 ) Spring-Boot using Zipkin
```yaml
# ✅ 요청의 흐름 파악 대상의 Service에는 모두 적용해줘야한다.  
```

### 2 - 1 ) build.gradle
- Spring Boot3에서 **spring-cloud-starter-sleuth가 제거**되었고, 대신 **Micrometer Tracing을 사용**해야 함
  - Micrometer Tracing을 이용하여 Zipkin과 연동하려면 **Brave를 사용한 설정이 필요**
- Feign을 사용해 연계를 진행할 경우 해당 Trace를 추적하기 위해 `feign-micrometer`가 필수 적으로 필요함
  - ☠️ 삽질 : 서버 기동 및 Zipkin Dashboard 내 에러는 없으나 TraceId가 이어지지 않음
    
```groovy
dependencies {
	// Zipkin
	implementation 'org.springframework.boot:spring-boot-starter-actuator'
	implementation 'io.micrometer:micrometer-tracing-bridge-brave'
	implementation 'io.zipkin.reporter2:zipkin-reporter-brave'
	implementation 'io.github.openfeign:feign-micrometer' // ✅ feign 사용 시 zipkin 과 traceId 공유를 위해 필요
}
```

### 2 - 2 ) application.yml
- `tracing.sampling.probability` : **샘플링 비율을 설정** 1.0 일 경우 100% / 0.7 일 경우 70%
- `tracing.propagation` 설정
  - `tracing.propagation.consume`→ 외부에서 들어오는 트레이스 정보를 어떤 형식으로 받을지 결정
  - `tracing.propagation.produce` → 외부로 나가는 트레이스 정보를 어떤 형식으로 보낼지 결정 

```yaml
management:
  # Zipkin 설정
  tracing:
    sampling:
      probability: 1.0 # 샘플링할 비율 - 100% 샘플링 (모든 요청을 추적)
    propagation:
      consume: B3
      produce: B3_MULTI
  zipkin:
    tracing:
      endpoint: "http://localhost:9411/api/v2/spans" # ⭐️ Zipkin 서버 주소
```