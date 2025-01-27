package com.yoo.first_server;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/first-service")
@RestController
public class HomeController {

    @GetMapping("/welcome")
    public ResponseEntity foo(){
        return ResponseEntity.ok("First-API-Server");
    }
}
