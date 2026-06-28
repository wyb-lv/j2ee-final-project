package com.example.tanksolarheaterbe.dto;

/** Outcome of verifying and applying a VNPay return/IPN callback. */
public record VnPayConfirmation(Status status, Long paymentId, Integer orderId) {

    public enum Status {
        /** Payment verified and marked PAID by this call. */
        SUCCESS,
        /** Payment was already PAID before this call (duplicate callback). */
        ALREADY_CONFIRMED,
        /** Gateway reported the payment failed/cancelled; marked FAILED. */
        FAILED,
        /** Signature did not match — the callback is untrusted. */
        INVALID_SIGNATURE,
        /** No payment matched vnp_TxnRef. */
        NOT_FOUND,
        /** Returned amount did not match the recorded payment amount. */
        AMOUNT_MISMATCH
    }

    public boolean paid() {
        return status == Status.SUCCESS || status == Status.ALREADY_CONFIRMED;
    }
}
