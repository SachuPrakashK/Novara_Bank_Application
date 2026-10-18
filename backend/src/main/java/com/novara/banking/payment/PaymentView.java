package com.novara.banking.payment;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PaymentView(UUID id,String status,String destinationType,UUID sourceAccountId,String sourceLabel,
  String sourceMaskedNumber,UUID destinationId,String destinationName,String destinationMaskedNumber,long amountMinor,
  long feeMinor,long totalDebitMinor,String currency,String method,String description,boolean requiresStepUp,
  String reference,Instant createdAt,Instant expiresAt,Instant completedAt,List<StatusEvent> timeline) {
    public record StatusEvent(String status,String reasonCode,Instant occurredAt) {}
}
