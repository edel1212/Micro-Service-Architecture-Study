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
