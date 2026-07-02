package com.yotrip.service;

import com.yotrip.config.AppProperties;
import com.yotrip.exception.BadRequestException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class RazorpayClient {

    private static final String ORDERS_URL = "https://api.razorpay.com/v1/orders";

    private final AppProperties appProperties;
    private final RestClient restClient;

    public RazorpayClient(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.restClient = RestClient.create();
    }

    public String createOrder(long amountPaise, String receipt) {
        String keyId = appProperties.getRazorpay().getKeyId();
        String keySecret = appProperties.getRazorpay().getKeySecret();
        if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
            throw new BadRequestException("Razorpay credentials are not configured");
        }

        String safeReceipt = receipt.length() > 40 ? receipt.substring(0, 40) : receipt;
        Map<String, Object> request = Map.of(
                "amount", amountPaise,
                "currency", "INR",
                "receipt", safeReceipt
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(ORDERS_URL)
                    .header(HttpHeaders.AUTHORIZATION, basicAuth(keyId, keySecret))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(Map.class);

            if (response == null || response.get("id") == null) {
                throw new BadRequestException("Invalid Razorpay order response");
            }
            return response.get("id").toString();
        } catch (RestClientException ex) {
            throw new BadRequestException("Razorpay order creation failed: " + ex.getMessage());
        }
    }

    private String basicAuth(String keyId, String keySecret) {
        String credentials = keyId + ":" + keySecret;
        return "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
