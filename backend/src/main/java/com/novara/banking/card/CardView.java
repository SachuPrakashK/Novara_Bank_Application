package com.novara.banking.card;
import java.time.Instant; import java.util.*;
public record CardView(UUID id,String product,String tier,String fundingType,String cardholderName,String maskedReference,String status,
 UUID linkedAccountId,String linkedAccountName,String linkedAccountMasked,Integer expiryMonth,Integer expiryYear,Controls controls,boolean sandboxEnforced) {
 public record Controls(boolean atmEnabled,boolean onlineEnabled,boolean posEnabled,boolean contactlessEnabled,boolean internationalEnabled,long atmLimitMinor,long onlineLimitMinor,long posLimitMinor,long contactlessLimitMinor,long dailyLimitMinor,long version){}
 public record Activity(UUID id,String merchantDescription,long amountMinor,String currency,String status,String transactionType,String maskedReference,Instant occurredAt){}
 public record ActivityPage(List<Activity> content,int page,int size,long totalElements,int totalPages){}
 public record Service(UUID id,String requestType,String status,String notes,Instant createdAt){}
}
