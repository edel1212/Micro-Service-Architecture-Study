package com.yoo.user_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoo.user_service.dto.UserDto;
import com.yoo.user_service.service.UserService;
import com.yoo.user_service.vo.RequestLogin;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.ArrayList;

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
        log.info(userDto);
        log.info("login Succes!!!!!!");
    }
}
