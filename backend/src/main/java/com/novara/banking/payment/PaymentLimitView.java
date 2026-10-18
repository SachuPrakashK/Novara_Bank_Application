package com.novara.banking.payment;

public record PaymentLimitView(long perPaymentMinor,long dailyLimitMinor,long usedTodayMinor,long availableTodayMinor,
  Long customerSelectedDailyMinor,String currency,String bankingTimezone) {}
