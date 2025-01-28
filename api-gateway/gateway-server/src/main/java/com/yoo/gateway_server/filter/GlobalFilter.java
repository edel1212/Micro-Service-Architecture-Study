package com.yoo.gateway_server.filter;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Log4j2
@Component
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config>{

    public GlobalFilter(){
        super(GlobalFilter.Config.class);
    }

    @Override
    public GatewayFilter apply(GlobalFilter.Config config) {
        return (exchange, chain) -> {
            // PreFilter Business Logic 적용 가능
            ServerHttpRequest serverHttpRequest   = exchange.getRequest();
            ServerHttpResponse serverHttpResponse = exchange.getResponse();
            log.info("Global Pre Filter - baseMessage ? ::: {}", config.getBaseMessage());
            if(config.isPreLogger()){
                log.info("Global Filter Start -> request Id ? ::: {}",serverHttpRequest.getId());
            }
            return chain.filter(exchange).then(Mono.fromRunnable(()->{
                if(config.isPostLogger()){
                    log.info("Global Post Filter End - Http Status ? :: {}", serverHttpResponse.getStatusCode());;
                }
            }));
        } ;
    }

    @Data
    public static class Config{
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
