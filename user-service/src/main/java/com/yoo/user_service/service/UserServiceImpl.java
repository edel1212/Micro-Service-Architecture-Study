package com.yoo.user_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoo.user_service.dto.UserDto;
import com.yoo.user_service.entity.UserEntity;
import com.yoo.user_service.repository.UserRepository;
import com.yoo.user_service.vo.RequestUser;
import com.yoo.user_service.vo.ResponseUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ObjectMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseUser createUser(RequestUser requestUser) {
        // Req -> DTO
        UserDto userDto = mapper.convertValue(requestUser, UserDto.class);
        userDto.setUserId(UUID.randomUUID().toString());
        userDto.setEncryptedPwd( passwordEncoder.encode(requestUser.getPwd()) );
        // DTO -> Entity
        UserEntity userEntity =  mapper.convertValue(userDto, UserEntity.class);
        // Insert
        UserEntity entity = userRepository.save(userEntity);
        // Entity -> Res
        return mapper.convertValue(entity, ResponseUser.class);
    }
}
