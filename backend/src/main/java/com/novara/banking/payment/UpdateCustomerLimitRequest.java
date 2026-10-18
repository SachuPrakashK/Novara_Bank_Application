package com.novara.banking.payment;
import jakarta.validation.constraints.Positive;
public record UpdateCustomerLimitRequest(@Positive long dailyLimitMinor) {}
