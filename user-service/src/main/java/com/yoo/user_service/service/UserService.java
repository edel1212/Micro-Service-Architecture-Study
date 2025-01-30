package com.yoo.user_service.service;

import com.yoo.user_service.vo.RequestUser;
import com.yoo.user_service.vo.ResponseUser;

public interface UserService {
    ResponseUser createUser(RequestUser requestUser);
}
