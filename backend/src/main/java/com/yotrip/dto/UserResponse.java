package com.yotrip.dto;

public record UserResponse(
        Long id,
        String customerId,
        String name,
        String email,
        String mobile
) {}
