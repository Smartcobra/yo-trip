package com.yotrip.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyOtpRequest(
        @NotBlank @Pattern(regexp = "^[6-9]\\d{9}$") String mobile,
        @NotBlank @Size(min = 4, max = 6) String otp,
        String name,
        @Email String email,
        @Size(min = 6) String password
) {}
