package com.novara.banking.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreatePaymentIntentRequest(
    @NotNull UUID sourceAccountId,
    @NotNull DestinationType destinationType,
    @NotNull UUID destinationId,
    @Positive long amountMinor,
    @NotBlank String currency,
    @NotBlank String method,
    @Size(max=140) String description
) {
    public enum DestinationType { OWN_ACCOUNT, BENEFICIARY }
}
