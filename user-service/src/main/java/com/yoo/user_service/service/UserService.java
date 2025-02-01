package com.yoo.user_service.service;

import com.yoo.user_service.dto.UserDto;
import com.yoo.user_service.entity.UserEntity;
import com.yoo.user_service.vo.RequestUser;
import com.yoo.user_service.vo.ResponseUser;

import java.util.List;

public interface UserService {
    ResponseUser createUser(RequestUser requestUser);
    UserDto getUserByUserId(String userId);
    List<UserEntity> getUserAll();
}
