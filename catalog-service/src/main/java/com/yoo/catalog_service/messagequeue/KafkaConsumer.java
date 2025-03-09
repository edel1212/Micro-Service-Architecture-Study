package com.yoo.catalog_service.messagequeue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoo.catalog_service.entity.CatalogEntity;
import com.yoo.catalog_service.repository.CatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class KafkaConsumer {
    private final CatalogRepository catalogRepository;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "example-catalog-topic")
    public void updateQty(String kafkaMessage){
        log.info("kafka Message: -> {}", kafkaMessage);
        Map<Object, Object> map = new HashMap<>();
        // String -> JSON
        try {
            map = mapper.readValue(kafkaMessage, new TypeReference<>() {});
        } catch (Exception e){
            e.printStackTrace();;
        } // try - catch

        String productId = (String) map.get("productId");
        log.info("productId :: {}", productId);

        CatalogEntity entity = catalogRepository.findByProductId(productId);
        if(entity != null){
            // update to qty
            int qty = (Integer) map.get("qty");
            entity.setStock(entity.getStock() - qty);
            catalogRepository.save(entity);
        } // if

    }
}
