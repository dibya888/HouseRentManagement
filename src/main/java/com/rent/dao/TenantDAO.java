package com.rent.dao;

import com.rent.model.Tenant;
import com.rent.util.DBUtil;
import com.rent.util.AuditActions;
import com.rent.dao.AuditLogDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class TenantDAO {

    // CREATE
    public static void addTenant(Tenant t) {

        String sql =
                "INSERT INTO tenants " +
                        "(name, phone, email, nid, address, flat_no, rent, nid_path, doc_path) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection conn = DBUtil.connect();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, t.getName());
            ps.setString(2, t.getPhone());
            ps.setString(3, t.getEmail());
            ps.setString(4, t.getNid());
            ps.setString(5, t.getAddress());
            ps.setString(6, t.getFlatNo());
            ps.setDouble(7, t.getRent());
            ps.setString(8, t.getNidPath());
            ps.setString(9, t.getDocPath());

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
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // READ ALL
    public static List<Tenant> getAllTenants() {

        List<Tenant> list = new ArrayList<>();

        String sql = "SELECT * FROM tenants";

        try (
                Connection conn = DBUtil.connect();

                Statement st = conn.createStatement();

                ResultSet rs = st.executeQuery(sql)
        ) {

            while (rs.next()) {

                Tenant tenant = new Tenant();

                tenant.setId(
                        rs.getInt("id")
                );

                tenant.setName(
                        rs.getString("name")
                );

                tenant.setPhone(
                        rs.getString("phone")
                );

                tenant.setEmail(
                        rs.getString("email")
                );

                tenant.setNid(
                        rs.getString("nid")
                );

                tenant.setAddress(
                        rs.getString("address")
                );

                tenant.setFlatNo(
                        rs.getString("flat_no")
                );

                tenant.setRent(
                        rs.getDouble("rent")
                );

                tenant.setNidPath(
                        rs.getString("nid_path")
                );

                tenant.setDocPath(
                        rs.getString("doc_path")
                );

                list.add(tenant);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // UPDATE
    public static void updateTenant(Tenant t) {
        String sql = "UPDATE tenants SET name=?, phone=?, email=?, flat_no=?, rent=?, address=?, nid_path=?, doc_path=? WHERE id=?";
        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getName());
            ps.setString(2, t.getPhone());
            ps.setString(3, t.getEmail());
            ps.setString(4, t.getFlatNo());
            ps.setDouble(5, t.getRent());
            ps.setString(6, t.getAddress());
            ps.setString(7, t.getNidPath());
            ps.setString(8, t.getDocPath());
            ps.setInt(9, t.getId());

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
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public static void deleteTenant(int id) {
        Tenant tenant = getTenantById(id);

        String sql = "DELETE FROM tenants WHERE id=?";

        try (
                Connection conn = DBUtil.connect();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {
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

    public static Tenant getTenantById(int id) {
        String sql = "SELECT * FROM tenants WHERE id=?";
        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Tenant t = new Tenant();
                    t.setId(rs.getInt("id"));
                    t.setName(rs.getString("name"));
                    t.setPhone(rs.getString("phone"));
                    t.setEmail(rs.getString("email"));
                    t.setNid(rs.getString("nid"));
                    t.setAddress(rs.getString("address"));
                    t.setFlatNo(rs.getString("flat_no"));
                    t.setRent(rs.getDouble("rent"));
                    t.setNidPath(rs.getString("nid_path"));
                    t.setDocPath(rs.getString("doc_path"));
                    return t;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteTenantAndFreeFlat(int tenantId) {
        String tenantName = null;
        String tenantPhone = null;

        String selectFlatSql = "SELECT name, phone, flat_no FROM tenants WHERE id=?";
        String deleteTenantSql = "DELETE FROM tenants WHERE id=?";
        String updateFlatSql = "UPDATE flats SET status='Available' WHERE flat_no=?";

        try (Connection conn = DBUtil.connect()) {

            conn.setAutoCommit(false); // ✅ one transaction

            String flatNo = null;

            // 1️⃣ Get flat number
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

            // 2️⃣ Delete tenant
            try (PreparedStatement ps = conn.prepareStatement(deleteTenantSql)) {
                ps.setInt(1, tenantId);
                ps.executeUpdate();
            }

            // 3️⃣ Mark flat available (SAME connection)
            if (flatNo != null && !flatNo.isBlank()) {
                try (PreparedStatement ps = conn.prepareStatement(updateFlatSql)) {
                    ps.setString(1, flatNo);
                    ps.executeUpdate();
                }
            }

            conn.commit(); // ✅ commit both operations
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
}