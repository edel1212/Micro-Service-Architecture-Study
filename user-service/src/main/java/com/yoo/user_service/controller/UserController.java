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

import java.util.List;

@RestController
@Log4j2
// ⭐️API Gate-way에서 호출하려는 Path의 Prefix가 같아야한다.
@RequestMapping("/user-service")
@RequiredArgsConstructor
public class UserController {

    private final Environment environment;
    private final Greeting greeting;
    private final UserService userService;

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
