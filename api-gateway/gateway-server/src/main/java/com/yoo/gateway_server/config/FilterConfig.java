package com.yoo.gateway_server.config;

import com.yoo.gateway_server.filter.CustomFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
//@Configuration
public class FilterConfig {

    private final CustomFilter customFilter;

  //  @Bean
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
                                .filter(customFilter.apply(new Object())))
                        .uri("http://localhost:8082"))
                .build();
    }
}
