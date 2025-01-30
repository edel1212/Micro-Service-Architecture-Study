package com.yoo.user_service.controller;

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

@RestController
@Log4j2
@RequestMapping("/")
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
}
