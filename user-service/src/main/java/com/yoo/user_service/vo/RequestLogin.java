package com.yoo.user_service.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestLogin {
    @NotNull(message = "Email Cannot be null")
    @Email
    @Size(min = 2, message = "Email Less characters")
    private String email;
    @NotNull(message = "pwd Not Null")
    private String password;
}
