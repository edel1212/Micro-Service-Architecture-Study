package com.yoo.user_service.dto;

import com.yoo.user_service.vo.ResponseOrder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDto {
    private String email;
    private String name;
    private String pwd;
    private String userId;
    private LocalDateTime createAt;

    private String encryptedPwd;

    private List<ResponseOrder> orders;
}
