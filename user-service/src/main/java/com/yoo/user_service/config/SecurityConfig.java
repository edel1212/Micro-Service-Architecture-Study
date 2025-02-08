package com.yoo.user_service.config;

import com.yoo.user_service.security.AuthenticationFilter;
import com.yoo.user_service.service.UserService;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final Environment env;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(passwordEncoder);
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
        http.authenticationManager(authenticationManager);


        http.csrf(csrf -> csrf.disable());

        // frame 접근 허용 -  h2 내 UI 구성을 위함
        http.headers( head -> head.frameOptions( config -> config.disable()));

        // 👉 모든 접근 제한
        http.authorizeHttpRequests( access ->{
            // "/user-service" 로 시작하는 요청 접근 허용
            //access.requestMatchers("/user-service/**").anonymous();
            access.requestMatchers("/**").permitAll();
            // h2-console 접근 허용
            access.requestMatchers("/h2-console/**").permitAll();
        });

        http.addFilter(this.getAuthenticationFilter(authenticationManager));
        return http.build();
    }

    private AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager){
        return new AuthenticationFilter(authenticationManager, userService, env);
    }
}
