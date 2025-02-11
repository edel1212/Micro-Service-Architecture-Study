package com.yoo.gateway_server.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Log4j2
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    private final Environment env;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // PreFilter Business Logic 적용 가능
            ServerHttpRequest serverHttpRequest   = exchange.getRequest();


            return onError(exchange, "no authorization header error", HttpStatus.UNAUTHORIZED);
        } ;
    }

    private boolean isJwtValid(String accessToken) {
        String secretKey = env.getProperty("token.secret");
        // Base64 디코딩 후 SecretKey 생성
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        boolean returnValue = true;
        String subject = null;
        try {
            subject = Jwts.parser().verifyWith(key).build().parseSignedClaims(accessToken).getPayload().getSubject();
        } catch (Exception ex) {
            returnValue = false;
        }

        if (subject == null || subject.isEmpty()) {
            returnValue = false;
        }

        return returnValue;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String onError, HttpStatus httpStatus) {
        ServerHttpResponse httpResponse = exchange.getResponse();
        httpResponse.setStatusCode(httpStatus);
        return httpResponse.setComplete();
    }

    public static class Config{

    }
}
