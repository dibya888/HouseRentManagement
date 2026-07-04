package com.rent.dao;

import com.rent.model.ReportRow;
import com.rent.model.ReportSummary;
import com.rent.util.DBUtil;
import com.rent.dao.RepairDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReportDAO {

    public static ReportSummary getSummary() {
        ReportSummary summary = new ReportSummary();

        String currentMonth = YearMonth.now().toString();
        String currentYear = String.valueOf(Year.now().getValue());

        double totalRepair = RepairDAO.getTotalRepairCost();
        double monthRepair = RepairDAO.getMonthRepairCost(currentMonth);
        double yearRepair = RepairDAO.getYearRepairCost(currentYear);

        double ownerPaidRepair = RepairDAO.getOwnerPaidTotalRepairCost();
        double tenantPaidRepair = RepairDAO.getTenantPaidTotalRepairCost();

        try (Connection conn = DBUtil.connect()) {

            // Income = House Rent only
            summary.setTotalIncome(getDouble(conn,
                    "SELECT COALESCE(SUM(MAX(house_rent - discount, 0)), 0) FROM rent_archive"));

            summary.setMonthIncome(getDouble(conn,
                    "SELECT COALESCE(SUM(MAX(house_rent - discount, 0)), 0) FROM rent_archive WHERE bill_month = ?",
                    currentMonth));

            summary.setYearIncome(getDouble(conn,
                    "SELECT COALESCE(SUM(MAX(house_rent - discount, 0)), 0) FROM rent_archive WHERE bill_month LIKE ?",
                    currentYear + "-%"));

            // Due = full unpaid receivable from tenant
            summary.setTotalDue(getDouble(conn,
                    "SELECT COALESCE(SUM(total - paid_amount), 0) FROM rent_current"));

            // Flat/Tenant counts
            summary.setTotalFlats(getInt(conn,
                    "SELECT COUNT(*) FROM flats"));

            summary.setOccupiedFlats(getInt(conn,
                    "SELECT COUNT(*) FROM flats WHERE status = 'Occupied'"));

            summary.setAvailableFlats(getInt(conn,
                    "SELECT COUNT(*) FROM flats WHERE status = 'Available'"));

            summary.setTotalTenants(getInt(conn,
                    "SELECT COUNT(*) FROM tenants"));

            // Repair
            summary.setTotalRepairCost(totalRepair);
            summary.setMonthRepairCost(monthRepair);
            summary.setYearRepairCost(yearRepair);
            summary.setOwnerPaidRepairCost(ownerPaidRepair);
            summary.setTenantPaidRepairCost(tenantPaidRepair);

            // Utility Bills = electricity + water + gas + other_bills
            summary.setTotalUtilityBills(getDouble(conn,
                    """
                    SELECT COALESCE(SUM(electricity + water + gas + other_bills), 0)
                    FROM rent_archive
                    """));

            summary.setMonthUtilityBills(getDouble(conn,
                    """
                    SELECT COALESCE(SUM(electricity + water + gas + other_bills), 0)
                    FROM rent_archive
                    WHERE bill_month = ?
                    """,
                    currentMonth));

            summary.setYearUtilityBills(getDouble(conn,
                    """
                    SELECT COALESCE(SUM(electricity + water + gas + other_bills), 0)
                    FROM rent_archive
                    WHERE bill_month LIKE ?
                    """,
                    currentYear + "-%"));

            summary.setElectricityBills(getDouble(conn,
                    "SELECT COALESCE(SUM(electricity), 0) FROM rent_archive"));

            summary.setWaterBills(getDouble(conn,
                    "SELECT COALESCE(SUM(water), 0) FROM rent_archive"));

            summary.setGasBills(getDouble(conn,
                    "SELECT COALESCE(SUM(gas), 0) FROM rent_archive"));

            summary.setOtherBills(getDouble(conn,
                    "SELECT COALESCE(SUM(other_bills), 0) FROM rent_archive"));

            // Net Income = House Rent Income - Owner Paid Repair
            summary.setNetProfit(summary.getTotalIncome() - ownerPaidRepair);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return summary;
    }

    public static List<ReportRow> getUtilityBillReportRows() {
        List<ReportRow> list = new ArrayList<>();

        String sql = """
            SELECT
                ra.bill_month,
                ra.payment_date AS report_date,
                ra.flat_no,
                t.name AS tenant_name,
                ra.electricity,
                ra.water,
                ra.gas,
                ra.other_bills,
                (ra.electricity + ra.water + ra.gas + ra.other_bills) AS utility_total,
                ra.status
            FROM rent_archive ra
            JOIN tenants t ON ra.tenant_id = t.id
            ORDER BY ra.bill_month DESC, ra.flat_no
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                double electricity = rs.getDouble("electricity");
                double water = rs.getDouble("water");
                double gas = rs.getDouble("gas");
                double other = rs.getDouble("other_bills");
                double utilityTotal = rs.getDouble("utility_total");

                String breakdown =
                        "Electricity: " + moneyText(electricity)
                                + ", Water: " + moneyText(water)
                                + ", Gas: " + moneyText(gas)
                                + ", Other: " + moneyText(other);

                list.add(new ReportRow(
                        "Utility Bills",
                        rs.getString("bill_month"),
                        rs.getString("report_date"),
                        rs.getString("flat_no"),
                        rs.getString("tenant_name"),
                        utilityTotal,
                        0,
                        0,
                        rs.getString("status"),
                        breakdown
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<ReportRow> getAllReportRows() {
        List<ReportRow> list = new ArrayList<>();

        String paidSql = """
            SELECT
                ra.bill_month,
                ra.payment_date AS report_date,
                ra.flat_no,
                t.name AS tenant_name,
                MAX(ra.house_rent - ra.discount, 0) AS total,
                MAX(ra.house_rent - ra.discount, 0) AS paid_amount,
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

        String paidSql = """
            SELECT
                flat_no,
                SUM(MAX(house_rent - discount, 0)) AS total_amount,
                SUM(MAX(house_rent - discount, 0)) AS paid_amount
            FROM rent_archive
            GROUP BY flat_no
            """;

        // Outstanding (unpaid/partially paid) rent for each flat lives in
        // rent_current, not rent_archive. Merged below into the same row
        // as the paid total, so a flat with one paid month and one due
        // month shows both figures together instead of two split rows.
        String dueSql = """
            SELECT
                flat_no,
                SUM(total) AS total_amount,
                SUM(paid_amount) AS paid_amount,
                SUM(total - paid_amount) AS due_amount
            FROM rent_current
            WHERE (total - paid_amount) > 0
            GROUP BY flat_no
            """;

        Map<String, double[]> merged = new LinkedHashMap<>(); // flat_no -> {total, paid, due}

        try (Connection conn = DBUtil.connect()) {

            try (PreparedStatement ps = conn.prepareStatement(paidSql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    double[] agg = merged.computeIfAbsent(rs.getString("flat_no"), k -> new double[3]);
                    agg[0] += rs.getDouble("total_amount");
                    agg[1] += rs.getDouble("paid_amount");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(dueSql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    double[] agg = merged.computeIfAbsent(rs.getString("flat_no"), k -> new double[3]);
                    agg[0] += rs.getDouble("total_amount");
                    agg[1] += rs.getDouble("paid_amount");
                    agg[2] += rs.getDouble("due_amount");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> flatNos = new ArrayList<>(merged.keySet());
        flatNos.sort(String::compareTo);

        for (String flatNo : flatNos) {
            double[] agg = merged.get(flatNo);
            list.add(new ReportRow(
                    "Flat-wise Income",
                    "",
                    "",
                    flatNo,
                    "",
                    agg[0],
                    agg[1],
                    agg[2],
                    agg[2] > 0 ? "DUE" : "PAID"
            ));
        }

        return list;
    }

    public static List<ReportRow> getMonthlyIncomeRows() {
        List<ReportRow> list = new ArrayList<>();

        // Individual paid transactions
        String paidSql = """
            SELECT
                ra.bill_month,
                ra.payment_date AS report_date,
                ra.flat_no,
                t.name AS tenant_name,
                MAX(ra.house_rent - ra.discount, 0) AS total,
                MAX(ra.house_rent - ra.discount, 0) AS paid_amount,
                0 AS due,
                ra.status
            FROM rent_archive ra
            JOIN tenants t ON ra.tenant_id = t.id
            """;

        // Individual outstanding/current transactions
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
            """;

        List<ReportRow> rawRows = new ArrayList<>();

        try (Connection conn = DBUtil.connect()) {

            try (PreparedStatement ps = conn.prepareStatement(paidSql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    rawRows.add(new ReportRow(
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
                    rawRows.add(new ReportRow(
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
            return list;
        }

        // Group by month (most recent first); each group lists every flat
        // individually, followed by one Total row for that month.
        Map<String, List<ReportRow>> byMonth = new TreeMap<>(Comparator.reverseOrder());
        for (ReportRow r : rawRows) {
            byMonth.computeIfAbsent(r.getMonth(), k -> new ArrayList<>()).add(r);
        }

        for (Map.Entry<String, List<ReportRow>> entry : byMonth.entrySet()) {
            List<ReportRow> monthRows = entry.getValue();
            monthRows.sort(Comparator.comparing(
                    r -> r.getFlatNo() == null ? "" : r.getFlatNo()));

            double totalSum = 0, paidSum = 0, dueSum = 0;

            for (ReportRow r : monthRows) {
                list.add(r);
                totalSum += r.getTotal();
                paidSum += r.getPaid();
                dueSum += r.getDue();
            }

            list.add(new ReportRow(
                    "Total",
                    entry.getKey(),
                    "",
                    "",
                    "",
                    totalSum,
                    paidSum,
                    dueSum,
                    dueSum > 0 ? "DUE" : "PAID"
            ));
        }

        return list;
    }

    public static List<ReportRow> getYearlyIncomeRows() {
        List<ReportRow> list = new ArrayList<>();

        // Individual paid transactions
        String paidSql = """
            SELECT
                ra.bill_month,
                ra.payment_date AS report_date,
                ra.flat_no,
                t.name AS tenant_name,
                MAX(ra.house_rent - ra.discount, 0) AS total,
                MAX(ra.house_rent - ra.discount, 0) AS paid_amount,
                0 AS due,
                ra.status
            FROM rent_archive ra
            JOIN tenants t ON ra.tenant_id = t.id
            """;

        // Individual outstanding/current transactions
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
            """;

        List<ReportRow> rawRows = new ArrayList<>();

        try (Connection conn = DBUtil.connect()) {

            try (PreparedStatement ps = conn.prepareStatement(paidSql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    rawRows.add(new ReportRow(
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
                    rawRows.add(new ReportRow(
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
            return list;
        }

        // Group by year (most recent first); each group lists every flat's
        // monthly transaction individually, followed by one Total row.
        Map<String, List<ReportRow>> byYear = new TreeMap<>(Comparator.reverseOrder());
        for (ReportRow r : rawRows) {
            String month = r.getMonth();
            String year = (month != null && month.length() >= 4) ? month.substring(0, 4) : "";
            byYear.computeIfAbsent(year, k -> new ArrayList<>()).add(r);
        }

        for (Map.Entry<String, List<ReportRow>> entry : byYear.entrySet()) {
            List<ReportRow> yearRows = entry.getValue();
            yearRows.sort(
                    Comparator.comparing((ReportRow r) -> r.getMonth() == null ? "" : r.getMonth())
                            .thenComparing(r -> r.getFlatNo() == null ? "" : r.getFlatNo())
            );

            double totalSum = 0, paidSum = 0, dueSum = 0;

            for (ReportRow r : yearRows) {
                list.add(r);
                totalSum += r.getTotal();
                paidSum += r.getPaid();
                dueSum += r.getDue();
            }

            list.add(new ReportRow(
                    "Total",
                    entry.getKey(),
                    "",
                    "",
                    "",
                    totalSum,
                    paidSum,
                    dueSum,
                    dueSum > 0 ? "DUE" : "PAID"
            ));
        }

        return list;
    }

    public static List<ReportRow> getTenantWiseIncomeRows() {
        List<ReportRow> list = new ArrayList<>();

        // Paid history, grouped by tenant + flat
        String paidSql = """
            SELECT
                t.name AS tenant_name,
                ra.flat_no,
                SUM(MAX(ra.house_rent - ra.discount, 0)) AS total_amount,
                SUM(MAX(ra.house_rent - ra.discount, 0)) AS paid_amount
            FROM rent_archive ra
            JOIN tenants t ON ra.tenant_id = t.id
            GROUP BY ra.tenant_id, t.name, ra.flat_no
            """;

        // Outstanding due rent, grouped by tenant + flat. Merged below into
        // the same row as the paid total, so a tenant who paid one month
        // but owes another shows both figures together in one row.
        String dueSql = """
            SELECT
                t.name AS tenant_name,
                rc.flat_no,
                SUM(rc.total) AS total_amount,
                SUM(rc.paid_amount) AS paid_amount,
                SUM(rc.total - rc.paid_amount) AS due_amount
            FROM rent_current rc
            JOIN tenants t ON rc.tenant_id = t.id
            WHERE (rc.total - rc.paid_amount) > 0
            GROUP BY rc.tenant_id, t.name, rc.flat_no
            """;

        // key = tenant_name + "|" + flat_no -> {total, paid, due}
        Map<String, double[]> merged = new LinkedHashMap<>();
        Map<String, String[]> labels = new LinkedHashMap<>(); // same key -> {tenant_name, flat_no}

        try (Connection conn = DBUtil.connect()) {

            try (PreparedStatement ps = conn.prepareStatement(paidSql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String tenant = rs.getString("tenant_name");
                    String flat = rs.getString("flat_no");
                    String key = tenant + "|" + flat;

                    double[] agg = merged.computeIfAbsent(key, k -> new double[3]);
                    agg[0] += rs.getDouble("total_amount");
                    agg[1] += rs.getDouble("paid_amount");
                    labels.putIfAbsent(key, new String[]{tenant, flat});
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(dueSql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String tenant = rs.getString("tenant_name");
                    String flat = rs.getString("flat_no");
                    String key = tenant + "|" + flat;

                    double[] agg = merged.computeIfAbsent(key, k -> new double[3]);
                    agg[0] += rs.getDouble("total_amount");
                    agg[1] += rs.getDouble("paid_amount");
                    agg[2] += rs.getDouble("due_amount");
                    labels.putIfAbsent(key, new String[]{tenant, flat});
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> keys = new ArrayList<>(merged.keySet());
        keys.sort((a, b) -> labels.get(a)[0].compareToIgnoreCase(labels.get(b)[0]));

        for (String key : keys) {
            double[] agg = merged.get(key);
            String[] label = labels.get(key);

            list.add(new ReportRow(
                    "Tenant-wise Income",
                    "",
                    "",
                    label[1],
                    label[0],
                    agg[0],
                    agg[1],
                    agg[2],
                    agg[2] > 0 ? "DUE" : "PAID"
            ));
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
    public static List<ReportRow> getRepairReportRows() {
        List<ReportRow> list = new ArrayList<>();

        String sql = """
            SELECT
                repair_date,
                flat_no,
                category,
                description,
                cost,
                paid_by,
                status
            FROM repairs
            ORDER BY repair_date DESC, id DESC
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String repairDate = rs.getString("repair_date");
                String month = "";

                if (repairDate != null && repairDate.length() >= 7) {
                    month = repairDate.substring(0, 7);
                }

                list.add(new ReportRow(
                        "Repair - " + rs.getString("category"),
                        month,
                        repairDate,
                        rs.getString("flat_no"),
                        rs.getString("description"),
                        rs.getDouble("cost"),
                        0,
                        0,
                        rs.getString("status"),
                        rs.getString("paid_by")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    private static String moneyText(double value) {
        return "৳ " + String.format("%,.2f", value);
    }
}