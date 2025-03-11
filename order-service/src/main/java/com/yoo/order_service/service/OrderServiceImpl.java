package com.yoo.order_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoo.order_service.dto.OrderDto;
import com.yoo.order_service.entity.OrderEntity;
import com.yoo.order_service.messagequeue.KafkaOrderProducer;
import com.yoo.order_service.messagequeue.KafkaProducer;
import com.yoo.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ObjectMapper mapper;
    private final OrderRepository orderRepository;
    private final KafkaProducer kafkaProducer;
    private final KafkaOrderProducer kafkaOrderProducer;

    @Override
    public OrderDto createOrder(OrderDto orderDto) {
        orderDto.setOrderId(UUID.randomUUID().toString());
        orderDto.setTotalPrice(orderDto.getQty() * orderDto.getUnitPrice());

        /* send this order kafka */
        // ✅ topic명은 consumer에서 소모할 topic과 일치 해야함
        kafkaProducer.send("example-catalog-topic", orderDto);
        // Kafka Connect Sink 전송 ( DB - 저장  )
        kafkaOrderProducer.send("orders", orderDto);

        return orderDto;
    }

    @Override
    public OrderDto getOrderByOrderId(String orderId) {
        OrderEntity orderEntity = orderRepository.findByOrderId(orderId);
        OrderDto orderDto = mapper.convertValue(orderEntity, OrderDto.class);

        return orderDto;
    }

    @Override
    public List<OrderEntity> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }
}
