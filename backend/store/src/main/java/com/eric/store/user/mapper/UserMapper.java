package com.eric.store.user.mapper;

import com.eric.store.user.dto.UserProfile;
import com.eric.store.user.dto.UserRegister;
import com.eric.store.user.dto.UserSettingsDto;
import com.eric.store.user.entity.User;
import com.eric.store.user.entity.UserSettings;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toUser(UserRegister request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        return user;
    }

    public UserProfile toUserProfile(User u) {
        return new UserProfile(
                u.getName(),
                u.getEmail(),
                u.isEmailVerified(),
                u.getProvider().name()
        );
    }

    public UserSettingsDto toUserSettingsDto(UserSettings s) {
        return new UserSettingsDto(
                s.isTwoFactorEnabled()
        );
    }
}
