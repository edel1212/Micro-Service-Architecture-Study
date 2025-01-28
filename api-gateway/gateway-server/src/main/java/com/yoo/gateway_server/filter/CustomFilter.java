package com.yoo.gateway_server.filter;

import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Log4j2
@Component
public class CustomFilter extends AbstractGatewayFilterFactory{
    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            // PreFilter Business Logic 적용 가능
            ServerHttpRequest serverHttpRequest   = exchange.getRequest();
            ServerHttpResponse serverHttpResponse = exchange.getResponse();
            log.info("Pre Filter - Request Id is ? ::: {}", serverHttpRequest.getId());

            return chain.filter(exchange).then(Mono.fromRunnable(()->{
                // PostFilter Business Logic 적용 가능
                log.info("Post Filter - Http Status ? :: {}", serverHttpResponse.getStatusCode());
            }));
        } ;
    }
}
