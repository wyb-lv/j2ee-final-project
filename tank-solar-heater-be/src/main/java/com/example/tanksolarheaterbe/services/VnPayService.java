package com.example.tanksolarheaterbe.services;

import com.example.tanksolarheaterbe.config.VnPayProperties;
import com.example.tanksolarheaterbe.entities.Payment;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Builds VNPay redirect URLs and verifies the signed parameters VNPay sends back.
 * Used as the gateway behind the {@code BANK_TRANSFER} payment method.
 */
@Service
@RequiredArgsConstructor
public class VnPayService {

    private static final DateTimeFormatter VNP_DATE =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final VnPayProperties props;

    /**
     * Builds the URL the customer is redirected to in order to pay {@code payment}.
     * The payment id is used as {@code vnp_TxnRef} so the callback can be matched back.
     */
    public String buildPaymentUrl(Payment payment, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now(VN_ZONE);

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", props.getVersion());
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", props.getTmnCode());
        // VNPay expects the amount in the smallest VND unit (× 100), no decimals.
        params.put("vnp_Amount", payment.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .toBigInteger().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", String.valueOf(payment.getId()));
        params.put("vnp_OrderInfo", "Thanh toan don hang " + payment.getOrderHeader().getId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", props.getReturnUrl());
        params.put("vnp_IpAddr", clientIp(request));
        params.put("vnp_CreateDate", now.format(VNP_DATE));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(VNP_DATE));

        String hashData = buildSignedData(params);
        String secureHash = hmacSha512(props.getHashSecret(), hashData);

        return props.getPayUrl() + "?" + hashData + "&vnp_SecureHash=" + secureHash;
    }

    /**
     * Verifies the {@code vnp_SecureHash} of a callback (return or IPN) by recomputing
     * the HMAC over every {@code vnp_*} field except the hash fields themselves.
     */
    public boolean isValidSignature(Map<String, String> params) {
        String received = params.get("vnp_SecureHash");
        if (received == null || received.isBlank()) {
            return false;
        }
        Map<String, String> signed = new TreeMap<>(params);
        signed.remove("vnp_SecureHash");
        signed.remove("vnp_SecureHashType");

        String expected = hmacSha512(props.getHashSecret(), buildSignedData(signed));
        return expected.equalsIgnoreCase(received);
    }

    /** Joins a sorted, URL-encoded {@code field=value} list with {@code &} (the VNPay hash format). */
    private String buildSignedData(Map<String, String> params) {
        List<String> names = new ArrayList<>(params.keySet());
        Collections.sort(names);
        StringBuilder sb = new StringBuilder();
        for (String name : names) {
            String value = params.get(name);
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(encode(name)).append('=').append(encode(value));
        }
        return sb.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }

    private static String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign VNPay request", e);
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
