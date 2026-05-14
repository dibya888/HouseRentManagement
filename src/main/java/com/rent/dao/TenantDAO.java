package com.rent.dao;

import com.rent.model.Tenant;
import com.rent.util.AuditActions;
import com.rent.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TenantDAO {

    // CREATE
    public static void addTenant(Tenant t) {
        String sql = """
                INSERT INTO tenants
                (name, phone, email, nid, address, flat_no, rent,
                 nid_path, doc_path, status, move_in_date,
                 move_out_date, move_out_reason,
                 security_deposit, security_deposit_date, security_deposit_note)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Active', ?, NULL, NULL, ?, ?, ?)
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            double deposit = Math.max(0, t.getSecurityDeposit());

            ps.setString(1, t.getName());
            ps.setString(2, t.getPhone());
            ps.setString(3, t.getEmail());
            ps.setString(4, t.getNid());
            ps.setString(5, t.getAddress());
            ps.setString(6, t.getFlatNo());
            ps.setDouble(7, t.getRent());
            ps.setString(8, t.getNidPath());
            ps.setString(9, t.getDocPath());

            ps.setString(10, t.getMoveInDate());

            ps.setDouble(11, deposit);

            if (deposit > 0) {
                ps.setString(12, t.getSecurityDepositDate());
                ps.setString(13, t.getSecurityDepositNote());
            } else {
                ps.setString(12, null);
                ps.setString(13, null);
            }

            int rows = ps.executeUpdate();

            if (rows > 0) {
                AuditLogDAO.log(
                        AuditActions.TENANT_ADDED,
                        "Tenant added. Name: "
                                + t.getName()
                                + ", Phone: "
                                + t.getPhone()
                                + ", Flat: "
                                + t.getFlatNo()
                                + ", Rent: "
                                + t.getRent()
                                + ", Security Deposit: "
                                + deposit
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // READ ALL
    public static List<Tenant> getAllTenants() {
        List<Tenant> list = new ArrayList<>();

        String sql = """
                SELECT *
                FROM tenants
                ORDER BY id DESC
                """;

        try (Connection conn = DBUtil.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapTenant(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // READ ACTIVE ONLY
    public static List<Tenant> getActiveTenants() {
        List<Tenant> list = new ArrayList<>();

        String sql = """
                SELECT *
                FROM tenants
                WHERE status IS NULL
                   OR status = ''
                   OR status = 'Active'
                ORDER BY id DESC
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapTenant(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // READ MOVED OUT ONLY
    public static List<Tenant> getMovedOutTenants() {
        List<Tenant> list = new ArrayList<>();

        String sql = """
                SELECT *
                FROM tenants
                WHERE status = 'Moved Out'
                ORDER BY move_out_date DESC, id DESC
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapTenant(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // UPDATE
    public static void updateTenant(Tenant t) {
        String sql = """
                UPDATE tenants
                SET name=?,
                    phone=?,
                    email=?,
                    nid=?,
                    flat_no=?,
                    rent=?,
                    address=?,
                    nid_path=?,
                    doc_path=?,
                    status=?,
                    move_in_date=?,
                    move_out_date=?,
                    move_out_reason=?,
                    security_deposit=?,
                    security_deposit_date=?,
                    security_deposit_note=?
                WHERE id=?
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            double deposit = Math.max(0, t.getSecurityDeposit());

            ps.setString(1, t.getName());
            ps.setString(2, t.getPhone());
            ps.setString(3, t.getEmail());
            ps.setString(4, t.getNid());
            ps.setString(5, t.getFlatNo());
            ps.setDouble(6, t.getRent());
            ps.setString(7, t.getAddress());
            ps.setString(8, t.getNidPath());
            ps.setString(9, t.getDocPath());
            ps.setString(10, blankToDefault(t.getStatus(), "Active"));
            ps.setString(11, t.getMoveInDate());
            ps.setString(12, t.getMoveOutDate());
            ps.setString(13, t.getMoveOutReason());

            ps.setDouble(14, deposit);

            if (deposit > 0) {
                ps.setString(15, t.getSecurityDepositDate());
                ps.setString(16, t.getSecurityDepositNote());
            } else {
                ps.setString(15, null);
                ps.setString(16, null);
            }

            ps.setInt(17, t.getId());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                AuditLogDAO.log(
                        AuditActions.TENANT_UPDATED,
                        "Tenant updated. ID: "
                                + t.getId()
                                + ", Name: "
                                + t.getName()
                                + ", Phone: "
                                + t.getPhone()
                                + ", Flat: "
                                + t.getFlatNo()
                                + ", Rent: "
                                + t.getRent()
                                + ", Security Deposit: "
                                + deposit
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE ONLY TENANT
    public static void deleteTenant(int id) {
        Tenant tenant = getTenantById(id);

        String sql = "DELETE FROM tenants WHERE id=?";

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                AuditLogDAO.log(
                        AuditActions.TENANT_DELETED,
                        "Tenant deleted. ID: "
                                + id
                                + ", Name: "
                                + (tenant == null ? "" : tenant.getName())
                                + ", Phone: "
                                + (tenant == null ? "" : tenant.getPhone())
                                + ", Flat: "
                                + (tenant == null ? "" : tenant.getFlatNo())
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE TENANT AND FREE FLAT
    public static void deleteTenantAndFreeFlat(int tenantId) {
        String tenantName = null;
        String tenantPhone = null;
        String flatNo = null;

        String selectFlatSql = "SELECT name, phone, flat_no FROM tenants WHERE id=?";
        String deleteTenantSql = "DELETE FROM tenants WHERE id=?";
        String updateFlatSql = "UPDATE flats SET status='Available' WHERE flat_no=?";

        try (Connection conn = DBUtil.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(selectFlatSql)) {
                ps.setInt(1, tenantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tenantName = rs.getString("name");
                        tenantPhone = rs.getString("phone");
                        flatNo = rs.getString("flat_no");
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteTenantSql)) {
                ps.setInt(1, tenantId);
                ps.executeUpdate();
            }

            if (flatNo != null && !flatNo.isBlank()) {
                try (PreparedStatement ps = conn.prepareStatement(updateFlatSql)) {
                    ps.setString(1, flatNo);
                    ps.executeUpdate();
                }
            }

            conn.commit();

            AuditLogDAO.log(
                    AuditActions.TENANT_DELETED,
                    "Tenant deleted. ID: "
                            + tenantId
                            + ", Name: "
                            + tenantName
                            + ", Phone: "
                            + tenantPhone
                            + ", Flat freed: "
                            + flatNo
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE MISTAKEN TENANT + CURRENT DUE ROWS ONLY
    public static boolean deleteTenantAddedByMistake(int tenantId) {
        String selectTenantSql = """
                SELECT name, phone, flat_no
                FROM tenants
                WHERE id = ?
                """;

        String checkArchiveSql = """
                SELECT COUNT(*)
                FROM rent_archive
                WHERE tenant_id = ?
                """;

        String deleteCurrentRentSql = """
                DELETE FROM rent_current
                WHERE tenant_id = ?
                """;

        String deleteTenantSql = """
                DELETE FROM tenants
                WHERE id = ?
                """;

        String freeFlatSql = """
                UPDATE flats
                SET status = 'Available'
                WHERE flat_no = ?
                """;

        String tenantName = null;
        String tenantPhone = null;
        String flatNo = null;

        try (Connection conn = DBUtil.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(selectTenantSql)) {
                ps.setInt(1, tenantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tenantName = rs.getString("name");
                        tenantPhone = rs.getString("phone");
                        flatNo = rs.getString("flat_no");
                    }
                }
            }

            int archiveCount = 0;

            try (PreparedStatement ps = conn.prepareStatement(checkArchiveSql)) {
                ps.setInt(1, tenantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        archiveCount = rs.getInt(1);
                    }
                }
            }

            if (archiveCount > 0) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteCurrentRentSql)) {
                ps.setInt(1, tenantId);
                ps.executeUpdate();
            }

            int deletedRows;

            try (PreparedStatement ps = conn.prepareStatement(deleteTenantSql)) {
                ps.setInt(1, tenantId);
                deletedRows = ps.executeUpdate();
            }

            if (flatNo != null && !flatNo.isBlank()) {
                try (PreparedStatement ps = conn.prepareStatement(freeFlatSql)) {
                    ps.setString(1, flatNo);
                    ps.executeUpdate();
                }
            }

            conn.commit();

            if (deletedRows > 0) {
                AuditLogDAO.log(
                        AuditActions.TENANT_DELETED,
                        "Mistaken tenant deleted with current due rows. ID: "
                                + tenantId
                                + ", Name: "
                                + tenantName
                                + ", Phone: "
                                + tenantPhone
                                + ", Flat freed: "
                                + flatNo
                );
            }

            return deletedRows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // MOVE OUT TENANT AND FREE FLAT
    public static boolean moveOutTenant(int tenantId, String moveOutDate, String reason) {
        Tenant tenant = getTenantById(tenantId);

        if (tenant == null) {
            return false;
        }

        String flatNo = tenant.getFlatNo();

        String updateTenantSql = """
                UPDATE tenants
                SET status = 'Moved Out',
                    move_out_date = ?,
                    move_out_reason = ?
                WHERE id = ?
                """;

        String updateFlatSql = """
                UPDATE flats
                SET status = 'Available'
                WHERE flat_no = ?
                """;

        try (Connection conn = DBUtil.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(updateTenantSql)) {
                ps.setString(1, moveOutDate);
                ps.setString(2, reason);
                ps.setInt(3, tenantId);
                ps.executeUpdate();
            }

            if (flatNo != null && !flatNo.isBlank()) {
                try (PreparedStatement ps = conn.prepareStatement(updateFlatSql)) {
                    ps.setString(1, flatNo);
                    ps.executeUpdate();
                }
            }

            conn.commit();

            AuditLogDAO.log(
                    AuditActions.TENANT_MOVED_OUT,
                    "Tenant moved out. ID: "
                            + tenantId
                            + ", Name: "
                            + tenant.getName()
                            + ", Phone: "
                            + tenant.getPhone()
                            + ", Flat freed: "
                            + flatNo
                            + ", Move-out date: "
                            + moveOutDate
                            + ", Reason: "
                            + reason
            );

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int countUnpaidRentRows(int tenantId) {
        String sql = """
                SELECT COUNT(*)
                FROM rent_current
                WHERE tenant_id = ?
                  AND status IN ('DUE', 'PARTIAL', 'LATE')
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenantId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean hasArchiveHistory(int tenantId) {
        String sql = """
                SELECT COUNT(*)
                FROM rent_archive
                WHERE tenant_id = ?
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenantId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static int countTenantRentHistory(int tenantId) {
        String sql = """
                SELECT
                    (
                        SELECT COUNT(*)
                        FROM rent_current
                        WHERE tenant_id = ?
                    )
                    +
                    (
                        SELECT COUNT(*)
                        FROM rent_archive
                        WHERE tenant_id = ?
                    ) AS total_count
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenantId);
            ps.setInt(2, tenantId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total_count") : 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static Tenant getTenantById(int id) {
        String sql = "SELECT * FROM tenants WHERE id=?";

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTenant(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Tenant mapTenant(ResultSet rs) throws Exception {
        Tenant tenant = new Tenant();

        tenant.setId(rs.getInt("id"));
        tenant.setName(rs.getString("name"));
        tenant.setPhone(rs.getString("phone"));
        tenant.setEmail(rs.getString("email"));
        tenant.setNid(rs.getString("nid"));
        tenant.setAddress(rs.getString("address"));
        tenant.setFlatNo(rs.getString("flat_no"));
        tenant.setRent(rs.getDouble("rent"));
        tenant.setNidPath(rs.getString("nid_path"));
        tenant.setDocPath(rs.getString("doc_path"));

        tenant.setStatus(rs.getString("status"));
        tenant.setMoveInDate(rs.getString("move_in_date"));
        tenant.setMoveOutDate(rs.getString("move_out_date"));
        tenant.setMoveOutReason(rs.getString("move_out_reason"));

        tenant.setSecurityDeposit(rs.getDouble("security_deposit"));
        tenant.setSecurityDepositDate(rs.getString("security_deposit_date"));
        tenant.setSecurityDepositNote(rs.getString("security_deposit_note"));

        if (tenant.getStatus() == null || tenant.getStatus().isBlank()) {
            tenant.setStatus("Active");
        }

        return tenant;
    }

    private static String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static double getTotalUnpaidDueByTenant(int tenantId) {
        String sql = """
            SELECT COALESCE(SUM(total - paid_amount), 0)
            FROM rent_current
            WHERE tenant_id = ?
              AND status IN ('DUE', 'PARTIAL', 'LATE')
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenantId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}