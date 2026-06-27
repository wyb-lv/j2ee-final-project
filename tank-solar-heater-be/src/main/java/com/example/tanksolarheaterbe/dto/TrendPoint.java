package com.example.tanksolarheaterbe.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One day of the revenue trend: its total and the bar height (% of the window's peak). */
public record TrendPoint(
        LocalDate date,
        BigDecimal total,
        int pct
) {
}
