package com.novara.banking.deposit;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class DepositModels {
    private DepositModels() {}
    public record Product(UUID id,String code,String name,String type,String currency,long minimumAmountMinor,long maximumAmountMinor,
                          int minimumTenureMonths,int maximumTenureMonths,String interestMethod,String compoundingFrequency,
                          int annualRateBasisPoints,String ratePolicyVersion,int gracePeriodDays,boolean prematureClosureAllowed,boolean renewalAllowed,boolean sandbox) {}
    public record ReviewRequest(@NotNull UUID productId,@Positive long amountMinor,@Min(1) @Max(120) int tenureMonths,
                                @NotNull UUID fundingAccountId,@NotBlank String maturityInstruction) {}
    public record Review(UUID productId,String productName,String depositType,long amountMinor,String currency,int tenureMonths,
                         int annualRateBasisPoints,String interestMethod,String ratePolicyVersion,LocalDate openingDate,LocalDate maturityDate,
                         long projectedInterestMinor,long projectedMaturityMinor,long totalContributionMinor,String maturityInstruction,
                         List<ScheduleItem> schedule,String disclosure) {}
    public record ScheduleItem(int sequence,LocalDate scheduledDate,long amountMinor,String status,String failureReason) {}
    public record Deposit(UUID id,String reference,String productName,String type,String status,String currency,long principalMinor,
                          long totalContributedMinor,long accruedInterestMinor,int annualRateBasisPoints,String ratePolicyVersion,
                          String interestMethod,LocalDate openingDate,LocalDate maturityDate,LocalDate nextDueDate,int tenureMonths,
                          long projectedInterestMinor,long projectedMaturityMinor,String fundingAccountMasked,String maturityInstruction,
                          int completedInstallments,int pendingInstallments,int missedInstallments) {}
    public record Page<T>(List<T> content,int page,int size,long totalElements,int totalPages) {}
}
