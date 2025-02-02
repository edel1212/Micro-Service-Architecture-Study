package com.yoo.user_service.config;

import com.yoo.user_service.security.AuthenticationFilter;
import jakarta.servlet.Filter;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

@Configuration
@Log4j2
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,AuthenticationManager authenticationManager) throws Exception{
        http.csrf(csrf -> csrf.disable());

        // frame 접근 허용 -  h2 내 UI 구성을 위함
        http.headers( head -> head.frameOptions( config -> config.disable()));

        // 👉 모든 접근 제한
        http.authorizeHttpRequests( access ->{
            // "/user-service" 로 시작하는 요청 접근 허용
            access.requestMatchers("/user-service/**").anonymous();
            // h2-console 접근 허용
            access.requestMatchers("/h2-console/**").permitAll();
        });

        http.addFilter(getAuthenticationFilter(authenticationManager));
        return http.build();
    }

    private Filter getAuthenticationFilter(AuthenticationManager authenticationManager) {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter();
        authenticationFilter.setAuthenticationManager(authenticationManager);
        return authenticationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
