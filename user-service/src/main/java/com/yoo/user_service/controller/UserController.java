package com.yoo.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoo.user_service.dto.UserDto;
import com.yoo.user_service.service.UserService;
import com.yoo.user_service.vo.Greeting;
import com.yoo.user_service.vo.RequestLogin;
import com.yoo.user_service.vo.RequestUser;
import com.yoo.user_service.vo.ResponseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
@RequiredArgsConstructor
public class UserController {

    private final Environment environment;
    private final Greeting greeting;
    private final UserService userService;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody RequestLogin loginDTO){
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginDTO.getEmail()
                , loginDTO.getPwd());

        /** 실제 검증 후 반환하는  authentication에는 내가 커스텀한 UserDetail정보가 들어가 있음*/
        // 2. 실제 검증. authenticate() 메서드를 통해 요청된 Member 에 대한 검증 진행
        // authenticate 메서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        String userName = authentication.getName();


        return ResponseEntity.ok("login Success" + userName);
    }

    @GetMapping("/health-check")
    public ResponseEntity<String> status(){
        return ResponseEntity.ok("It's Working in User Service");
    }

    @GetMapping("/welcome")
    public ResponseEntity<String> welcome(){
        String messageToVo = greeting.getMessage();
        String messageToProperty = environment.getProperty("greeting.message");
        return ResponseEntity.ok(messageToVo + " :::::: " + messageToProperty);
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser requestUser){
        return ResponseEntity.status(HttpStatus.CREATED).body( userService.createUser(requestUser) );
    }

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers(){
        var mapper = new ObjectMapper();
        List<ResponseUser> result = userService.getUserAll().
                stream()
                .map( i-> mapper.convertValue(i, ResponseUser.class) )
                .toList();
        return ResponseEntity.ok( result );
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable String userId){
        return ResponseEntity.ok(userService.getUserByUserId(userId));
    }
}
