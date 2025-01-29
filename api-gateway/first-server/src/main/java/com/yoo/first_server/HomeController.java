package com.yoo.first_server;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@Log4j2
@RequestMapping("/first-service")
@RestController
public class HomeController {

    private  final Environment env;

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

    @GetMapping("/check")
    public ResponseEntity check(HttpServletRequest httpServletRequest){
        log.info("Server Port  :: {}",httpServletRequest.getServerPort());
        return ResponseEntity.ok("Server Port Is  " + env.getProperty("local.server.port"));
    }
}
