# Config Service

![img.png](img.png)

## 1 ) Spring Config Server란?
- MSA 구조에서 서버에 필요한 **설정 정보**를 **외부 시스템에서 관리** 할 수 있다.
  - 기존 `application.yml` -> `remote repository, git, static file` 등을 사용 가능
- **하나의 중앙화된 저장소에서 구성 요소를 관리** 할 수 있기에 **일관적인 설정이 가능**
  - 휴먼 에러가 줄어 듬
- 기존 application.yml을 사용 할 경우 변경이 있을 때 서버 재배포가 필요 하지만 Config Server를 사용하면 설정 변경 시 **재배포 불필요**
- 각각의 설정 파일또한 운영 환경에 맞게 **구성 정보를 파이프 라인을 통해 설정 가능**
    - local, dev, pord 등..

## 2 ) 설정 파일 read 우선 순서
```properties
# application.yml -> application-<profile>.yml -> 타겟.yml -> 타겟-<profile>.yml
# ex) application.yml -> application-dev.yml -> user-service.yml -> user-service-dev.yml 
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
- 다른 dependencies는 불필요하며, **spring-cloud-config-server**만 있으면 된다.
```groovy
dependencies {
	implementation 'org.springframework.cloud:spring-cloud-config-server'
}
```

### 3 - 2 ) ServerApplication.java
-`@EnableConfigServer` 사용 ConfigServer로 **활성화**
  - ☠️ 삽질 : 해당 부분을 누락 삽질함... 미지정 시 에러는 없으나 **어떠한 방법으로도 설정 파일을 못 읽음**
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
- `spring.cloud.server` 하위에 필요 설정을 작성

####  3 - 3 - A )  git local 방식
- `spring.cloud.server.git.uri` 지정 필요
- local git의 파일 정보 - [참고](https://github.com/edel1212/Micro-Service-Architecture-Study/tree/main/git-local-repo)에서 확인 가능하다.
  - `file://`프로토콜을 사용하여 가져온 파일의 위치에 **중앙에서 관리할 설졍 파일(yml)** 존재
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
- `file://` 프로토톨이 아닌 `https://` 프로토콜을 이용해 git clone의 주소 주입
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
- `profiles.active: native` **설정 필수**
- 기본 틀은 크게 다르지 않지만 server 지정에서 git -> native로 변경과 `search-locations`를 통한 경로 지정
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

##  4 ) Client에서 Config Service 사용
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
- `spring.config.import` 설정
  - `optional:configserver` 부분에서 configserver는 **prefix로 고정 값**이다.
    - Config Server에서 설정을 가져오도록 지정함
    - `optional:` prefix 사용 이유
      -  해당 prefix가 없으면, Config Server가 없을 때 **애플리케이션이 실행되지 않음**
      - ✅ 해당 prefix가 있을 땐, Config Server가 없더라도 애플리케이션이 **정상 실행되며 기본 설정을 사용**
- `spring.cloud.config.name` 설정
  - config server에서 **가져울 yml의 파일명 지정** (<target>.yml)
- ConfigServer와 Client Server 내 중복된 형식의 값을 가져올 경우 우선 순위
  - **ConfigServer에서 가져 오는 값이 우선으로 적용**
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
  - Config Server에서 **설정 값 변경 시** Client에서 **바로 해당 변경 값 적용되지 않음 서버 재기동 필요** - 👎(불편함 옳은 방법이 아님)
    - **서버 재기동 시 변경된 값을 가져오는 것을 확인** 가능
```text
>> Adding property source: Config resource 'file [/Users/yoo/Desktop/Project/config-repo/ecommerce.yml]' via location 'file:/Users/yoo/Desktop/Project/config-repo/'
```

## 5 ) [Config Client] 변경된 config service 값 갱신 방법 ( Actuator refresh 방식 ) 
- "4 - 3"에서 언급한 Config Server에서 값이 변경 한다 해도 Client를 재기동하는 것은 말이 안되는 방법이기에 대안을 사용 할 수 있다.
  - 해결 방법 : `"Actuator refresh", "Spring cloud bus"` 두가지 방법이 있다.
- 현 단계는 "Actuator refresh"를 사용 방법 진행

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

## 6 ) [Config client] GateWay Service 적용 ( Actuator refresh 빙식 )
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

## 7 ) profile별 설정 값 가져오기
```yaml
# ✅ profile별 config-server에서 yml파일을 가져옴 
# 
# Config-Server 내부 파일 구조
# > -rw-r--r--@ 1 yoo  staff   133B  2 16 17:00 ecommerce-dev.yml
# > -rw-r--r--@ 1 yoo  staff   130B  2 16 17:00 ecommerce-prod.yml
# > -rw-r--r--@ 1 yoo  staff   134B  2 16 17:00 ecommerce.yml
```

### 7 - 1 ) Micro-Service - application.yml
- 지정 profile 설정 값을 가져움
  - 예시 ) `target-<profile>.yml`
- 우선 순위 :Config-Server 내 application-{profile}.yml -> 없을 경우 해당 프로젝트 내 application-{profile}.yml
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

## 8 ) 대칭 암호화
- config server를 사용 시 내장 되어 있는 대칭 암호화를 사용 할 수 있다.

### 8 - 1 ) build.gradle
```groovy
dependencies {
  // Config Server
  implementation 'org.springframework.cloud:spring-cloud-config-server'
}
```
### 8 - 2 ) application.yml
- 대칭 암호화에 사용될 암호키를 등록
```yaml
encrypt:
  key: abcdefghijklmnopqrstuvwxyz0123456789
```

### 8 - 3 ) 사용 방법
```yaml
# ✅ Http protocol을 사용하여 암호화 및 복호화 된 값을 가져올 수 있다
#    - Post Method 사용
```
- 암호화 요청 방법
  - Http Method : POST
  - Path : encrypt
  - Request Body :  "암호화를 원하는 내용"
    - ✨중요 포인트는 **'Content-Type: text/plain'로 요청**을 보내는 것이다.
  - Response : 7dbf10c1f7937423428dbdf2fc8d1b54fc45d28a48bfa065b8ff0c011b32908e
- 복암호화 요청 방법
  - Http Method : POST
  - Path : decrypt
  - Request Body : "암호화된 값"
  - Response : 복호화된 값
- config server기 읽는 설정 파일
  - `{cipher}` prefix를 붙여 사용하면 해당 값을 **Config Server를 통해 읽을 때 복호화된 값으로 사용**할 수 있다.
  ```yaml
  password: "{cipher}7dbf10c1f7937423428dbdf2fc8d1b54fc45d28a48bfa065b8ff0c011b32908e"
  ```

## 9 ) 비대칭 암호화
```yaml
# ✅ Java KeyStore(JKS)를 관리하기 위한 Java 제공 유틸리티
#     - Java 애플리케이션에서 보안 기능(SSL/TLS, 인증서, 키 쌍 등)을 사용할 때 필요
```

### 9 - 1 ) Key 생성
```text
keytool -genkeypair -alias 별칭등록 -keyalg RSA -keysize 2048 -validity 365 \
-keystore 파일명등록.jks -storepass "지정 패스워드" -keypass "지정 패스워드" \
-dname "CN=yoo.com, OU=API Dev, O=MyOrg, L=Seoul, C=KR"e
```

#### 9 - 1 - A ) keytool 명령어 옵션 설명

| 옵션 | 설명 |
|------|------|
| `keytool` | Java KeyStore(JKS)에서 키를 관리하는 명령어 |
| `-genkeypair` | 키 쌍(공개 키 + 개인 키)을 생성 |
| `-alias apiEncryptionKey` | 키의 별칭(Alias) 설정. 여러 키를 관리할 때 개별 식별자로 사용 |
| `-keyalg RSA` | 키 알고리즘을 RSA로 지정 (기본값은 DSA이므로 명확히 지정해야 함) |
| `-keysize 2048` | 키 크기를 2048비트로 설정 (보안성을 위해 최소 2048 이상 권장) |
| `-validity 365` | 키의 유효 기간을 365일(1년)로 설정 |
| `-keystore apiEncryptionKey.jks` | 키를 저장할 키 저장소 파일(JKS) 이름 |
| `-storepass "123456"` | 키 저장소의 비밀번호 (6자 이상이어야 하며, 보안성을 위해 강력한 비밀번호 사용 권장) |
| `-keypass "123456"` | 키의 비밀번호 (storepass와 동일하게 설정 가능, 6자 이상 필요) |
| `-dname "CN=yoo.com, OU=API Dev, O=MyOrg, L=Seoul, C=KR"` | 인증서 주체 정보 (Distinguished Name) |

#### 9 - 1 - B ) `-dname` 옵션 세부 정보

| 속성 | 설명 |
|------|------|
| `CN=yoo.com` | 공통 이름 (Common Name, 예: 도메인 또는 사용자) |
| `OU=API Dev` | 조직 단위 (Organizational Unit, 예: 부서) |
| `O=MyOrg` | 조직명 (Organization) |
| `L=Seoul` | 도시 (Locality) |
| `C=KR` | 국가 코드 (Country, 한국은 `KR`) |

### 9 - 2 ) 생성된 키 정보 확인
- 명령어 입력 후 생성에 사용한 storepass 입력 필요
`keytool -list -keystore apiEncryptionKey.jks -v`

### 9 - 3 ) 인증서 파일 추출
- `-rfc` 옵션을 사용할 경우 공개키가 Base64로 인코딩된 PEM 형식으로 출력
`keytool -export -alias apiEncryptionKey -keystore apiEncryptionKey.jks -rfc -file publicKey.cer`

### 9 - 4 ) 공개 Key 생성
- 공개키 인증서를 publicKey.jks KeyStore에 저장
`keytool -import -alias apiEncryptionKey -file publicKey.cer -keystore publicKey.jks`

### 9 - 5 ) Config Server 적용 - application.yml
- location에 생성했던 암호키 위치 지정
- password 사용한 비밀번호 입력
- alias에는 사용했던 별칭 입력
```yaml
encrypt:
  key-store:
    location: file://${user.home}/Desktop/Project/keystroe/apiEncryptionKey.jks
    password: 123456
    alias: apiEncryptionKey
```