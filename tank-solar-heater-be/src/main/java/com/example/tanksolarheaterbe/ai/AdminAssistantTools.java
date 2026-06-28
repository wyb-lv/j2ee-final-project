package com.example.tanksolarheaterbe.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class AdminAssistantTools {

    private static final Logger log = LoggerFactory.getLogger(AdminAssistantTools.class);

    private static final int MAX_ROWS = 200;

    /** Statements must start with one of these (a read-only query). */
    private static final Pattern READ_ONLY_START =
            Pattern.compile("^(select|with)\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern FORBIDDEN = Pattern.compile(
            "\\b(insert|update|delete|merge|drop|create|alter|truncate|rename|grant|revoke|"
                    + "exec|execute|call|backup|restore|shutdown|dbcc|into)\\b|sp_|xp_",
            Pattern.CASE_INSENSITIVE);

    private final JdbcTemplate jdbc;

    public AdminAssistantTools(DataSource dataSource) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.setMaxRows(MAX_ROWS);   // safety net at the driver level
        this.jdbc = template;
    }

    @Tool(description = """
            FALLBACK ONLY. Prefer the specific tools (getTotalRevenue, getRevenueByMonth,
            getPaymentBreakdown, getOrderBreakdown, getTopProducts, getStoreOverview) whenever they
            fit the question — they are guaranteed correct. Use this raw-SQL tool ONLY for questions
            none of those cover.
            Run a single READ-ONLY SQL query (SELECT only) against the database and return the rows.
            Rules: SQL Server dialect; one SELECT statement only; no semicolons; use TOP (not LIMIT)
            to limit rows. Writing/altering data is blocked.

            Schema (SQL Server, schema dbo). Tables/camelCase columns are case-sensitive — wrap them in
            double quotes. The "Payments" table instead uses snake_case columns (no quotes needed):
              "Account"(id, email, name, phone, role, enabled)
              "Brand"(id, name)
              "Category"(id, name)
              "Product"(id, name, description, price, discount, "categoryId" -> "Category".id, "brandId" -> "Brand".id, "imageUrl")
              "OrderHeader"(id, date, status, address, "customerId" -> "Account".id, "employeeId")
              "OrderDetail"(id, "orderHeaderId" -> "OrderHeader".id, "productId" -> "Product".id, quantity, price, discount)
              "Payments"(id, order_id -> "OrderHeader".id, payment_method, payment_status, amount, transaction_id, created_at, paid_at)
            Notes: order status is PENDING/SHIPPING/DONE/CANCELLED; payment_status is PENDING/PAID/FAILED/REFUNDED;
            role is 'admin' or 'customer'.
            REVENUE definition (must match the admin dashboard): revenue = SUM(od.price * od.quantity)
            over "OrderDetail" joined to non-cancelled "OrderHeader" (oh.status <> 'CANCELLED'), grouped/
            filtered by the order date oh.date. Discounts are NOT applied, and it is based on orders
            (NOT on the Payments table). Only add a date filter on oh.date when a specific period is asked.""")
    public String runQuery(
            @ToolParam(description = "A single read-only SQL SELECT statement.") String sql) {

        if (sql == null || sql.isBlank()) {
            return "Error: empty query.";
        }

        String query = sql.trim();
        if (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1).trim();
        }
        if (query.contains(";")) {
            return "Rejected: only a single statement is allowed (no semicolons).";
        }
        if (!READ_ONLY_START.matcher(query).find()) {
            return "Rejected: only read-only SELECT queries are allowed.";
        }
        if (FORBIDDEN.matcher(query).find()) {
            return "Rejected: the query contains a forbidden (data-changing or DDL) keyword. "
                    + "Only SELECT is permitted.";
        }

        log.info("AI SQL tool executing: {}", query);
        try {
            List<Map<String, Object>> rows = jdbc.query(query, new ColumnMapRowMapper());
            log.info("AI SQL tool returned {} row(s)", rows.size());
            return formatRows(rows);
        } catch (Exception e) {
            log.warn("AI SQL tool query failed: {}", e.getMessage());
            return "Query failed: " + e.getMessage();
        }
    }

    // ---- Purpose-built tools: correct SQL baked in so the model can't get it wrong. ----

    /**
     * Revenue, defined EXACTLY like the admin dashboard: the total value of non-cancelled orders,
     * summed by order date. Order value = price * quantity (discounts are not applied, matching
     * OrderService.mapToResponse). Append a date filter on oh.date for a specific period.
     */
    private static final String REVENUE_BASE =
            "SELECT COALESCE(SUM(od.price * od.quantity), 0) "
                    + "FROM \"OrderDetail\" od "
                    + "JOIN \"OrderHeader\" oh ON oh.id = od.\"orderHeaderId\" "
                    + "WHERE oh.status <> 'CANCELLED'";

    @Tool(description = """
            Returns the current SERVER date and time: the full date plus day, month, year as numbers,
            and the weekday. Call this whenever the question involves 'today' / 'hôm nay',
            'this month' / 'tháng này', 'this year' / 'năm nay', or any relative date, so you use the
            correct day/month/year instead of guessing.""")
    public String getCurrentDate() {
        log.info("AI tool [currentDate]");
        try {
            Map<String, Object> r = jdbc.queryForMap(
                    "SELECT CONVERT(varchar(10), GETDATE(), 23) AS today, "
                            + "DAY(GETDATE()) AS day, MONTH(GETDATE()) AS month, "
                            + "YEAR(GETDATE()) AS year, DATENAME(weekday, GETDATE()) AS weekday");
            String result = "today=" + r.get("today") + " | day=" + r.get("day")
                    + " | month=" + r.get("month") + " | year=" + r.get("year")
                    + " | weekday=" + r.get("weekday");
            log.info("AI tool [currentDate] -> {}", result);
            return result;
        } catch (Exception e) {
            log.warn("AI tool [currentDate] failed: {}", e.getMessage());
            return "Query failed: " + e.getMessage();
        }
    }

    @Tool(description = """
            Total revenue across ALL time. Matches the dashboard: total value of every non-cancelled
            order. Use for general revenue questions ('total revenue', 'doanh thu', 'tổng doanh thu').
            Not filtered by date.""")
    public String getTotalRevenue() {
        return revenueResult("totalRevenue", REVENUE_BASE);
    }

    @Tool(description = """
            Revenue for ONE specific, explicitly-named month (e.g. 'doanh thu tháng 6 năm 2026').
            Do NOT use this for relative words like 'this month' — use getRevenueThisMonth instead,
            because you do not reliably know today's date.""")
    public String getRevenueByMonth(
            @ToolParam(description = "4-digit year, e.g. 2026") int year,
            @ToolParam(description = "month number, 1-12") int month) {
        return revenueResult("revenueByMonth",
                REVENUE_BASE + " AND YEAR(oh.date) = ? AND MONTH(oh.date) = ?", year, month);
    }

    @Tool(description = """
            Revenue for the CURRENT month (computed on the server). Use for 'doanh thu tháng này',
            'this month's revenue'. The date is resolved server-side, so you do NOT need to know
            today's date. Equals the dashboard's "Monthly revenue" card.""")
    public String getRevenueThisMonth() {
        return revenueResult("revenueThisMonth",
                REVENUE_BASE + " AND YEAR(oh.date) = YEAR(GETDATE()) "
                        + "AND MONTH(oh.date) = MONTH(GETDATE())");
    }

    @Tool(description = """
            Revenue for TODAY (computed on the server). Use for 'doanh thu hôm nay', 'today's revenue'.
            Equals the dashboard's "Daily revenue" card.""")
    public String getRevenueToday() {
        return revenueResult("revenueToday",
                REVENUE_BASE + " AND oh.date = CAST(GETDATE() AS DATE)");
    }

    @Tool(description = """
            Revenue for the CURRENT year (computed on the server). Use for 'doanh thu năm nay',
            'this year's revenue'. Equals the dashboard's "Annual revenue" card.""")
    public String getRevenueThisYear() {
        return revenueResult("revenueThisYear",
                REVENUE_BASE + " AND YEAR(oh.date) = YEAR(GETDATE())");
    }

    @Tool(description = """
            Breakdown of payments by status: count and total amount for each payment_status
            (PENDING / PAID / FAILED / REFUNDED). Use for 'payment status', 'how many paid/failed'.""")
    public String getPaymentBreakdown() {
        return runInternal("paymentBreakdown",
                "SELECT payment_status, COUNT(*) AS count, COALESCE(SUM(amount), 0) AS total_amount "
                        + "FROM payments GROUP BY payment_status");
    }

    @Tool(description = """
            Breakdown of orders by status: number of orders for each status
            (PENDING / SHIPPING / DONE / CANCELLED).""")
    public String getOrderBreakdown() {
        return runInternal("orderBreakdown",
                "SELECT status, COUNT(*) AS count FROM \"OrderHeader\" GROUP BY status");
    }

    /** Counts ALL orders (any status), counted by order date. Append a date filter on oh.date. */
    private static final String ORDER_COUNT_BASE = "SELECT COUNT(*) FROM \"OrderHeader\" oh";

    @Tool(description = """
            Total number of orders across ALL time (every status). Use for 'tổng số đơn hàng',
            'how many orders'.""")
    public String getTotalOrderCount() {
        return countResult("orderCountTotal", ORDER_COUNT_BASE);
    }

    @Tool(description = """
            Number of orders placed in the CURRENT month (computed on the server). Use for
            'số đơn hàng tháng này', 'orders this month'.""")
    public String getOrderCountThisMonth() {
        return countResult("orderCountThisMonth",
                ORDER_COUNT_BASE + " WHERE YEAR(oh.date) = YEAR(GETDATE()) "
                        + "AND MONTH(oh.date) = MONTH(GETDATE())");
    }

    @Tool(description = """
            Number of orders placed TODAY (computed on the server). Use for 'số đơn hàng hôm nay',
            'orders today'.""")
    public String getOrderCountToday() {
        return countResult("orderCountToday",
                ORDER_COUNT_BASE + " WHERE oh.date = CAST(GETDATE() AS DATE)");
    }

    @Tool(description = """
            Number of orders placed in the CURRENT year (computed on the server). Use for
            'số đơn hàng năm nay', 'orders this year'.""")
    public String getOrderCountThisYear() {
        return countResult("orderCountThisYear",
                ORDER_COUNT_BASE + " WHERE YEAR(oh.date) = YEAR(GETDATE())");
    }

    @Tool(description = """
            Number of orders placed in ONE specific, explicitly-named month. Use only when the user
            names a month/year; for 'this month' use getOrderCountThisMonth.""")
    public String getOrderCountByMonth(
            @ToolParam(description = "4-digit year, e.g. 2026") int year,
            @ToolParam(description = "month number, 1-12") int month) {
        return countResult("orderCountByMonth",
                ORDER_COUNT_BASE + " WHERE YEAR(oh.date) = ? AND MONTH(oh.date) = ?", year, month);
    }

    @Tool(description = """
            Best-selling products ranked by units sold, with revenue per product. Use for
            'top products', 'best sellers', 'sản phẩm bán chạy'.""")
    public String getTopProducts(
            @ToolParam(description = "how many products to return; defaults to 5") Integer limit) {
        int top = (limit == null || limit <= 0) ? 5 : limit;
        return runInternal("topProducts",
                "SELECT TOP (" + top + ") p.name AS product, SUM(od.quantity) AS units_sold, "
                        + "COALESCE(SUM(od.price * od.quantity), 0) AS revenue "
                        + "FROM \"OrderDetail\" od "
                        + "JOIN \"Product\" p ON p.id = od.\"productId\" "
                        + "JOIN \"OrderHeader\" oh ON oh.id = od.\"orderHeaderId\" "
                        + "WHERE oh.status <> 'CANCELLED' "
                        + "GROUP BY p.name ORDER BY units_sold DESC");
    }

    @Tool(description = """
            High-level store snapshot in one row: number of products, number of customers, total
            orders, and total revenue (non-cancelled orders). Use for 'overview', 'tổng quan',
            'dashboard'.""")
    public String getStoreOverview() {
        return runInternal("storeOverview",
                "SELECT "
                        + "(SELECT COUNT(*) FROM \"Product\") AS products, "
                        + "(SELECT COUNT(*) FROM \"Account\" WHERE role = 'customer') AS customers, "
                        + "(SELECT COUNT(*) FROM \"OrderHeader\") AS orders, "
                        + "(SELECT COALESCE(SUM(od.price * od.quantity), 0) "
                        + " FROM \"OrderDetail\" od "
                        + " JOIN \"OrderHeader\" oh ON oh.id = od.\"orderHeaderId\" "
                        + " WHERE oh.status <> 'CANCELLED') AS total_revenue");
    }

    /**
     * Runs a revenue SUM query (single value) and returns ONE clearly-labelled number, so the model
     * can't mistake a secondary column (like a count) for the revenue.
     */
    private String revenueResult(String label, String sql, Object... args) {
        log.info("AI tool [{}]: {}", label, sql);
        try {
            BigDecimal revenue = jdbc.queryForObject(sql, BigDecimal.class, args);
            if (revenue == null) {
                revenue = BigDecimal.ZERO;
            }
            log.info("AI tool [{}] -> {}", label, revenue);
            return "Doanh thu (tổng giá trị các đơn hàng chưa huỷ) = "
                    + revenue.toPlainString() + " VND";
        } catch (Exception e) {
            log.warn("AI tool [{}] failed: {}", label, e.getMessage());
            return "Query failed: " + e.getMessage();
        }
    }

    /** Runs a COUNT(*) query and returns ONE clearly-labelled number. */
    private String countResult(String label, String sql, Object... args) {
        log.info("AI tool [{}]: {}", label, sql);
        try {
            Long count = jdbc.queryForObject(sql, Long.class, args);
            long n = count != null ? count : 0L;
            log.info("AI tool [{}] -> {}", label, n);
            return "Số đơn hàng = " + n;
        } catch (Exception e) {
            log.warn("AI tool [{}] failed: {}", label, e.getMessage());
            return "Query failed: " + e.getMessage();
        }
    }

    /** Runs a fixed (developer-authored) parameterized query and formats the result. */
    private String runInternal(String label, String sql, Object... args) {
        log.info("AI tool [{}]: {}", label, sql);
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
            log.info("AI tool [{}] returned {} row(s)", label, rows.size());
            return formatRows(rows);
        } catch (Exception e) {
            log.warn("AI tool [{}] failed: {}", label, e.getMessage());
            return "Query failed: " + e.getMessage();
        }
    }

    private String formatRows(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "No rows returned.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(rows.size()).append(rows.size() == MAX_ROWS ? "+ (capped) rows:\n" : " row(s):\n");
        for (Map<String, Object> row : rows) {
            sb.append("- ");
            boolean first = true;
            for (Map.Entry<String, Object> col : row.entrySet()) {
                if (!first) sb.append(" | ");
                sb.append(col.getKey()).append('=').append(col.getValue());
                first = false;
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
