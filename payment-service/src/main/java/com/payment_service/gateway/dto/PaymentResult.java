package com.payment_service.gateway.dto;

public sealed interface PaymentResult permits PaymentResult.Failure, PaymentResult.Pending, PaymentResult.Success {
    record Pending(String registrationRef) implements PaymentResult {

    }

    record Failure(String errorCode, String errorDescription) implements PaymentResult {

    }

    record Success(String bankReference) implements PaymentResult {       //Doubt

    }
}
