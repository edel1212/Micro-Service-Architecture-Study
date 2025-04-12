package com.yoo.gateway_server.config;

import com.yoo.gateway_server.filter.CustomFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@RequiredArgsConstructor
//@Configuration
public class FilterConfig {

    private final CustomFilter customFilter;

    //@Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder routeLocatorBuilder){
        return routeLocatorBuilder.routes()
                .route( r
                            // gateway에 해당 path 요청이 들어올 경우
                            -> r.path("/first-service/**")
                            // 필터 사용
                            /**
                             * ✅ gateway를 통해 호출 될 경우 아래와 같이 header 값 추가가 가능
                             *    - 호출 대상이 되는 서버에서 특정 header 값을 강제하여 gateway만을 통해 들어오게 끔 설정도 가능
                             *       - 앞단 gateway를 사용했을 당시 개발자 도구에도 header 값을 볼 수 없지만.. 사실상 외부 접근을 막고 싶은거면 망분리가 잘 되어 있다면 크게 활용 도는 떨어짐
                             * */
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

//    @Bean
//    public GlobalFilter globalFilterConfig() {
//        return (exchange, chain) -> {
//            // PreFilter Business Logic 적용 가능
//            ServerHttpRequest serverHttpRequest   = exchange.getRequest();
//            ServerHttpResponse serverHttpResponse = exchange.getResponse();
//            log.info("Global Pre Filter - baseMessage ? ::: {}");
//            return chain.filter(exchange).then(Mono.fromRunnable(()->{
//                    log.info("Global Post Filter End - Http Status ? :: {}", serverHttpResponse.getStatusCode());;
//            }));
//        } ;
//    }

}
