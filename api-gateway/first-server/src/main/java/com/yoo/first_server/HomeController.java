package com.yoo.first_server;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RequestMapping("/first-service")
@RestController
public class HomeController {

    @GetMapping("/welcome")
    public ResponseEntity foo(){
        return ResponseEntity.ok("First-API-Server");
    }

    @GetMapping("/message")
    public ResponseEntity message(HttpServletRequest httpServletRequest){
        String header = httpServletRequest.getHeader("first-request");
        log.info("header :: {}",header);
        return ResponseEntity.ok("First-API-Server - " + header);
    }
}
