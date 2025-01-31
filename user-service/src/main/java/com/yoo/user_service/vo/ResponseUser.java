package com.yoo.user_service.vo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ResponseUser {
    private String email;
    private String name;
    private String userId;
}
