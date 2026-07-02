package com.yotrip.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Razorpay razorpay = new Razorpay();
    private Otp otp = new Otp();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public Razorpay getRazorpay() {
        return razorpay;
    }

    public void setRazorpay(Razorpay razorpay) {
        this.razorpay = razorpay;
    }

    public Otp getOtp() {
        return otp;
    }

    public void setOtp(Otp otp) {
        this.otp = otp;
    }

    public static class Jwt {
        private String secret;
        private long expirationMs = 86400000;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpirationMs() {
            return expirationMs;
        }

        public void setExpirationMs(long expirationMs) {
            this.expirationMs = expirationMs;
        }
    }

    public static class Cors {
        private String allowedOrigins = "http://localhost:8080";

        public String getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Razorpay {
        private String mode = "test";
        private String keyId;
        private String keySecret;

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public boolean isTestMode() {
            return "test".equalsIgnoreCase(mode);
        }

        public String getKeyId() {
            return keyId;
        }

        public void setKeyId(String keyId) {
            this.keyId = keyId;
        }

        public String getKeySecret() {
            return keySecret;
        }

        public void setKeySecret(String keySecret) {
            this.keySecret = keySecret;
        }
    }

    public static class Otp {
        private int expiryMinutes = 5;

        public int getExpiryMinutes() {
            return expiryMinutes;
        }

        public void setExpiryMinutes(int expiryMinutes) {
            this.expiryMinutes = expiryMinutes;
        }
    }
}
