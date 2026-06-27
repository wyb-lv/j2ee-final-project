package com.example.tanksolarheaterbe.services;

import com.example.tanksolarheaterbe.dto.DashboardStatsResponse;
import com.example.tanksolarheaterbe.dto.OrderResponse;
import com.example.tanksolarheaterbe.dto.TrendPoint;
import com.example.tanksolarheaterbe.entities.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Computes the admin dashboard revenue figures. All filtering/aggregation lives here, not in the UI. */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int TREND_DAYS = 7;

    private final OrderService orderService;

    public DashboardStatsResponse getStats(LocalDate referenceDate) {

        LocalDate ref = referenceDate != null ? referenceDate : LocalDate.now();

        // Revenue counts placed (non-cancelled) orders only — same rule the dashboard used.
        List<OrderResponse> orders = orderService.getAllOrders(OrderStatus.CANCELLED);

        BigDecimal daily = sumWhere(orders, d -> d.isEqual(ref));
        BigDecimal monthly = sumWhere(orders, d -> d.getYear() == ref.getYear()
                && d.getMonthValue() == ref.getMonthValue());
        BigDecimal annual = sumWhere(orders, d -> d.getYear() == ref.getYear());

        return DashboardStatsResponse.builder()
                .dailyRevenue(daily)
                .monthlyRevenue(monthly)
                .annualRevenue(annual)
                .trend(buildTrend(orders, ref))
                .build();
    }

    /** Daily totals for the {@value #TREND_DAYS} days ending on (and including) the reference date. */
    private List<TrendPoint> buildTrend(List<OrderResponse> orders, LocalDate ref) {

        List<LocalDate> days = new ArrayList<>();
        List<BigDecimal> totals = new ArrayList<>();
        BigDecimal peak = BigDecimal.ZERO;

        for (int i = TREND_DAYS - 1; i >= 0; i--) {
            LocalDate day = ref.minusDays(i);
            BigDecimal total = sumWhere(orders, day::isEqual);
            days.add(day);
            totals.add(total);
            if (total.compareTo(peak) > 0) {
                peak = total;
            }
        }

        BigDecimal denom = peak.compareTo(BigDecimal.ZERO) > 0 ? peak : BigDecimal.ONE;

        List<TrendPoint> trend = new ArrayList<>();
        for (int i = 0; i < days.size(); i++) {
            int pct = totals.get(i)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(denom, 0, RoundingMode.HALF_UP)
                    .intValue();
            trend.add(new TrendPoint(days.get(i), totals.get(i), pct));
        }
        return trend;
    }

    private BigDecimal sumWhere(List<OrderResponse> orders, Predicate<LocalDate> matches) {
        return orders.stream()
                .filter(o -> o.date() != null && matches.test(o.date()))
                .map(OrderResponse::total)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
