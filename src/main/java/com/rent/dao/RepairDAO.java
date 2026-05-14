package com.rent.dao;

import com.rent.model.Repair;
import com.rent.util.DBUtil;
import com.rent.util.AuditActions;
import com.rent.dao.AuditLogDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RepairDAO {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static List<Repair> getAllRepairs() {
        List<Repair> list = new ArrayList<>();

        String sql = """
                SELECT *
                FROM repairs
                ORDER BY repair_date DESC, id DESC
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRepair(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean addRepair(Repair repair) {
        String sql = """
                INSERT INTO repairs
                (flat_no, repair_date, category, description, cost,
                 paid_by, status, notes, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, repair.getFlatNo());
            ps.setString(2, repair.getRepairDate());
            ps.setString(3, repair.getCategory());
            ps.setString(4, repair.getDescription());
            ps.setDouble(5, repair.getCost());
            ps.setString(6, repair.getPaidBy());
            ps.setString(7, repair.getStatus());
            ps.setString(8, repair.getNotes());
            ps.setString(9, LocalDateTime.now().format(TS));

            boolean success = ps.executeUpdate() > 0;

            if (success) {
                AuditLogDAO.log(
                        AuditActions.REPAIR_ADDED,
                        "Repair added. Flat: "
                                + repair.getFlatNo()
                                + ", Date: "
                                + repair.getRepairDate()
                                + ", Category: "
                                + repair.getCategory()
                                + ", Cost: "
                                + repair.getCost()
                                + ", Paid By: "
                                + repair.getPaidBy()
                                + ", Status: "
                                + repair.getStatus()
                );
            }

            return success;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateRepair(Repair repair) {
        String sql = """
                UPDATE repairs SET
                    flat_no=?,
                    repair_date=?,
                    category=?,
                    description=?,
                    cost=?,
                    paid_by=?,
                    status=?,
                    notes=?
                WHERE id=?
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, repair.getFlatNo());
            ps.setString(2, repair.getRepairDate());
            ps.setString(3, repair.getCategory());
            ps.setString(4, repair.getDescription());
            ps.setDouble(5, repair.getCost());
            ps.setString(6, repair.getPaidBy());
            ps.setString(7, repair.getStatus());
            ps.setString(8, repair.getNotes());
            ps.setInt(9, repair.getId());

            boolean success = ps.executeUpdate() > 0;

            if (success) {
                AuditLogDAO.log(
                        AuditActions.REPAIR_UPDATED,
                        "Repair updated. ID: "
                                + repair.getId()
                                + ", Flat: "
                                + repair.getFlatNo()
                                + ", Date: "
                                + repair.getRepairDate()
                                + ", Category: "
                                + repair.getCategory()
                                + ", Cost: "
                                + repair.getCost()
                                + ", Paid By: "
                                + repair.getPaidBy()
                                + ", Status: "
                                + repair.getStatus()
                );
            }

            return success;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteRepair(int id) {
        String sql = "DELETE FROM repairs WHERE id=?";

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static double getTotalRepairCost() {
        String sql = "SELECT COALESCE(SUM(cost), 0) FROM repairs";

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getDouble(1) : 0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static double getMonthRepairCost(String month) {
        String sql = """
                SELECT COALESCE(SUM(cost), 0)
                FROM repairs
                WHERE repair_date LIKE ?
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, month + "-%");

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static double getYearRepairCost(String year) {
        String sql = """
                SELECT COALESCE(SUM(cost), 0)
                FROM repairs
                WHERE repair_date LIKE ?
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, year + "-%");

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static Repair mapRepair(ResultSet rs) throws Exception {
        Repair repair = new Repair();

        repair.setId(rs.getInt("id"));
        repair.setFlatNo(rs.getString("flat_no"));
        repair.setRepairDate(rs.getString("repair_date"));
        repair.setCategory(rs.getString("category"));
        repair.setDescription(rs.getString("description"));
        repair.setCost(rs.getDouble("cost"));
        repair.setPaidBy(rs.getString("paid_by"));
        repair.setStatus(rs.getString("status"));
        repair.setNotes(rs.getString("notes"));
        repair.setCreatedAt(rs.getString("created_at"));

        return repair;
    }

    public static double getOwnerPaidTotalRepairCost() {
        String sql = """
            SELECT COALESCE(SUM(cost), 0)
            FROM repairs
            WHERE paid_by = 'Owner'
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getDouble(1) : 0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static double getOwnerPaidMonthRepairCost(String month) {
        String sql = """
            SELECT COALESCE(SUM(cost), 0)
            FROM repairs
            WHERE paid_by = 'Owner'
            AND repair_date LIKE ?
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, month + "-%");

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static double getOwnerPaidYearRepairCost(String year) {
        String sql = """
            SELECT COALESCE(SUM(cost), 0)
            FROM repairs
            WHERE paid_by = 'Owner'
            AND repair_date LIKE ?
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, year + "-%");

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static double getTenantPaidTotalRepairCost() {
        String sql = """
            SELECT COALESCE(SUM(cost), 0)
            FROM repairs
            WHERE paid_by = 'Tenant'
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getDouble(1) : 0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static double getTenantPaidMonthRepairCost(String month) {
        String sql = """
            SELECT COALESCE(SUM(cost), 0)
            FROM repairs
            WHERE paid_by = 'Tenant'
            AND repair_date LIKE ?
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, month + "-%");

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static double getTenantPaidYearRepairCost(String year) {
        String sql = """
            SELECT COALESCE(SUM(cost), 0)
            FROM repairs
            WHERE paid_by = 'Tenant'
            AND repair_date LIKE ?
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, year + "-%");

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}