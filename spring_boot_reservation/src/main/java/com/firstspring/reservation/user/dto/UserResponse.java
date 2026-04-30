package com.firstspring.reservation.user.dto;

import com.firstspring.reservation.user.entity.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        String role,
        String status) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getStatus());
    }
}
