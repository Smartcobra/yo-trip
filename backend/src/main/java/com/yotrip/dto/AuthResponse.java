package com.yotrip.dto;

public record AuthResponse(
        String token,
        UserResponse user,
        boolean isNewUser
) {}
