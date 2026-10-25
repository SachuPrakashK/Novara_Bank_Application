package com.novara.banking.deposit;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DepositInterestService {
    private static final BigDecimal BASIS_POINTS = new BigDecimal("10000");
    private static final BigDecimal MONTHS = new BigDecimal("12");
    public Projection fixed(long principalMinor,int annualRateBasisPoints,int tenureMonths,String method) {
        requirePositive(principalMinor,annualRateBasisPoints,tenureMonths);
        BigDecimal principal=BigDecimal.valueOf(principalMinor);
        BigDecimal rate=BigDecimal.valueOf(annualRateBasisPoints).divide(BASIS_POINTS,16,RoundingMode.HALF_EVEN);
        BigDecimal maturity;
        if("SIMPLE".equals(method)) maturity=principal.add(principal.multiply(rate).multiply(BigDecimal.valueOf(tenureMonths)).divide(MONTHS,16,RoundingMode.HALF_EVEN));
        else if("COMPOUND_MONTHLY".equals(method)) maturity=principal.multiply(BigDecimal.ONE.add(rate.divide(MONTHS,16,RoundingMode.HALF_EVEN)).pow(tenureMonths));
        else throw new IllegalArgumentException("Unsupported interest method");
        long rounded=maturity.setScale(0,RoundingMode.HALF_EVEN).longValueExact();
        return new Projection(rounded-principalMinor,rounded);
    }
    public Projection recurring(long installmentMinor,int annualRateBasisPoints,int tenureMonths,String method) {
        requirePositive(installmentMinor,annualRateBasisPoints,tenureMonths);
        long maturity=0;
        for(int sequence=1;sequence<=tenureMonths;sequence++) maturity=Math.addExact(maturity,fixed(installmentMinor,annualRateBasisPoints,tenureMonths-sequence,method).maturityMinor());
        long contribution=Math.multiplyExact(installmentMinor,tenureMonths);
        return new Projection(maturity-contribution,maturity);
    }
    public long accruedForCashFlows(List<CashFlow> cashFlows,LocalDate asOf,int annualRateBasisPoints,String method) {
        long total=0;
        for(CashFlow flow:cashFlows){
            if(flow.postedDate().isAfter(asOf)) continue;
            int completeMonths=(int)ChronoUnit.MONTHS.between(flow.postedDate(),asOf);
            total=Math.addExact(total,fixed(flow.amountMinor(),annualRateBasisPoints,completeMonths,method).interestMinor());
        }
        return total;
    }
    private void requirePositive(long amount,int rate,int months){if(amount<=0||rate<=0||months<0)throw new IllegalArgumentException("Positive financial inputs are required");}
    public record Projection(long interestMinor,long maturityMinor) {}
    public record CashFlow(long amountMinor,LocalDate postedDate) {}
}
