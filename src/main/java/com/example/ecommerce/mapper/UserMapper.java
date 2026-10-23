package com.example.ecommerce.mapper;

import com.example.ecommerce.dto.UserRequest;
import com.example.ecommerce.dto.UserResponse;
import com.example.ecommerce.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(UserRequest request) {
        return new User(
                request.name(),
                request.email(),
                request.phone(),
                request.address()
        );
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getCreatedAt()
        );
    }
}
