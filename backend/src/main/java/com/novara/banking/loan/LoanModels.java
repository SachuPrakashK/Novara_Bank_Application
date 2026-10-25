package com.novara.banking.loan;

import jakarta.validation.constraints.*;
import java.time.*;import java.util.*;

public final class LoanModels {private LoanModels(){}
 public record Product(UUID id,String code,String name,String category,String currency,long minimumPrincipalMinor,long maximumPrincipalMinor,int minimumTenureMonths,int maximumTenureMonths,int annualRateBasisPoints,String policyVersion,String calculationMethod,long processingFeeMinor,boolean sandbox,boolean disbursementAvailable,boolean repaymentAvailable){}
 public record QuoteRequest(@NotNull UUID productId,@Positive long principalMinor,@Min(1) @Max(360) int tenureMonths){}
 public record Installment(int sequence,LocalDate dueDate,long openingPrincipalMinor,long principalMinor,long interestMinor,long feeMinor,long totalDueMinor,long closingPrincipalMinor,String status){}
 public record Quote(UUID id,UUID productId,String productName,String policyVersion,long principalMinor,String currency,int tenureMonths,int annualRateBasisPoints,long emiMinor,long totalInterestMinor,long totalRepaymentMinor,long processingFeeMinor,LocalDate firstInstallmentDate,LocalDate maturityDate,Instant expiresAt,List<Installment> schedule,String assumptions){}
 public record ApplicationRequest(@NotNull UUID quoteId,@Pattern(regexp="PERSONAL|VEHICLE|HOME|EDUCATION|OTHER") String purposeCategory,@AssertTrue boolean customerDeclaration){}
 public record Application(UUID id,String reference,UUID quoteId,String productName,String category,String status,long requestedPrincipalMinor,String currency,int tenureMonths,int annualRateBasisPoints,long illustrativeEmiMinor,Instant submittedAt,String decisionMessage,boolean bookingAvailable){}
}
