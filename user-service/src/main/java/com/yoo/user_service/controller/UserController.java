package com.yoo.user_service.controller;

import com.yoo.user_service.vo.Greeting;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {

    private final Environment environment;
    private final Greeting greeting;

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
}
