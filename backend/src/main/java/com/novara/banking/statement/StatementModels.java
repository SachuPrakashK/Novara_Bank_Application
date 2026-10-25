package com.novara.banking.statement;

import java.time.*;
import java.util.*;

public final class StatementModels { private StatementModels() {}
 public record Account(UUID id,String maskedNumber,String nickname,String type,String currency) {}
 public record Entry(UUID id,String reference,Instant postedAt,LocalDate valueDate,String description,String transactionType,long debitMinor,long creditMinor,long runningBalanceMinor,String currency,String status,boolean receiptAvailable) {}
 public record Summary(UUID accountId,String maskedNumber,String accountType,String currency,LocalDate from,LocalDate to,Instant cutoff,long openingBalanceMinor,long totalDebitMinor,long totalCreditMinor,long closingBalanceMinor,long transactionCount) {}
 public record Page(List<Entry> items,int page,int size,long totalElements,int totalPages,Summary summary) {}
 public record Generation(UUID id,String reference,UUID accountId,LocalDate from,LocalDate to,Instant cutoff,Instant generatedAt,String status) {}
 public record Receipt(UUID id,String reference,Instant postedAt,String type,long amountMinor,String currency,String status,String description,String debitAccountMask,String creditAccountMask) {}
}
