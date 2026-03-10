package com.eric.store.user.mapper;

import com.eric.store.products.dto.ProductResponse;
import com.eric.store.products.entity.Product;
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

    /**
     * Maps a {@link User} entity to {@link UserProfile}.
     *
     * IMPORTANT:
     * The given User must be fetched with JOIN FETCH to UserSettings.
     */
    public UserProfile toUserProfile(User u) {
        return new UserProfile(
                u.getName(),
                u.getEmail(),
                u.isEmailVerified(),
                toUserSettingsDto(u.getSettings())
        );
    }

    public UserSettingsDto toUserSettingsDto(UserSettings u) {
        return new UserSettingsDto(
                u.isTwoFactorEnabled()
        );
    }
}
