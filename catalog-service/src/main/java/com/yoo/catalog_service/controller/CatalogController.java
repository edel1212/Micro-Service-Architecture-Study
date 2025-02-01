package com.yoo.catalog_service.controller;

import com.yoo.catalog_service.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/catalog-service")
public class CatalogController {

    private final Environment env;
    private final CatalogService catalogService;

    @GetMapping("/health-check")
    public String status() {
        return String.format("It's Working in Catalog Service on LOCAL PORT %s (SERVER PORT %s)",
                env.getProperty("local.server.port"),
                env.getProperty("server.port"));
    }


}
