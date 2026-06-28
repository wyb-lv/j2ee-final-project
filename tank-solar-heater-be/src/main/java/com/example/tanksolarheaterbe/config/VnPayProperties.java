package com.example.tanksolarheaterbe.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * VNPay sandbox/production settings, bound from the {@code app.vnpay.*} keys in
 * application.properties. See https://sandbox.vnpayment.vn for test credentials.
 */
@Component
@ConfigurationProperties(prefix = "app.vnpay")
@Getter
@Setter
public class VnPayProperties {

    /** Merchant terminal code issued by VNPay (vnp_TmnCode). */
    private String tmnCode;

    /** Secret used to sign the payment URL and verify callbacks (HMAC-SHA512). */
    private String hashSecret;

    /** VNPay payment gateway entry point the customer is redirected to. */
    private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    /** Backend URL VNPay redirects the browser back to after payment. */
    private String returnUrl = "http://localhost:8080/api/payments/vnpay/return";

    /** Storefront page the backend forwards the browser to once the result is recorded. */
    private String frontendReturnUrl = "http://localhost:4200/payment-result";

    /** API version expected by the gateway. */
    private String version = "2.1.0";
}
