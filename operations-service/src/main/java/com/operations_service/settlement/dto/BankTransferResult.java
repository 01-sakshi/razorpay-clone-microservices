package com.operations_service.settlement.dto;

/**
 * Result of submitting a settlement transfer to the bank processor.
 *
 * @param bankReference processor reference returned by the bank
 */
public record BankTransferResult(
        String bankReference) {

}
