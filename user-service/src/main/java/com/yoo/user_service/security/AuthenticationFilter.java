package com.yoo.user_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoo.user_service.vo.RequestLogin;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.ArrayList;

@Log4j2
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

       try {
           var mapper = new ObjectMapper();
           log.info("input Stream ::: {}", request.getInputStream().toString() );
           RequestLogin requestLogin = mapper.convertValue(request.getInputStream(), RequestLogin.class);
           UsernamePasswordAuthenticationToken token
                   = new UsernamePasswordAuthenticationToken(   requestLogin.getEmail()
                                                               , requestLogin.getPassword()
                                                               , new ArrayList<>());
           return getAuthenticationManager().authenticate(token);
       } catch (Exception e){
           throw new RuntimeException(e);
       } // try - catch
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
// TODO void Metod 이기에 반환 값 없음     super.successfulAuthentication(request, response, chain, authResult);
    }
}
