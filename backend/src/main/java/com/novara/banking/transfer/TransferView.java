package com.novara.banking.transfer;
import java.time.Instant;
import java.util.UUID;
public record TransferView(UUID id,String status,UUID sourceAccountId,UUID destinationAccountId,long amountMinor,String currency,String reference,Instant createdAt,Instant completedAt) {}
