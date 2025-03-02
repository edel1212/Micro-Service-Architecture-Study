package com.yoo.user_service.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final Environment env;

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        switch(status) {
            case 400:
                break;
            case 404:
                if (methodKey.contains("getOrders")) {
                    return new ResponseStatusException(HttpStatus.valueOf(status),
                            env.getProperty("order_service.exception_not_found"));
                }// if
                break;
            default:
                return new Exception(response.reason());
        }
        return null;
    }


}
