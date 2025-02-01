package com.yoo.catalog_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoo.catalog_service.entity.CatalogEntity;
import com.yoo.catalog_service.service.CatalogService;
import com.yoo.catalog_service.vo.ResponseCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/catalog-service")
public class CatalogController {

    private final Environment env;
    private final CatalogService catalogService;
    private final ObjectMapper mapper;

    @GetMapping("/health-check")
    public String status() {
        return String.format("It's Working in Catalog Service on LOCAL PORT %s (SERVER PORT %s)",
                env.getProperty("local.server.port"),
                env.getProperty("server.port"));
    }

    @GetMapping("/catalogs")
    public ResponseEntity<List<ResponseCatalog>> getCatalogs() {
        Iterable<CatalogEntity> catalogList = catalogService.getAllCatalogs();

        List<ResponseCatalog> result = new ArrayList<>();
        catalogList.forEach(v -> {
            result.add(mapper.convertValue(v, ResponseCatalog.class));
        });

        return ResponseEntity.ok(result);
    }



}
