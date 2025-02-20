package com.yoo.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoo.user_service.dto.UserDto;
import com.yoo.user_service.service.UserService;
import com.yoo.user_service.vo.Greeting;
import com.yoo.user_service.vo.RequestUser;
import com.yoo.user_service.vo.ResponseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
public class UserController {

    private final Environment env;
    private final Greeting greeting;
    private final UserService userService;

    @GetMapping("/health-check")
    public ResponseEntity<Map<String, String>> status(){
        Map<String, String> map = new HashMap<>();
        map.put("[ application.yml ] port(local.server.port)", env.getProperty("local.server.port"));
        map.put("[ config server ] token secret-key(env)", env.getProperty("token.secret"));
        map.put("[ config server ] token expiration time(env)", env.getProperty("token.expiration-time"));
        map.put("[ config server ] datasource", env.getProperty("spring.application.datasource.password"));
        return ResponseEntity.ok(map);
    }

    @GetMapping("/welcome")
    public ResponseEntity<String> welcome(){
        String messageToVo = greeting.getMessage();
        String messageToProperty = env.getProperty("greeting.message");
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
