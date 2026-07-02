package com.yotrip.dto;

import java.util.List;

public record PnrPassengerDto(
        String firstName,
        String lastName,
        String passengerType
) {}
