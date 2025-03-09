package com.yoo.order_service.messagequeue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoo.order_service.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderDto send(String topic, OrderDto orderDto){
        String jsonInString = "";
        try{
            jsonInString = objectMapper.writeValueAsString(orderDto);
        } catch (Exception e){
            e.printStackTrace();
        } // try - catch

        kafkaTemplate.send(topic, jsonInString);
        log.info("kafka Producer sent data from the Order micro service :: {}", orderDto);

        return orderDto;
    }
}
