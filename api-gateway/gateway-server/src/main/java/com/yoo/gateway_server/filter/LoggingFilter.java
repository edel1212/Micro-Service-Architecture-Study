 package com.yoo.gateway_server.filter;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Log4j2
@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config>{

    public LoggingFilter(){
        super(LoggingFilter.Config.class);
    }

    @Override
    public GatewayFilter apply(LoggingFilter.Config config) {
        // 1 . 첫번째 인자는 Interface에서 요구하는 함수 인 filter()를 주입
        GatewayFilter gatewayFilter = new OrderedGatewayFilter( (exchange, chain) -> {
            ServerHttpRequest serverHttpRequest   = exchange.getRequest();
            ServerHttpResponse serverHttpResponse = exchange.getResponse();
            log.info("Logging Pre Filter - baseMessage ? ::: {}", config.getBaseMessage());
            if(config.isPreLogger()){
                log.info("Logging Filter  -> request Id ? ::: {}",serverHttpRequest.getId());
            }
            return chain.filter(exchange).then(Mono.fromRunnable(()->{
                if(config.isPostLogger()){
                    log.info("Logging Post Filter - Http Status ? :: {}", serverHttpResponse.getStatusCode());;
                }
            }));
            // 2 . 순서 지정
        }, Ordered.HIGHEST_PRECEDENCE );

        return gatewayFilter;
    }

    @Data
    public static class Config{
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
