package com.yoo.user_service.service;

import com.yoo.user_service.dto.UserDto;
import com.yoo.user_service.entity.UserEntity;
import com.yoo.user_service.vo.RequestUser;
import com.yoo.user_service.vo.ResponseUser;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    ResponseUser createUser(RequestUser requestUser);
    UserDto getUserByUserId(String userId);
    UserDto getUserDetailsByEmail(String email);
    List<UserEntity> getUserAll();
}
