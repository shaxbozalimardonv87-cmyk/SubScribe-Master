package com.examplesubscribemaster.enums;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodicityTest {

    private final LocalDate date = LocalDate.of(2026, 1, 31);

    @Test
    void weekly_addsOneWeek() {
        assertThat(Periodicity.WEEKLY.next(date)).isEqualTo(date.plusWeeks(1));
    }

    @Test
    void monthly_addsOneMonth() {
        assertThat(Periodicity.MONTHLY.next(date)).isEqualTo(date.plusMonths(1));
    }

    @Test
    void yearly_addsOneYear() {
        assertThat(Periodicity.YEARLY.next(date)).isEqualTo(date.plusYears(1));
    }
}
