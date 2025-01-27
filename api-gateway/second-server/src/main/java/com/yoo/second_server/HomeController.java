package com.yoo.second_server;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/second-service")
@RestController
public class HomeController {

    @GetMapping("/welcome")
    public ResponseEntity foo(){
        return ResponseEntity.ok("Second-API-Server");
    }
}
