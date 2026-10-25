package com.novara.banking.deposit;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class DepositInterestServiceTest {
    private final DepositInterestService service=new DepositInterestService();
    @Test void simpleInterestUsesHalfEvenMinorUnitRounding(){assertThat(service.fixed(100_000,600,12,"SIMPLE")).isEqualTo(new DepositInterestService.Projection(6_000,106_000));}
    @Test void compoundMonthlyMatchesIndependentWorkedExample(){assertThat(service.fixed(100_000,1200,12,"COMPOUND_MONTHLY")).isEqualTo(new DepositInterestService.Projection(12_683,112_683));}
    @Test void recurringProjectionCompoundsEachCashFlowForItsActualRemainingPeriods(){assertThat(service.recurring(10_000,1200,3,"COMPOUND_MONTHLY")).isEqualTo(new DepositInterestService.Projection(301,30_301));}
    @Test void actualCashFlowAccrualIgnoresFutureAndUsesPostedDates(){var flows=List.of(new DepositInterestService.CashFlow(100_000,LocalDate.of(2024,1,29)),new DepositInterestService.CashFlow(50_000,LocalDate.of(2024,3,1)));assertThat(service.accruedForCashFlows(flows,LocalDate.of(2024,2,29),1200,"COMPOUND_MONTHLY")).isEqualTo(1_000);}
    @Test void invalidInputsAreRejected(){assertThatThrownBy(()->service.fixed(0,650,12,"COMPOUND_MONTHLY")).isInstanceOf(IllegalArgumentException.class);}
}
