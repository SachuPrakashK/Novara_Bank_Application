package com.novara.banking.payment;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public record CreatePaymentScheduleRequest(
    @NotNull UUID sourceAccountId,
    @NotNull CreatePaymentIntentRequest.DestinationType destinationType,
    @NotNull UUID destinationId,
    @Positive long amountMinor,
    @NotBlank String currency,
    @Size(max=140) String description,
    @NotNull Instant firstExecutionAt,
    @NotBlank String timezone,
    @NotNull Recurrence recurrence,
    Instant endAt,
    @Positive Integer maximumOccurrences
) { public enum Recurrence { ONCE, WEEKLY, MONTHLY } }
