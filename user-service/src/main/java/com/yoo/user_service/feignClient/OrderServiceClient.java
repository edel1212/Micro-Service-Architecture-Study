package com.yoo.user_service.feignClient;

import com.yoo.user_service.vo.FailResponseOrder;
import com.yoo.user_service.vo.ResponseOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "order-service")
public interface OrderServiceClient {

    @GetMapping("/order-service/{userId}/orders")
    List<ResponseOrder> getOrders(@PathVariable String userId);

    // 404 Error
    @GetMapping("/order-service/{userId}/orders-404")
    List<ResponseOrder> getOrders404Error(@PathVariable String userId);

    // 응답 값이 매칭이 안될 경우
    @GetMapping("/order-service/{userId}/orders")
    List<FailResponseOrder> getOrdersNotMatchResponse(@PathVariable String userId);
}
