package com.yoo.catalog_service.service;

import com.yoo.catalog_service.entity.CatalogEntity;

import java.util.List;

public interface CatalogService {
    List<CatalogEntity> getAllCatalogs();
}
