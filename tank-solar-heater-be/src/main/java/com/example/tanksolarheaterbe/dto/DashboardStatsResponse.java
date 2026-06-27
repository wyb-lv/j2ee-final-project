package com.example.tanksolarheaterbe.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/** Pre-computed revenue figures for the admin dashboard (all aggregation done server-side). */
@Builder
public record DashboardStatsResponse(
        BigDecimal dailyRevenue,
        BigDecimal monthlyRevenue,
        BigDecimal annualRevenue,
        List<TrendPoint> trend
) {
}
