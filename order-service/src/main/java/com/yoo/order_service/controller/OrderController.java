package com.yoo.order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoo.order_service.dto.OrderDto;
import com.yoo.order_service.entity.OrderEntity;
import com.yoo.order_service.service.OrderService;
import com.yoo.order_service.vo.RequestOrder;
import com.yoo.order_service.vo.ResponseOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@Log4j2
@RequestMapping("/order-service")
@RequiredArgsConstructor
public class OrderController {

    private final Environment env;
    private final ObjectMapper mapper;
    private final OrderService orderService;

    @GetMapping("/health-check")
    public String status() {
        return String.format("It's Working in Order Service on LOCAL PORT %s (SERVER PORT %s)",
                env.getProperty("local.server.port"),
                env.getProperty("server.port"));
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrder(@PathVariable("userId") String userId,
                                                     @RequestBody RequestOrder orderDetails) {
        log.info("Before add orders data");

        OrderDto orderDto = mapper.convertValue(orderDetails, OrderDto.class);
        orderDto.setUserId(userId);
        /* jpa */
        OrderDto createdOrder = orderService.createOrder(orderDto);
        ResponseOrder responseOrder = mapper.convertValue(createdOrder, ResponseOrder.class);

        log.info("After added orders data");
        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable("userId") String userId) throws Exception {
        log.info("Order Data 전달 전 - Controller 접근");
        List<OrderEntity> orderList = orderService.getOrdersByUserId(userId);

        List<ResponseOrder> result = orderList
                .stream()
                .map(i-> mapper.convertValue(i, ResponseOrder.class) )
                .collect(Collectors.toList());
        log.info("Order Data 전달 후");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
