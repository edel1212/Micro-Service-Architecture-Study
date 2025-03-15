package com.yoo.user_service.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4JConfig {
    @Bean
    Customizer< Resilience4JCircuitBreakerFactory > globalCustomConfiguration(){

        /**
         * CircuitBreaker 설정
         * - failureRateThreshold(4): 실패율이 4%를 초과하면 서킷을 열어 요청 차단
         * - waitDurationInOpenState(1000ms): 서킷이 열린 후 1초 동안 요청 차단 후 반개 상태로 전환
         * - slidingWindowType(COUNT_BASED): 실패율을 계산할 때 고정된 요청 개수 기준 사용
         * - slidingWindowSize(2): 최근 2개의 요청을 기준으로 서킷 브레이커 동작 결정
         */
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(4)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(2)
                .build();

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                // timeoutDuration(4초): 요청이 4초 이상 걸리면 자동으로 타임아웃 발생
                .timeoutDuration(Duration.ofSeconds(4))
                .build();


        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(timeLimiterConfig)
                .circuitBreakerConfig(circuitBreakerConfig)
                .build()
        );
    }
}
