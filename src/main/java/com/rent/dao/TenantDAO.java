package com.rent.dao;

import com.rent.model.Tenant;
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

            ps.executeUpdate();

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

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public static void deleteTenant(int id) {

        String sql =
                "DELETE FROM tenants WHERE id=?";

        try (
                Connection conn = DBUtil.connect();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setInt(1, id);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}