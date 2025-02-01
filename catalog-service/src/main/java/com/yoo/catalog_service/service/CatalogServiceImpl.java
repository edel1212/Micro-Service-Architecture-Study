package com.yoo.catalog_service.service;

import com.yoo.catalog_service.entity.CatalogEntity;
import com.yoo.catalog_service.repository.CatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService{
    private final CatalogRepository catalogRepository;

    @Override
    public List<CatalogEntity> getAllCatalogs() {
        return catalogRepository.findAll();
    }
}
