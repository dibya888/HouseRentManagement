package com.rent.dao;

import com.rent.model.ChartItem;
import com.rent.model.DashboardSummary;
import com.rent.model.Repair;
import com.rent.model.ReportRow;
import com.rent.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardDAO {

    public static DashboardSummary getSummary() {
        DashboardSummary summary = new DashboardSummary();

        String currentMonth = YearMonth.now().toString();

        try (Connection conn = DBUtil.connect()) {

            summary.setTotalFlats(getInt(conn, "SELECT COUNT(*) FROM flats"));
            summary.setOccupiedFlats(getInt(conn, "SELECT COUNT(*) FROM flats WHERE status='Occupied'"));
            summary.setAvailableFlats(getInt(conn, "SELECT COUNT(*) FROM flats WHERE status='Available'"));
            summary.setTotalTenants(getInt(conn, "SELECT COUNT(*) FROM tenants"));

            double monthIncome = getDouble(conn,
                    "SELECT COALESCE(SUM(house_rent), 0) FROM rent_archive WHERE bill_month=?",
                    currentMonth);

            double totalDue = getDouble(conn,
                    "SELECT COALESCE(SUM(total - paid_amount), 0) FROM rent_current");

            double monthOwnerRepair = getDouble(conn,
                    """
                    SELECT COALESCE(SUM(cost), 0)
                    FROM repairs
                    WHERE paid_by='Owner'
                    AND repair_date LIKE ?
                    """,
                    currentMonth + "-%");

            double monthUtilityBills = getDouble(conn,
                    """
                    SELECT COALESCE(SUM(electricity + water + gas + other_bills), 0)
                    FROM rent_archive
                    WHERE bill_month=?
                    """,
                    currentMonth);

            summary.setMonthIncome(monthIncome);
            summary.setTotalDue(totalDue);
            summary.setMonthOwnerRepair(monthOwnerRepair);
            summary.setMonthUtilityBills(monthUtilityBills);
            summary.setMonthNetProfit(monthIncome - monthOwnerRepair);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return summary;
    }

    public static List<ChartItem> getMonthlyIncomeChartData() {
        List<ChartItem> list = new ArrayList<>();

        String sql = """
                SELECT bill_month, COALESCE(SUM(house_rent), 0) AS income
                FROM rent_archive
                GROUP BY bill_month
                ORDER BY bill_month DESC
                LIMIT 6
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ChartItem(
                        rs.getString("bill_month"),
                        rs.getDouble("income")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.reverse(list);
        return list;
    }

    public static List<ChartItem> getPaidDueChartData() {
        List<ChartItem> list = new ArrayList<>();

        try (Connection conn = DBUtil.connect()) {
            double paid = getDouble(conn,
                    "SELECT COALESCE(SUM(paid_amount), 0) FROM rent_archive");

            double due = getDouble(conn,
                    "SELECT COALESCE(SUM(total - paid_amount), 0) FROM rent_current");

            list.add(new ChartItem("Paid", paid));
            list.add(new ChartItem("Due", due));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<ChartItem> getOccupancyChartData() {
        List<ChartItem> list = new ArrayList<>();

        try (Connection conn = DBUtil.connect()) {
            double occupied = getDouble(conn,
                    "SELECT COUNT(*) FROM flats WHERE status='Occupied'");

            double available = getDouble(conn,
                    "SELECT COUNT(*) FROM flats WHERE status='Available'");

            list.add(new ChartItem("Occupied", occupied));
            list.add(new ChartItem("Available", available));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<ChartItem> getIncomeRepairChartData() {
        List<ChartItem> list = new ArrayList<>();

        String currentMonth = YearMonth.now().toString();

        try (Connection conn = DBUtil.connect()) {
            double income = getDouble(conn,
                    "SELECT COALESCE(SUM(house_rent), 0) FROM rent_archive WHERE bill_month=?",
                    currentMonth);

            double ownerRepair = getDouble(conn,
                    """
                    SELECT COALESCE(SUM(cost), 0)
                    FROM repairs
                    WHERE paid_by='Owner'
                    AND repair_date LIKE ?
                    """,
                    currentMonth + "-%");

            list.add(new ChartItem("Income", income));
            list.add(new ChartItem("Owner Repair", ownerRepair));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<ReportRow> getRecentDueRows() {
        List<ReportRow> list = new ArrayList<>();

        String sql = """
                SELECT
                    rc.bill_month,
                    rc.due_date,
                    rc.flat_no,
                    t.name AS tenant_name,
                    rc.total,
                    rc.paid_amount,
                    (rc.total - rc.paid_amount) AS due_amount,
                    rc.status
                FROM rent_current rc
                JOIN tenants t ON rc.tenant_id = t.id
                WHERE (rc.total - rc.paid_amount) > 0
                ORDER BY rc.due_date ASC, rc.bill_month DESC
                LIMIT 5
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ReportRow(
                        "Due Rent",
                        rs.getString("bill_month"),
                        rs.getString("due_date"),
                        rs.getString("flat_no"),
                        rs.getString("tenant_name"),
                        rs.getDouble("total"),
                        rs.getDouble("paid_amount"),
                        rs.getDouble("due_amount"),
                        rs.getString("status")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<Repair> getRecentRepairs() {
        List<Repair> list = new ArrayList<>();

        String sql = """
                SELECT *
                FROM repairs
                ORDER BY repair_date DESC, id DESC
                LIMIT 5
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Repair r = new Repair();

                r.setId(rs.getInt("id"));
                r.setFlatNo(rs.getString("flat_no"));
                r.setRepairDate(rs.getString("repair_date"));
                r.setCategory(rs.getString("category"));
                r.setDescription(rs.getString("description"));
                r.setCost(rs.getDouble("cost"));
                r.setPaidBy(rs.getString("paid_by"));
                r.setStatus(rs.getString("status"));
                r.setNotes(rs.getString("notes"));
                r.setCreatedAt(rs.getString("created_at"));

                list.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private static int getInt(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static double getDouble(Connection conn, String sql, String... params) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }
    public static List<ReportRow> getRecentPaymentRows() {
        List<ReportRow> list = new ArrayList<>();

        String sql = """
            SELECT
                ra.bill_month,
                ra.payment_date,
                ra.flat_no,
                t.name AS tenant_name,
                ra.house_rent AS total,
                ra.house_rent AS paid_amount,
                ra.status
            FROM rent_archive ra
            JOIN tenants t ON ra.tenant_id = t.id
            ORDER BY ra.payment_date DESC, ra.id DESC
            LIMIT 5
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ReportRow(
                        "Paid Rent",
                        rs.getString("bill_month"),
                        rs.getString("payment_date"),
                        rs.getString("flat_no"),
                        rs.getString("tenant_name"),
                        rs.getDouble("total"),
                        rs.getDouble("paid_amount"),
                        0,
                        rs.getString("status")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}