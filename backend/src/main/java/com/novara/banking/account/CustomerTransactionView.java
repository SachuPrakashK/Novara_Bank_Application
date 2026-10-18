package com.novara.banking.account;
import java.time.Instant;
import java.util.UUID;
public record CustomerTransactionView(UUID id, String counterparty, Instant occurredAt, String maskedAccount, Money amount, String direction, String status, String reference, String category, String note) {
    public record Money(long amountMinor, String currency) {}
}
