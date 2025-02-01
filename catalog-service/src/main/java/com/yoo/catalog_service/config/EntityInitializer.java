package com.yoo.catalog_service.config;

import com.yoo.catalog_service.entity.CatalogEntity;
import com.yoo.catalog_service.repository.CatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class EntityInitializer {

    private final CatalogRepository catalogRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (catalogRepository.count() == 0) { // 기존 데이터가 없을 경우만 삽입
            catalogRepository.saveAll(
                    List.of(
                            CatalogEntity.builder()
                                    .productId("CATALOG-001")
                                    .productName("Berlin")
                                    .stock(100)
                                    .unitPrice(1500)
                                    .build(),

                            CatalogEntity.builder()
                                    .productId("CATALOG-002")
                                    .productName("Tokyo")
                                    .stock(110)
                                    .unitPrice(1000)
                                    .build(),

                            CatalogEntity.builder()
                                    .productId("CATALOG-003")
                                    .productName("Stockholm")
                                    .stock(120)
                                    .unitPrice(2000)
                                    .build()
                    )
            );

            log.info("------");
            log.info("✅ 더미 데이터가 추가되었습니다!");
            log.info("------");
        }
    }
}