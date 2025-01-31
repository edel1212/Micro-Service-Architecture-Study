package com.yoo.user_service.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Log4j2
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.csrf(csrf -> csrf.disable());

        // frame 접근 허용 -  h2 내 UI 구성을 위함
        http.headers( head -> head.frameOptions( config -> config.disable()));

        // 👉 모든 접근 제한
        http.authorizeHttpRequests( access ->{
            // "/user-service" 로 시작하는 요청 접근 허용
            access.requestMatchers("/user-service/**").permitAll();
            // h2-console 접근 허용
            access.requestMatchers("/h2-console/**").permitAll();

        });
        return http.build();
    }
}
