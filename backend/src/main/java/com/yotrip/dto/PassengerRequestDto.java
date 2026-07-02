package com.yotrip.dto;

import jakarta.validation.constraints.NotBlank;

public record PassengerRequestDto(
        String title,
        @NotBlank String firstName,
        @NotBlank String lastName,
        String ageCategory,
        String gender
) {}
