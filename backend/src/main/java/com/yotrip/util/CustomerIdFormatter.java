package com.yotrip.util;

public final class CustomerIdFormatter {

    private static final String PREFIX = "CUST";

    private CustomerIdFormatter() {}

    public static String format(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required to generate customer id");
        }
        return PREFIX + String.format("%08d", userId);
    }

    public static Long parseUserId(String customerId) {
        if (customerId == null || !customerId.startsWith(PREFIX)) {
            return null;
        }
        try {
            return Long.parseLong(customerId.substring(PREFIX.length()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
