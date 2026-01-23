package com.nghiashop.ecome_backend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.nghiashop.ecome_backend.utils.HmacUtil;

import jakarta.annotation.PostConstruct;

@Service
public class PayosService {

    private final RestTemplate rest = new RestTemplate();

    @Value("${payos.clientId}")
    private String clientId;

    @Value("${payos.apiKey}")
    private String apiKey;

    @Value("${payos.checksumKey}")
    private String checksumKey;

    @PostConstruct
    public void init() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🔧 PayOS Configuration Check:");
        System.out.println("   - Client ID: " + maskValue(clientId));
        System.out.println("   - API Key: " + maskValue(apiKey));
        System.out.println("   - Checksum Key: " + maskValue(checksumKey));

        if (clientId == null || clientId.trim().isEmpty() ||
                apiKey == null || apiKey.trim().isEmpty() ||
                checksumKey == null || checksumKey.trim().isEmpty()) {
            System.err.println("⚠️  ERROR: PayOS credentials chưa được cấu hình!");
        } else {
            System.out.println("✅ PayOS credentials loaded successfully!");
        }
        System.out.println("=".repeat(50) + "\n");
    }

    private String maskValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "✗ NOT SET";
        }
        if (value.length() <= 8) {
            return "***";
        }
        return value.substring(0, 4) + "..." + value.substring(value.length() - 4);
    }

    public Map<String, Object> createPaymentLink(Long orderCode, Long amount,
            String description, String returnUrl, String cancelUrl, Integer expiredAt) {

        try {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("💳 Tạo PayOS Payment Link");
            System.out.println("=".repeat(60));

            if (clientId == null || apiKey == null || checksumKey == null) {
                throw new RuntimeException("PayOS config thiếu: clientId, apiKey hoặc checksumKey");
            }

            System.out.println("📋 Payment Info:");
            System.out.println("   - Order Code: " + orderCode);
            System.out.println("   - Amount: " + amount + " VNĐ");
            System.out.println("   - Description: " + description);

            Map<String, String> sigParams = new HashMap<>();
            sigParams.put("amount", amount.toString());
            sigParams.put("cancelUrl", cancelUrl);
            sigParams.put("description", description);
            sigParams.put("orderCode", orderCode.toString());
            sigParams.put("returnUrl", returnUrl);

            String signatureData = HmacUtil.buildSignatureString(sigParams);
            String signature = HmacUtil.hmacSha256(checksumKey, signatureData);

            System.out.println("🔐 Signature: " + signature);

            Map<String, Object> body = new HashMap<>();
            body.put("orderCode", orderCode);
            body.put("amount", amount);
            body.put("description", description);
            body.put("returnUrl", returnUrl);
            body.put("cancelUrl", cancelUrl);
            body.put("signature", signature);
            if (expiredAt != null) {
                body.put("expiredAt", expiredAt);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            System.out.println("🌐 Calling PayOS API...");

            ResponseEntity<Map> res = rest.postForEntity(
                    "https://api-merchant.payos.vn/v2/payment-requests",
                    entity,
                    Map.class);

            Map<String, Object> responseBody = res.getBody();

            System.out.println("📥 Response Status: " + res.getStatusCode());
            System.out.println("📥 Response Body: " + responseBody);

            if (responseBody == null) {
                throw new RuntimeException("PayOS trả về response null");
            }

            String code = responseBody.get("code") != null ? responseBody.get("code").toString() : null;

            if (!"00".equals(code)) {
                String errorMsg = (String) responseBody.get("desc");
                throw new RuntimeException("PayOS error: " +
                        (errorMsg != null ? errorMsg : "Unknown error"));
            }

            System.out.println("✅ Payment link created successfully!");
            System.out.println("=".repeat(60) + "\n");

            return responseBody;

        } catch (Exception e) {
            System.err.println("\n❌ PayOS API Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi gọi PayOS API: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getPaymentStatus(Long orderCode) {
        try {
            System.out.println("🔍 Checking payment status for order: " + orderCode);

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = "https://api-merchant.payos.vn/v2/payment-requests/" + orderCode;

            ResponseEntity<Map> res = rest.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class);

            Map<String, Object> responseBody = res.getBody();
            System.out.println("✅ Payment status response: " + responseBody);

            return responseBody;

        } catch (Exception e) {
            System.err.println("❌ Error getting payment status: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi kiểm tra trạng thái thanh toán: " + e.getMessage(), e);
        }
    }
}