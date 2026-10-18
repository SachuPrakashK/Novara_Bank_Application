package com.novara.banking.account;
import java.time.Instant;
import java.util.UUID;
public record AccountTransactionView(UUID journalId, String reference, Instant postedAt, String description, String direction, long amountMinor, String currency) {}
