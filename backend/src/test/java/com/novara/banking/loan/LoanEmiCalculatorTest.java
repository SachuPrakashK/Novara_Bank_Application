package com.novara.banking.loan;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

class LoanEmiCalculatorTest {
    private final LoanEmiCalculator calculator = new LoanEmiCalculator();

    @Test void zeroRateDividesAndReconcilesPrincipalExactly() {
        var result = calculator.calculate(120_000, 0, 12, LocalDate.of(2026, 1, 31));
        assertThat(result.emiMinor()).isEqualTo(10_000);
        assertThat(result.totalInterestMinor()).isZero();
        assertThat(result.totalRepaymentMinor()).isEqualTo(120_000);
        assertThat(result.schedule()).hasSize(12);
        assertThat(result.schedule().getLast().closingPrincipalMinor()).isZero();
        assertThat(result.schedule().get(1).dueDate()).isEqualTo(LocalDate.of(2026, 2, 28));
    }

    @Test void reducingBalanceUsesHalfEvenMinorUnitRounding() {
        var result = calculator.calculate(100_000, 1200, 12, LocalDate.of(2026, 2, 15));
        assertThat(result.emiMinor()).isEqualTo(8_885);
        assertThat(result.schedule()).extracting("openingPrincipalMinor").first().isEqualTo(100_000L);
        assertThat(result.schedule().stream().mapToLong(LoanModels.Installment::principalMinor).sum()).isEqualTo(100_000);
        assertThat(result.schedule().getLast().closingPrincipalMinor()).isZero();
        assertThat(result.totalRepaymentMinor()).isEqualTo(100_000 + result.totalInterestMinor());
    }

    @Test void calculationIsDeterministicAndRejectsInvalidTerms() {
        var date = LocalDate.of(2026, 5, 1);
        assertThat(calculator.calculate(999_999, 725, 37, date)).isEqualTo(calculator.calculate(999_999, 725, 37, date));
        assertThatThrownBy(() -> calculator.calculate(0, 725, 12, date)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calculator.calculate(1_000, -1, 12, date)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calculator.calculate(1_000, 1, 0, date)).isInstanceOf(IllegalArgumentException.class);
    }
}
