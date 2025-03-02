package com.yoo.user_service.config;

import feign.Logger;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AppConfig {

    // ⭐️ 해당 어노테이션 사용 시 application.yml에서 불러오는 도메인 주소가 Eureka에 등록된 값으로 불러올 수 있음
    //   - 단 Gateway를 거치지 않음
    @LoadBalanced
    @Bean
    public RestTemplate getRestTemplate() {
        int TIMEOUT = 5000;
        RestTemplate restTemplate = new RestTemplateBuilder()
                .connectTimeout(Duration.ofMillis(TIMEOUT))
                .readTimeout(Duration.ofMillis(TIMEOUT))
                .build();
        return restTemplate;
    }

    /**
     * Feign Log Setting
     * */
    @Bean
    public Logger.Level logLevel(){
        return Logger.Level.FULL;
    }
}
