package com.yoo.user_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoo.user_service.dto.UserDto;
import com.yoo.user_service.service.UserService;
import com.yoo.user_service.vo.RequestLogin;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;

@Log4j2
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final UserService userService;
    private final Environment env;

    public AuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, Environment env) {
        super(authenticationManager);
        this.userService = userService;
        this.env = env;
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
            var mapper = new ObjectMapper();
            RequestLogin requestLogin = mapper.readValue(request.getInputStream(), RequestLogin.class);
            log.info("getEmail ::: {}", requestLogin.getEmail());
            log.info("getPassword ::: {}", requestLogin.getPwd());
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(requestLogin.getEmail(), requestLogin.getPwd(), new ArrayList<>()));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String userName = ((User)authResult.getPrincipal()).getUsername();
        UserDto userDto = userService.getUserDetailsByEmail(userName);
        String userId = userDto.getUserId();
        // 만료 시간을 밀리초로 설정하여 Date 객체로 변환
        long expirationTime = Long.valueOf(env.getProperty("token.expiration-time"));
        Date expirationDate = new Date(System.currentTimeMillis() + expirationTime);
        // secretKey
        String secretKey    = env.getProperty("token.secret");
        byte[] keyBytes     = Decoders.BASE64.decode(secretKey);
        Key key             = Keys.hmacShaKeyFor(keyBytes);

        // token key 생성
        String token = Jwts.builder()
                // 사용자 또는 애플리케이션을 식별하는 값
                .subject(userId)
                // 만료 시간
                .expiration(expirationDate)
                // 알고리즘 방식
                .signWith(key)
                .compact();

        response.addHeader("token", token);
        response.addHeader("userId", userId);
    }
}
