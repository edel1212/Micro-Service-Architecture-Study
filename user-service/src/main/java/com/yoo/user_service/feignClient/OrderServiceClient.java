package com.yoo.user_service.feignClient;

import com.yoo.user_service.vo.FailResponseOrder;
import com.yoo.user_service.vo.ResponseOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

// ✅ url = "${order-service-url}"을 통해 url 지정
//    ㄴ> 기존 eureka-service가 아닌 configMap -> application.yml -> app 방식으로 값을 받아옴
@FeignClient(name = "order-service", url = "${order-service-url}")
public interface OrderServiceClient {

    @GetMapping("/order-service/{userId}/orders")
    List<ResponseOrder> getOrders(@PathVariable String userId);

    @GetMapping("/order-service/{userId}/orders")
    List<ResponseOrder> getOrdersWidthHeader(@PathVariable String userId, @RequestHeader("foo") String foo);

    // 404 Error
    @GetMapping("/order-service/{userId}/orders-404")
    List<ResponseOrder> getOrders404Error(@PathVariable String userId);

    // 응답 값이 매칭이 안될 경우
    @GetMapping("/order-service/{userId}/orders")
    List<FailResponseOrder> getOrdersNotMatchResponse(@PathVariable String userId);
}
