package com.rent.dao;

import com.rent.model.RentRow;
import com.rent.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RentDAO {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TS  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // -------------- 1) Load unpaid/current rent rows --------------
    public static List<RentRow> getCurrentRentRows() {
        List<RentRow> list = new ArrayList<>();

        String sql = """
            SELECT
                rc.id,
                rc.tenant_id,
                rc.flat_no,
                f.meter_no,
                t.name AS tenant_name,
                t.phone,
                rc.bill_month,
                rc.house_rent,
                rc.electricity,
                rc.water,
                rc.gas,
                rc.other_bills,
                rc.fine,
                rc.discount,
                rc.total,
                rc.status,
                rc.due_date,
                rc.payment_date,
                rc.paid_amount,
                rc.notes
            FROM rent_current rc
            JOIN tenants t ON rc.tenant_id = t.id
            JOIN flats f ON rc.flat_no = f.flat_no
            WHERE rc.status IN ('DUE', 'PARTIAL', 'LATE')
            ORDER BY rc.bill_month DESC, rc.flat_no
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RentRow r = new RentRow();
                r.setId(rs.getInt("id"));
                r.setTenantId(rs.getInt("tenant_id"));
                r.setFlatNo(rs.getString("flat_no"));
                r.setMeterNo(rs.getString("meter_no"));
                r.setTenantName(rs.getString("tenant_name"));
                r.setPhone(rs.getString("phone"));
                r.setBillMonth(rs.getString("bill_month"));
                r.setHouseRent(rs.getDouble("house_rent"));
                r.setElectricity(rs.getDouble("electricity"));
                r.setWater(rs.getDouble("water"));
                r.setGas(rs.getDouble("gas"));
                r.setOtherBills(rs.getDouble("other_bills"));
                r.setFine(rs.getDouble("fine"));
                r.setDiscount(rs.getDouble("discount"));
                r.setTotal(rs.getDouble("total"));
                r.setStatus(rs.getString("status"));
                r.setDueDate(rs.getString("due_date"));
                r.setPaymentDate(rs.getString("payment_date"));
                r.setPaidAmount(rs.getDouble("paid_amount"));
                r.setNotes(rs.getString("notes"));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // -------------- 2) Generate billing month (USES GLOBAL BILL DEFAULTS) --------------
    public static void ensureMonthGenerated(String billMonth, int dueDay) {

        LocalDate due = LocalDate.parse(billMonth + "-01", YMD).withDayOfMonth(dueDay);
        String dueDate = due.format(YMD);

        String selectOccupiedTenants = """
        SELECT t.id AS tenant_id, f.flat_no, f.rent
        FROM tenants t
        JOIN flats f ON t.flat_no = f.flat_no
        WHERE f.status='Occupied'
          AND (t.status IS NULL OR t.status = '' OR t.status = 'Active')
        AND NOT EXISTS (
            SELECT 1
            FROM rent_archive ra
            WHERE ra.tenant_id = t.id
            AND ra.bill_month = ?
        )
        AND NOT EXISTS (
            SELECT 1
            FROM rent_current rc
            WHERE rc.tenant_id = t.id
            AND rc.bill_month = ?
        )
        """;

        String selectGlobalDefaults = """
            SELECT electricity, gas, water, due_day
            FROM bill_defaults
            WHERE id = 1
            """;

        String insertRent = """
            INSERT OR IGNORE INTO rent_current
            (tenant_id, flat_no, bill_month, house_rent,
             electricity, water, gas,
             other_bills, fine, discount, total,
             paid_amount, payment_date, due_date, status, notes)
            VALUES
            (?, ?, ?, ?, ?, ?, ?, 0, 0, 0, ?, 0, NULL, ?, 'DUE', NULL)
            """;

        try (Connection conn = DBUtil.connect()) {
            conn.setAutoCommit(false);

            double elec = 0, gas = 0, water = 0;
            try (PreparedStatement psDef = conn.prepareStatement(selectGlobalDefaults);
                 ResultSet drs = psDef.executeQuery()) {
                if (drs.next()) {
                    elec = drs.getDouble("electricity");
                    gas = drs.getDouble("gas");
                    water = drs.getDouble("water");
                }
            }

            try (PreparedStatement psTenants = conn.prepareStatement(selectOccupiedTenants)) {

                psTenants.setString(1, billMonth);  // for NOT EXISTS rent_archive
                psTenants.setString(2, billMonth);  // for NOT EXISTS rent_current

                try (ResultSet rs = psTenants.executeQuery()) {
                    while (rs.next()) {
                        int tenantId = rs.getInt("tenant_id");
                        String flatNo = rs.getString("flat_no");
                        double houseRent = rs.getDouble("rent");

                        double total = calcTotal(houseRent, elec, water, gas, 0, 0, 0);

                        try (PreparedStatement psIns = conn.prepareStatement(insertRent)) {
                            psIns.setInt(1, tenantId);
                            psIns.setString(2, flatNo);
                            psIns.setString(3, billMonth);
                            psIns.setDouble(4, houseRent);
                            psIns.setDouble(5, elec);
                            psIns.setDouble(6, water);
                            psIns.setDouble(7, gas);
                            psIns.setDouble(8, total);
                            psIns.setString(9, dueDate);
                            psIns.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Convenience overload that reads the saved Rent Due Day from
     * bill_defaults instead of requiring the caller to hardcode it.
     * Falls back to day 5 if no value has been configured yet, which
     * matches the previous hardcoded behavior exactly.
     */
    public static void ensureMonthGenerated(String billMonth) {
        int dueDay = 5;

        String sql = "SELECT due_day FROM bill_defaults WHERE id = 1";

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int stored = rs.getInt("due_day");
                if (stored >= 1 && stored <= 28) {
                    dueDay = stored;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Falls back to day 5 on any read failure.
        }

        ensureMonthGenerated(billMonth, dueDay);
    }

    // -------------- 3) Payment update + archive if cleared --------------
    public static void applyPayment(
            int rentId,
            double elec, double water, double gas,
            double other, double fine, double discount,
            double paid,
            LocalDate payDate
    ) {
        String select = "SELECT * FROM rent_current WHERE id=?";
        String update = """
            UPDATE rent_current SET
              electricity=?, water=?, gas=?,
              other_bills=?, fine=?, discount=?,
              total=?, paid_amount=?, payment_date=?, status=?
            WHERE id=?
            """;

        try (Connection conn = DBUtil.connect()) {
            conn.setAutoCommit(false);

            RentSnapshot snap = null;
            try (PreparedStatement ps = conn.prepareStatement(select)) {
                ps.setInt(1, rentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        snap = new RentSnapshot();
                        snap.houseRent = rs.getDouble("house_rent");
                        snap.dueDate = rs.getString("due_date");
                    }
                }
            }

            if (snap == null) {
                conn.rollback();
                return;
            }

            double total = calcTotal(snap.houseRent, elec, water, gas, other, fine, discount);

            boolean late = snap.dueDate != null
                    && !snap.dueDate.isBlank()
                    && payDate.isAfter(LocalDate.parse(snap.dueDate, YMD));

            String status;
            if (paid >= total) status = "PAID";
            else if (paid > 0) status = late ? "LATE" : "PARTIAL";
            else status = late ? "LATE" : "DUE";

            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setDouble(1, elec);
                ps.setDouble(2, water);
                ps.setDouble(3, gas);
                ps.setDouble(4, other);
                ps.setDouble(5, fine);
                ps.setDouble(6, discount);
                ps.setDouble(7, total);
                ps.setDouble(8, paid);
                ps.setString(9, payDate.format(YMD));
                ps.setString(10, status);
                ps.setInt(11, rentId);
                ps.executeUpdate();
            }

            if ("PAID".equals(status)) {
                moveToArchive(conn, rentId);
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------- 4) Archive list --------------
    public static List<RentRow> getArchiveRows() {
        List<RentRow> list = new ArrayList<>();

        String sql = """
            SELECT
                ra.id,
                ra.tenant_id,
                ra.flat_no,
                f.meter_no,
                t.name AS tenant_name,
                t.phone,
                ra.bill_month,
                ra.house_rent,
                ra.electricity,
                ra.water,
                ra.gas,
                ra.other_bills,
                ra.fine,
                ra.discount,
                ra.total,
                ra.status,
                ra.due_date,
                ra.payment_date,
                ra.paid_amount,
                ra.notes,
                ra.receipt_no
            FROM rent_archive ra
            JOIN tenants t ON ra.tenant_id = t.id
            JOIN flats f ON ra.flat_no = f.flat_no
            ORDER BY ra.bill_month DESC, ra.flat_no
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RentRow r = new RentRow();
                r.setId(rs.getInt("id"));
                r.setTenantId(rs.getInt("tenant_id"));
                r.setFlatNo(rs.getString("flat_no"));
                r.setMeterNo(rs.getString("meter_no"));
                r.setTenantName(rs.getString("tenant_name"));
                r.setPhone(rs.getString("phone"));
                r.setBillMonth(rs.getString("bill_month"));
                r.setHouseRent(rs.getDouble("house_rent"));
                r.setElectricity(rs.getDouble("electricity"));
                r.setWater(rs.getDouble("water"));
                r.setGas(rs.getDouble("gas"));
                r.setOtherBills(rs.getDouble("other_bills"));
                r.setFine(rs.getDouble("fine"));
                r.setDiscount(rs.getDouble("discount"));
                r.setTotal(rs.getDouble("total"));
                r.setStatus(rs.getString("status"));
                r.setDueDate(rs.getString("due_date"));
                r.setPaymentDate(rs.getString("payment_date"));
                r.setPaidAmount(rs.getDouble("paid_amount"));
                r.setNotes(rs.getString("notes"));
                r.setReceiptNo(rs.getString("receipt_no"));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // -------------- 5) Restore from archive (Restore button) --------------
    public static void restoreFromArchive(int archiveId) {

        String insertBack = """
            INSERT OR IGNORE INTO rent_current
            (tenant_id, flat_no, bill_month, house_rent,
             electricity, water, gas,
             other_bills, fine, discount, total,
             paid_amount, payment_date, due_date, status, notes)
            SELECT
             tenant_id, flat_no, bill_month, house_rent,
             electricity, water, gas,
             other_bills, fine, discount, total,
             0, NULL, due_date, 'DUE', notes
            FROM rent_archive
            WHERE id=?
            """;

        String delete = "DELETE FROM rent_archive WHERE id=?";

        try (Connection conn = DBUtil.connect()) {
            conn.setAutoCommit(false);

            int insertedRows;

            try (PreparedStatement ps = conn.prepareStatement(insertBack)) {
                ps.setInt(1, archiveId);
                insertedRows = ps.executeUpdate();
            }

            if (insertedRows > 0) {
                try (PreparedStatement ps = conn.prepareStatement(delete)) {
                    ps.setInt(1, archiveId);
                    ps.executeUpdate();
                }
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Update existing DUE/PARTIAL/LATE rows to latest global defaults (does NOT touch archive)
    public static void applyGlobalDefaultsToUnpaidRows() {
        String select = "SELECT electricity, gas, water FROM bill_defaults WHERE id=1";

        String update = """
        UPDATE rent_current
        SET electricity=?,
            gas=?,
            water=?,
            total=(house_rent + ? + ? + ? + other_bills + fine) - discount
        WHERE status IN ('DUE','PARTIAL','LATE')
        """;

        try (Connection conn = DBUtil.connect()) {

            double elec = 0, gas = 0, water = 0;

            try (PreparedStatement ps = conn.prepareStatement(select);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    elec = rs.getDouble("electricity");
                    gas = rs.getDouble("gas");
                    water = rs.getDouble("water");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setDouble(1, elec);
                ps.setDouble(2, gas);
                ps.setDouble(3, water);

                // same values again for total formula
                ps.setDouble(4, elec);
                ps.setDouble(5, gas);
                ps.setDouble(6, water);

                ps.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- ARCHIVE MOVE ----------------
    private static void moveToArchive(Connection conn, int id) throws SQLException {
        String receiptNo = generateReceiptNo(conn);
        String insert = """
        INSERT INTO rent_archive
        (original_id, tenant_id, flat_no, bill_month,
         house_rent, electricity, water, gas,
         other_bills, fine, discount,
         total, paid_amount, payment_date, due_date,
         status, notes, archived_at, receipt_no)
        SELECT
         id, tenant_id, flat_no, bill_month,
         house_rent, electricity, water, gas,
         other_bills, fine, discount,
         total, paid_amount, payment_date, due_date,
         'PAID', notes, ?, ?
        FROM rent_current
        WHERE id=?
        """;

        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, LocalDateTime.now().format(TS));
            ps.setString(2, receiptNo);
            ps.setInt(3, id);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM rent_current WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ---------------- HELPERS ----------------
    private static double calcTotal(double rent, double e, double w, double g,
                                    double other, double fine, double discount) {
        return (rent + e + w + g + other + fine) - discount;
    }

    private static class RentSnapshot {
        double houseRent;
        String dueDate;
    }

    private static String generateReceiptNo(Connection conn) throws SQLException {
        String year = String.valueOf(LocalDate.now().getYear());

        String sql = """
            SELECT COUNT(*)
            FROM rent_archive
            WHERE receipt_no LIKE ?
            """;

        int nextNumber = 1;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "RCP-" + year + "-%");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nextNumber = rs.getInt(1) + 1;
                }
            }
        }

        return "RCP-" + year + "-" + String.format("%06d", nextNumber);
    }

    public static RentRow getArchivedRowByOriginalId(int originalId) {
        String sql = """
            SELECT
                ra.id,
                ra.original_id,
                ra.tenant_id,
                ra.flat_no,
                f.meter_no,
                t.name AS tenant_name,
                t.phone,
                ra.bill_month,
                ra.house_rent,
                ra.electricity,
                ra.water,
                ra.gas,
                ra.other_bills,
                ra.fine,
                ra.discount,
                ra.total,
                ra.status,
                ra.due_date,
                ra.payment_date,
                ra.paid_amount,
                ra.notes,
                ra.receipt_no
            FROM rent_archive ra
            JOIN tenants t ON ra.tenant_id = t.id
            JOIN flats f ON ra.flat_no = f.flat_no
            WHERE ra.original_id = ?
            ORDER BY ra.id DESC
            LIMIT 1
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, originalId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RentRow r = new RentRow();

                    r.setId(rs.getInt("id"));
                    r.setTenantId(rs.getInt("tenant_id"));
                    r.setFlatNo(rs.getString("flat_no"));
                    r.setMeterNo(rs.getString("meter_no"));
                    r.setTenantName(rs.getString("tenant_name"));
                    r.setPhone(rs.getString("phone"));
                    r.setBillMonth(rs.getString("bill_month"));
                    r.setHouseRent(rs.getDouble("house_rent"));
                    r.setElectricity(rs.getDouble("electricity"));
                    r.setWater(rs.getDouble("water"));
                    r.setGas(rs.getDouble("gas"));
                    r.setOtherBills(rs.getDouble("other_bills"));
                    r.setFine(rs.getDouble("fine"));
                    r.setDiscount(rs.getDouble("discount"));
                    r.setTotal(rs.getDouble("total"));
                    r.setStatus(rs.getString("status"));
                    r.setDueDate(rs.getString("due_date"));
                    r.setPaymentDate(rs.getString("payment_date"));
                    r.setPaidAmount(rs.getDouble("paid_amount"));
                    r.setNotes(rs.getString("notes"));
                    r.setReceiptNo(rs.getString("receipt_no"));

                    return r;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean deleteArchivePayment(int archiveId) {
        String sql = """
            DELETE FROM rent_archive
            WHERE id = ?
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, archiveId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<RentRow> getUnpaidRentRowsByTenant(int tenantId) {
        List<RentRow> list = new ArrayList<>();

        String sql = """
            SELECT
                rc.id,
                rc.tenant_id,
                rc.flat_no,
                f.meter_no,
                t.name AS tenant_name,
                t.phone,
                rc.bill_month,
                rc.house_rent,
                rc.electricity,
                rc.water,
                rc.gas,
                rc.other_bills,
                rc.fine,
                rc.discount,
                rc.total,
                rc.status,
                rc.due_date,
                rc.payment_date,
                rc.paid_amount,
                rc.notes
            FROM rent_current rc
            JOIN tenants t ON rc.tenant_id = t.id
            JOIN flats f ON rc.flat_no = f.flat_no
            WHERE rc.tenant_id = ?
              AND rc.status IN ('DUE', 'PARTIAL', 'LATE')
            ORDER BY rc.bill_month ASC, rc.id ASC
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenantId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RentRow r = new RentRow();

                    r.setId(rs.getInt("id"));
                    r.setTenantId(rs.getInt("tenant_id"));
                    r.setFlatNo(rs.getString("flat_no"));
                    r.setMeterNo(rs.getString("meter_no"));
                    r.setTenantName(rs.getString("tenant_name"));
                    r.setPhone(rs.getString("phone"));
                    r.setBillMonth(rs.getString("bill_month"));
                    r.setHouseRent(rs.getDouble("house_rent"));
                    r.setElectricity(rs.getDouble("electricity"));
                    r.setWater(rs.getDouble("water"));
                    r.setGas(rs.getDouble("gas"));
                    r.setOtherBills(rs.getDouble("other_bills"));
                    r.setFine(rs.getDouble("fine"));
                    r.setDiscount(rs.getDouble("discount"));
                    r.setTotal(rs.getDouble("total"));
                    r.setStatus(rs.getString("status"));
                    r.setDueDate(rs.getString("due_date"));
                    r.setPaymentDate(rs.getString("payment_date"));
                    r.setPaidAmount(rs.getDouble("paid_amount"));
                    r.setNotes(rs.getString("notes"));

                    list.add(r);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}