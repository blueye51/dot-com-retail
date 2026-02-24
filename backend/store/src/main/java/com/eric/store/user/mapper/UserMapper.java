package com.eric.store.user.mapper;

import com.eric.store.auth.dto.UserRegister;
import com.eric.store.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toUser(UserRegister request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        return user;
    }
}
