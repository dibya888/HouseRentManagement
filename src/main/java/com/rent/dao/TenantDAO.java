package com.rent.dao;

import com.rent.model.Tenant;
import com.rent.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TenantDAO {

    // CREATE
    public static void addTenant(Tenant t) {
        String sql = "INSERT INTO tenants(name, phone, email, nid, address) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getName());
            ps.setString(2, t.getPhone());
            ps.setString(3, t.getEmail());
            ps.setString(4, t.getNid());
            ps.setString(5, t.getAddress());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // READ ALL
    public static List<Tenant> getAllTenants() {
        List<Tenant> list = new ArrayList<>();
        String sql = "SELECT * FROM tenants";

        try (Connection conn = DBUtil.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Tenant(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("nid"),
                        rs.getString("address")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // UPDATE
    public static void updateTenant(Tenant t) {
        String sql = "UPDATE tenants SET name=?, phone=?, email=?, nid=?, address=? WHERE id=?";

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getName());
            ps.setString(2, t.getPhone());
            ps.setString(3, t.getEmail());
            ps.setString(4, t.getNid());
            ps.setString(5, t.getAddress());
            ps.setInt(6, t.getId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public static void deleteTenant(int id) {
        String sql = "DELETE FROM tenants WHERE id=?";

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}