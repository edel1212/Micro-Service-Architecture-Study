package com.yoo.user_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoo.user_service.dto.UserDto;
import com.yoo.user_service.entity.UserEntity;
import com.yoo.user_service.feignClient.OrderServiceClient;
import com.yoo.user_service.repository.UserRepository;
import com.yoo.user_service.vo.RequestUser;
import com.yoo.user_service.vo.ResponseOrder;
import com.yoo.user_service.vo.ResponseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Log4j2
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ObjectMapper mapper;
    private final PasswordEncoder passwordEncoder;

    private final OrderServiceClient orderServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    @Override
    public ResponseUser createUser(RequestUser requestUser) {
        // Req -> DTO
        UserDto userDto = mapper.convertValue(requestUser, UserDto.class);
        userDto.setUserId(UUID.randomUUID().toString());
        String  encodePw = passwordEncoder.encode(requestUser.getPwd());
        userDto.setEncryptedPwd( encodePw );
        log.info("bCryptPasswordEncoder :: {}", encodePw);

        // DTO -> Entity
        UserEntity userEntity =  mapper.convertValue(userDto, UserEntity.class);
        // Insert
        UserEntity entity = userRepository.save(userEntity);
        // Entity -> Res
        return mapper.convertValue(entity, ResponseUser.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        UserDto userDto = mapper.convertValue(userEntity, UserDto.class);

        log.info("Before call order microservice");
        // CircuitBreaker를 사용 -> feign을 요청하여 값을 받아옴
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("order-service");
        List<ResponseOrder> orders  = circuitBreaker.run( () -> orderServiceClient.getOrders(userId)
                                        , trouble -> new ArrayList<>() );

        userDto.setOrders(orders);

        return userDto;
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return mapper.convertValue(userEntity, UserDto.class);
    }

    @Override
    public List<UserEntity> getUserAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("userDetailS");
        UserEntity userEntity = userRepository.findByEmail(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found");
        }

        log.info("------------------");
        log.info("pw :: {}", userEntity.getEncryptedPwd());
        log.info("------------------");

        boolean matches = passwordEncoder.matches("1234", userEntity.getEncryptedPwd());
        System.out.println("비밀번호 일치 여부: " + matches);

        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(), true, true, true, true, new ArrayList<>() );
    }
}
