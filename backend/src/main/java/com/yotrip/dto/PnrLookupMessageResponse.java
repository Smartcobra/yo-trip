package com.yotrip.dto;

public record PnrLookupMessageResponse(
        String message,
        PnrInfoResponse info
) {}
