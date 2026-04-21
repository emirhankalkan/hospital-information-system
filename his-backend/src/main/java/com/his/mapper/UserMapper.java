package com.his.mapper;

import com.his.dto.response.UserResponse;
import com.his.entity.Role;
import com.his.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    /**
     * User entity -> UserResponse DTO
     * Şifre hiçbir zaman response'a dahil edilmez.
     */
    public UserResponse toResponse(User user) {
        if (user == null) return null;

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        if (user.getRoles() != null) {
            response.setRoles(
                user.getRoles().stream()
                    .map(role -> role.getName().name()) // RoleName enum -> String
                    .collect(Collectors.toSet())
            );
        }

        return response;
    }
}
