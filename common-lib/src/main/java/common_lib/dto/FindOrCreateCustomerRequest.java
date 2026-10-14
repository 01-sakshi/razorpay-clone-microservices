package common_lib.dto;

import java.util.UUID;

public record FindOrCreateCustomerRequest(
        UUID merchantId,
        String customerName,
        String phone,
        String email
) {
}
