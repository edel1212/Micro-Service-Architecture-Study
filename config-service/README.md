# Config Service

![img.png](img.png)

## 1 ) Spring Config Server란?
- MSA 구조에서 서버에 필요한 **설정 정보**를 **외부 시스템에서 관리** 할 수 있다.
  - 기존 application.yml -> Repository, 외부 접근 가능 파일 등을 사용
- **하나의 중앙화된 저장소에서 구성 요소를 관리** 할 수 있기에 **일관적인 설정이 가능**하다.
  - 휴먼 에러가 줄어 듬
- 기존 application.yml을 사용 할 경우 변경이 있을 때 서버 재배포가 필요 하지만 Config Server를 사용하면 설정 변경 시  재배포 불필요
- 각각의 설정 파일또한 운영 환경에 맞게 구성정보를 파이프라인을 통해 설정 가능
    - local, dev, pord 환경

## 2 ) 설정 파일 우선 순위
```properties
# application.yml -> 지정 이름(타겟 서버 이름).yml -> 지정 이름(타겟 서버 이름)-<profile>.yml
# ex) application.yml -> shop.yml -> shop-dev.yaml 
```
- Config 서버 내 다양한 설정 파일(application.yml)을 저장해둔 후 해당 설정을 읽는 하위 Micro Service 내에서 지정한 설정을 가져오는 방식
  - Ex) user-service 서버에서 config server에 user-service 설정 파일을 요청 시 해당 `user-service.yml` 설정 값 반환
    - user-service 서버에서 profile을 prod로 요청 시 `user-service-prod.ygml` 설정 값 반환

## 3 ) Config Server 설정 방법
```properties
# ✅ 설정 파일을 읽어오는 방법은 3가지 예제를 사용 local-repository, remote-repository, file-read 
#   - Config Server의 Default Port : 8888 이다.
#   - http://localhost:8888/ecommerce/default 경로 요청 시 파일을 읽는 방법
#     - 지정 경로의 ecommerce.yml 이며 profile : default 
```

### 3 - 1 ) build.gradle
- 다른 dependencies는 불필요하며, **spring-cloud-config-server 만 추가**
```groovy
dependencies {
	implementation 'org.springframework.cloud:spring-cloud-config-server'
}
```

### 3 - 2 ) ServerApplication.java
- 가장 중요하다 `@EnableConfigServer`를 추가해줘야 ConfigServer로 **활성화 된다.**
  - 해당 부분을 누락하여 삽질함... 미지정 시 어떠한 방법으로도 설정 파일을 못 읽음
```java
@SpringBootApplication
// ⭐️ 해당 어노테이션을 추가하지 않으면 config 위치를 알맞게 지정해도 찾을 수 없음
@EnableConfigServer
public class ConfigServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(ConfigServiceApplication.class, args);
	}
}
```

### 3 - 3 ) application.yml
```yaml
# ✅ git local, git remote, Native File 방식이 있다.
```
- spring -> cloud -> server 하위에 설정을 작성하여 진행한다.
####  3 - 3 - A )  git local 방식
- 해당 설정된 git의 파일 정보는 [링크](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/git-local-repo)에서 확인 가능하다.
  - 해당 위치에 중앙에서 관리할 설정 yml파일이 존재함
```yaml
server:
  port: 8888 # Config Server Default Port Number

spring:
  application:
    name: config-service
  cloud:
    config:
      server:
        git:
          uri: file:///Users/yoo/Desktop/Project/config-repo
```

####  3 - 3 - B ) git remote 방식
- 기본 틀은 크게 다르지 않지만 https 프로토콜을 이용해 git clone의 주소를 넣어주면 된다.
- private repository일 경우 uri 하단에 <username> 과 <password> 추가 필요
```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/edel1212/config-repo.git
          #username: foo
          #password: foo
```

####  3 - 3 - C ) native file 방식
- profiles.active: native **설정은 필수**이다.
- 기본 틀은 크게 다르지 않지만 server 지정에서 git -> native로 변경과 search-locations를 통한 경로 지정이 필요
  - **file 프로토콜을 사용**하여 파일을 가져옴
```yaml
spring:
  application:
    name: config-service
  profiles:
    # ✨ native 사용 선언
    active: native
  cloud:
    config:
      server:
        # ✨ native 경로 지정
        native:
          search-locations: file:///Users/yoo/Desktop/Project/Micro-Service-Architecture-Study/native-file-repo

```

##  4 ) Config Client 적용 방법
```properties
# ✅ user-service를 기준으로 작성 테스트 진행
```

### 4 - 1 ) build.gradle
```groovy
dependencies {
	// Config Client
	implementation 'org.springframework.cloud:spring-cloud-starter'
	implementation 'org.springframework.cloud:spring-cloud-starter-config'
}
```

### 4 - 2 ) application.yml
- config.import 설정
  - `optional:configserver` 부분에서 configserver는 **prefix로 고정 값**이다.
    - Config Server에서 설정을 가져오도록 지정한다는 의미
  - optional 부분 사용 이유
    -  optional:이 없으면, Config Server가 없을 때 **애플리케이션이 실행되지 않음**
    -   ✅ optional:이 있으면, Config Server가 없더라도 애플리케이션이 **정상 실행되며 기본 설정을 사용**
- cloud.config.name 설정
  - config server에서 **읽어올 yml 대상 지정**
- ConfigServer 와 Client Server 중복 설정 Key 시 우선 순위
  - **ConfigServer가 우선으로 적용**
```yaml
spring:
  application:
    name: user-service

  # Config Server Setting
  config:
    import: optional:configserver:http://localhost:8888
  cloud:
    config:
      name: ecommerce  # `ecommerce.yml`을 읽도록 설정
```

### 4 - 3 ) Result 
- ✅ 주의사항
  - Config Server에서 **설정을 변경**한다 해도 Client에서 **바로 해당 변경 값 적용되지 않음 서버 재기동 필요** (불편함 옳은 방법이 아님)
- 서버 기동 시 정상적으로 가져오는 것을 확인 가능함법
```text
>> Adding property source: Config resource 'file [/Users/yoo/Desktop/Project/config-repo/ecommerce.yml]' via location 'file:/Users/yoo/Desktop/Project/config-repo/'
```

## 5 ) Config Client - Changed configuration values ( Actuator refresh ) 
- "4 - 3"에서 언급한 Config Server에서 값이 변경 한다 해도 Client를 재기동하는 것은 말이 안되는 방법이기에 대안을 사용 할 수 있다.
  - 해결 방법 : "Actuator refresh", "Spring cloud bus"이 있다.
- 현재 단계에서는 "Actuator refresh"를 사용한 방법을진행

### 5 - 1 ) Actuator refresh란?
- Spring Boot Actuator를 사용 하는 것이다.
  - Appliationm 상태, 모니터링 기능 제공
  - Metric 수집을 위한 Http End point 제공

#### 5 - 2 ) build.gradle
```groovy
dependencies {
	// Actuator
	implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

#### 5 - 3 ) application.yml
- 실제 갱신하는 end point는 **refresh**이다. 
```yaml
# Actuator 설정
management:
  endpoints:
    web:
      exposure:
        # /actuator/** 로 사용할 기능 설정
        include: refresh, health, beans
```

#### 5 - 4 ) 갱신 요청
- 반드시 요청은 **POST방식으로 요청**해야 한다.
  - 필요 파라미터❌
- 응답 값은 변경된 값이  JSON형태로 응답 온다.
```text
// Request
curl --location --request POST '127.0.0.1:60312/actuator/refresh'
// Response
[
    "config.client.version",
    "token.expiration-time"
]
```

## 6 ) GateWay Service 적용 - Config client, Actuator refresh
```properties
# ✅ 이전에 진행했던 config-client 적용 및 Actuator 적용은 같지만 Actuator 중에서 httpexchanges가 추가 되었다
```
#### 6 - 1 ) Build.gradle
```groovy
dependencies {
	// Config Client
	implementation 'org.springframework.cloud:spring-cloud-starter'
	implementation 'org.springframework.cloud:spring-cloud-starter-config'
	// Actuator
	implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

#### 6 - 2 ) application.yml
- httpexchanges란?
  - 애플리케이션에서 주고받은 HTTP 요청(Request)과 응답(Response)을 **기록하는 기능**
  - Spring Boot 2.x에서는 httptrace를 사용했지만, 3.x부터는 **httpexchanges로 변경됨**
```yaml
spring:
  application:
    name: gateway-service

  # ✨ Config Server Setting
  config:
    import: optional:configserver:http://localhost:8888

  cloud:
    # ✨ Config Server Setting - target yml 파일 지정
    config:
      name: ecommerce  # `ecommerce.yml`을 읽도록 설정
    # gateway route setrting  
    gateway:
      routes:
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/user-service/login
            - Method=POST
          filters:
            - RemoveRequestHeader=Cookie
            - RewritePath=/user-service/(?<segment>.*), /$\{segment}

# Actuator 설정
management:
  endpoints:
    web:
      exposure:
        # /actuator/** 로 사용할 기능 설정
        include: refresh, health, beans, httpexchanges
```

## 7 ) profile 설정
```yaml
# ✅ profile별 config-server에서 yml파일을 가져옴 
# 
# Config-Server 내부 파일 구조
# > -rw-r--r--@ 1 yoo  staff   133B  2 16 17:00 ecommerce-dev.yml
# > -rw-r--r--@ 1 yoo  staff   130B  2 16 17:00 ecommerce-prod.yml
# > -rw-r--r--@ 1 yoo  staff   134B  2 16 17:00 ecommerce.yml
```

### 7 - 1 ) Micro-Service - application.yml
- profile 지정을 통해 값을 받아온다.
- 우선 순위 또한 Config-Server에 해당 application-{profile}.yml가 없을 경우 해당 프로젝트에서 찾은 후 실행 함
```yaml
spring:
  application:
    name: gateway-service
  config:
    import: optional:configserver:http://localhost:8888
  # ✨ Profile 지정    
  profiles:
    active: dev
  cloud:
    config:
      name: ecommerce 
    gateway:
      routes: # code ...
```