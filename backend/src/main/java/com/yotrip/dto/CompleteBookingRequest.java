package com.yotrip.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CompleteBookingRequest(
        @Min(1) Integer adults,
        @Min(0) Integer children,
        String tripType,
        @NotBlank String travelDate,
        String returnDate,
        @Valid List<PassengerRequestDto> passengers,
        SelectedFlightDto selectedOutboundFlight,
        SelectedFlightDto selectedReturnFlight,
        SelectedFlightDto selectedFlight,
        String departureCity,
        String destinationCity,
        String paymentMethod
) {}
