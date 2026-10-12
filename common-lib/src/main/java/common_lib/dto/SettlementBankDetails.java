package com.payment_gateway.razorpay.common.dto;

/**
 * Holds the merchant bank details used for settlement processing.
 *
 * @param accountNumber bank account number; sensitive and not intended for public responses
 * @param ifsc bank routing code
 * @param accountHolderName account holder name
 */
public record SettlementBankDetails(
        String accountNumber,
        String ifsc,
        String accountHolderName) {

}
