package com.yotrip.dto;

import java.math.BigDecimal;

public record CouponResponse(
        String code,
        String description,
        BigDecimal discountAmount,
        boolean valid
) {}
