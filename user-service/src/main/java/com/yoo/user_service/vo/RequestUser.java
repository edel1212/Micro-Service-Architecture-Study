package com.yoo.user_service.vo;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestUser {
    @NotNull(message = "Email Cannot be null")
    @Email
    @Size(min = 2, message = "Email Less characters")
    private String email;

    @Size(min = 2, message = "Name Less characters")
    @NotNull(message = "name Not Null")
    private String name;

    @Size(min = 2, message = "Password Less characters")
    @NotNull(message = "pwd Not Null")
    private String pwd;
}
