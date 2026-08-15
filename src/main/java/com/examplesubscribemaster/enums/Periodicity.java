package com.examplesubscribemaster.enums;

import java.time.LocalDate;

public enum Periodicity {
    WEEKLY, MONTHLY, YEARLY;

    public LocalDate next(LocalDate date) {
        return switch (this) {
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case YEARLY -> date.plusYears(1);
        };
    }
}
