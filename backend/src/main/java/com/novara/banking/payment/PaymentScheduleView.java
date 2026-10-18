package com.novara.banking.payment;

import java.time.Instant;
import java.util.UUID;
public record PaymentScheduleView(UUID id,String status,String recurrence,String timezone,UUID sourceAccountId,
  String sourceMaskedNumber,String destinationName,String destinationMaskedNumber,long amountMinor,String currency,
  Instant nextExecutionAt,Instant endAt,Integer maximumOccurrences,int executedOccurrences,String description) {}
