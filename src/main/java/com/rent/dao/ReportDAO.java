package com.rent.dao;

import com.rent.model.ReportRow;
import com.rent.model.ReportSummary;
import com.rent.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public static ReportSummary getSummary() {
        ReportSummary summary = new ReportSummary();

        String currentMonth = YearMonth.now().toString();
        String currentYear = String.valueOf(Year.now().getValue());

        try (Connection conn = DBUtil.connect()) {

            summary.setTotalIncome(getDouble(conn,
                    "SELECT COALESCE(SUM(paid_amount), 0) FROM rent_archive"));

            summary.setMonthIncome(getDouble(conn,
                    "SELECT COALESCE(SUM(paid_amount), 0) FROM rent_archive WHERE bill_month = ?",
                    currentMonth));

            summary.setYearIncome(getDouble(conn,
                    "SELECT COALESCE(SUM(paid_amount), 0) FROM rent_archive WHERE bill_month LIKE ?",
                    currentYear + "-%"));

            summary.setTotalDue(getDouble(conn,
                    "SELECT COALESCE(SUM(total - paid_amount), 0) FROM rent_current"));

            summary.setTotalFlats(getInt(conn,
                    "SELECT COUNT(*) FROM flats"));

            summary.setOccupiedFlats(getInt(conn,
                    "SELECT COUNT(*) FROM flats WHERE status = 'Occupied'"));

            summary.setAvailableFlats(getInt(conn,
                    "SELECT COUNT(*) FROM flats WHERE status = 'Available'"));

            summary.setTotalTenants(getInt(conn,
                    "SELECT COUNT(*) FROM tenants"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return summary;
    }

    public static List<ReportRow> getAllReportRows() {
        List<ReportRow> list = new ArrayList<>();

        String paidSql = """
            SELECT
                ra.bill_month,
                ra.payment_date AS report_date,
                ra.flat_no,
                t.name AS tenant_name,
                ra.total,
                ra.paid_amount,
                0 AS due,
                ra.status
            FROM rent_archive ra
            JOIN tenants t ON ra.tenant_id = t.id
            ORDER BY ra.bill_month DESC, ra.flat_no
            """;

        String dueSql = """
            SELECT
                rc.bill_month,
                rc.due_date AS report_date,
                rc.flat_no,
                t.name AS tenant_name,
                rc.total,
                rc.paid_amount,
                (rc.total - rc.paid_amount) AS due,
                rc.status
            FROM rent_current rc
            JOIN tenants t ON rc.tenant_id = t.id
            ORDER BY rc.bill_month DESC, rc.flat_no
            """;

        try (Connection conn = DBUtil.connect()) {

            try (PreparedStatement ps = conn.prepareStatement(paidSql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(new ReportRow(
                            "Paid Rent",
                            rs.getString("bill_month"),
                            rs.getString("report_date"),
                            rs.getString("flat_no"),
                            rs.getString("tenant_name"),
                            rs.getDouble("total"),
                            rs.getDouble("paid_amount"),
                            rs.getDouble("due"),
                            rs.getString("status")
                    ));
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(dueSql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(new ReportRow(
                            "Due Rent",
                            rs.getString("bill_month"),
                            rs.getString("report_date"),
                            rs.getString("flat_no"),
                            rs.getString("tenant_name"),
                            rs.getDouble("total"),
                            rs.getDouble("paid_amount"),
                            rs.getDouble("due"),
                            rs.getString("status")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
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

    private static int getInt(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static List<ReportRow> getFlatWiseIncomeRows() {
        List<ReportRow> list = new ArrayList<>();

        String sql = """
            SELECT
                flat_no,
                SUM(total) AS total_amount,
                SUM(paid_amount) AS paid_amount
            FROM rent_archive
            GROUP BY flat_no
            ORDER BY flat_no
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ReportRow(
                        "Flat-wise Income",
                        "",
                        "",
                        rs.getString("flat_no"),
                        "",
                        rs.getDouble("total_amount"),
                        rs.getDouble("paid_amount"),
                        0,
                        "PAID"
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<ReportRow> getMonthlyIncomeRows() {
        List<ReportRow> list = new ArrayList<>();

        String sql = """
            SELECT
                bill_month,
                SUM(total) AS total_amount,
                SUM(paid_amount) AS paid_amount
            FROM rent_archive
            GROUP BY bill_month
            ORDER BY bill_month DESC
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ReportRow(
                        "Monthly Income",
                        rs.getString("bill_month"),
                        "",
                        "",
                        "",
                        rs.getDouble("total_amount"),
                        rs.getDouble("paid_amount"),
                        0,
                        "PAID"
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<ReportRow> getYearlyIncomeRows() {
        List<ReportRow> list = new ArrayList<>();

        String sql = """
            SELECT
                substr(bill_month, 1, 4) AS report_year,
                SUM(total) AS total_amount,
                SUM(paid_amount) AS paid_amount
            FROM rent_archive
            GROUP BY substr(bill_month, 1, 4)
            ORDER BY report_year DESC
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ReportRow(
                        "Yearly Income",
                        rs.getString("report_year"),
                        "",
                        "",
                        "",
                        rs.getDouble("total_amount"),
                        rs.getDouble("paid_amount"),
                        0,
                        "PAID"
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<ReportRow> getTenantWiseIncomeRows() {
        List<ReportRow> list = new ArrayList<>();

        String sql = """
            SELECT
                t.name AS tenant_name,
                ra.flat_no,
                SUM(ra.total) AS total_amount,
                SUM(ra.paid_amount) AS paid_amount
            FROM rent_archive ra
            JOIN tenants t ON ra.tenant_id = t.id
            GROUP BY ra.tenant_id, t.name, ra.flat_no
            ORDER BY t.name
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ReportRow(
                        "Tenant-wise Income",
                        "",
                        "",
                        rs.getString("flat_no"),
                        rs.getString("tenant_name"),
                        rs.getDouble("total_amount"),
                        rs.getDouble("paid_amount"),
                        0,
                        "PAID"
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<ReportRow> getDueRentSummaryRows() {
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
            ORDER BY rc.bill_month DESC, rc.flat_no
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
}