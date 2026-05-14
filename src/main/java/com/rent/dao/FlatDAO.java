package com.rent.dao;

import com.rent.model.Flat;
import com.rent.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class FlatDAO {

    private static final String INSERT_SQL =
            "INSERT INTO flats " +
                    "(flat_no, meter_no, bedrooms, bathrooms, kitchens, balconies, dining_rooms, living_rooms, rent, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static boolean deleteFlat(String flatNo) {

        String sql = "DELETE FROM flats WHERE flat_no=?";

        try (Connection conn = DBUtil.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, flatNo);
            boolean success = stmt.executeUpdate() > 0;

            if (success) {
                AuditLogDAO.log(
                        AuditActions.FLAT_DELETED,
                        "Flat deleted. Flat No: " + flatNo
                );
            }

            return success;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateFlat(Flat flat) {
        String sql = """
        UPDATE flats SET
        meter_no=?,
        bedrooms=?,
        bathrooms=?,
        kitchens=?,
        balconies=?,
        dining_rooms=?,
        living_rooms=?,
        rent=?,
        status=?
        WHERE flat_no=?
        """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, flat.getMeterNo());
            stmt.setInt(2, flat.getBedrooms());
            stmt.setInt(3, flat.getBathrooms());
            stmt.setInt(4, flat.getKitchens());
            stmt.setInt(5, flat.getBalconies());
            stmt.setInt(6, flat.getDiningrooms());
            stmt.setInt(7, flat.getLivingrooms());
            stmt.setDouble(8, flat.getRent());
            stmt.setString(9, flat.getStatus());
            stmt.setString(10, flat.getFlatNo());

            boolean success = stmt.executeUpdate() > 0;

            if (success) {
                AuditLogDAO.log(
                        AuditActions.FLAT_UPDATED,
                        "Flat updated. Flat No: "
                                + flat.getFlatNo()
                                + ", Meter: "
                                + flat.getMeterNo()
                                + ", Rent: "
                                + flat.getRent()
                                + ", Status: "
                                + flat.getStatus()
                );
            }

            return success;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static List<Flat> getAllFlats() {

        List<Flat> flats = new ArrayList<>();
        String sql = "SELECT * FROM flats ORDER BY flat_no";

        try (Connection conn = DBUtil.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                flats.add(new Flat(
                        rs.getString("flat_no"),
                        rs.getString("meter_no"),
                        rs.getInt("bedrooms"),
                        rs.getInt("bathrooms"),
                        rs.getInt("kitchens"),
                        rs.getInt("balconies"),
                        rs.getInt("dining_rooms"),
                        rs.getInt("living_rooms"),
                        rs.getDouble("rent"),
                        rs.getString("status")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return flats;
    }

    // ✅ Get meter number using flat_no (used by Tenants table derived column)
    public static String getMeterNoByFlatNo(String flatNo) {
        String sql = "SELECT meter_no FROM flats WHERE flat_no=?";
        try (Connection conn = DBUtil.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, flatNo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("meter_no");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ""; // return empty if not found
    }


    public static boolean saveFlat(Flat flat) {

        try (Connection conn = DBUtil.connect();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            stmt.setString(1, flat.getFlatNo());
            stmt.setString(2, flat.getMeterNo());
            stmt.setInt(3, flat.getBedrooms());
            stmt.setInt(4, flat.getBathrooms());
            stmt.setInt(5, flat.getKitchens());
            stmt.setInt(6, flat.getBalconies());
            stmt.setInt(7, flat.getDiningrooms());
            stmt.setInt(8, flat.getLivingrooms());
            stmt.setDouble(9, flat.getRent());
            stmt.setString(10, flat.getStatus());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                AuditLogDAO.log(
                        AuditActions.FLAT_ADDED,
                        "Flat added. Flat No: "
                                + flat.getFlatNo()
                                + ", Meter: "
                                + flat.getMeterNo()
                                + ", Rent: "
                                + flat.getRent()
                                + ", Status: "
                                + flat.getStatus()
                );
            }

            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static List<String> getAvailableFlatNumbers() {

        List<String> flats = new ArrayList<>();
        String sql = "SELECT flat_no FROM flats WHERE status = 'Available' ORDER BY flat_no";

        try (Connection conn = DBUtil.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                flats.add(rs.getString("flat_no"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return flats;
    }

    public static void markFlatOccupied(String flatNo) {

        String sql = "UPDATE flats SET status='Occupied' WHERE flat_no=?";

        try (Connection conn = DBUtil.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, flatNo);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getOccupiedFlatNumbers() {
        List<String> flats = new ArrayList<>();
        String sql = "SELECT flat_no FROM flats WHERE status = 'Occupied' ORDER BY flat_no";
        try (Connection conn = DBUtil.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                flats.add(rs.getString("flat_no"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flats;
    }

    public static Flat getFlatByFlatNo(String flatNo) {
        String sql = "SELECT * FROM flats WHERE flat_no=?";
        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, flatNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Flat(
                            rs.getString("flat_no"),
                            rs.getString("meter_no"),
                            rs.getInt("bedrooms"),
                            rs.getInt("bathrooms"),
                            rs.getInt("kitchens"),
                            rs.getInt("balconies"),
                            rs.getInt("dining_rooms"),
                            rs.getInt("living_rooms"),
                            rs.getDouble("rent"),
                            rs.getString("status")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void markFlatAvailable(String flatNo) {

        String sql = "UPDATE flats SET status='Available' WHERE flat_no=?";

        try (Connection conn = DBUtil.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, flatNo);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static double getRentByFlatNo(String flatNo) {
        String sql = "SELECT rent FROM flats WHERE flat_no=?";
        try (Connection conn = DBUtil.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, flatNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("rent");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static boolean saveFlatWithProperty(Flat flat, int propertyId) {

        String updatePropertySql = "UPDATE flats SET property_id=? WHERE flat_no=?";

        try (Connection conn = DBUtil.connect()) {
            conn.setAutoCommit(false);

            // 1) insert flat (existing INSERT_SQL)
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
                stmt.setString(1, flat.getFlatNo());
                stmt.setString(2, flat.getMeterNo());
                stmt.setInt(3, flat.getBedrooms());
                stmt.setInt(4, flat.getBathrooms());
                stmt.setInt(5, flat.getKitchens());
                stmt.setInt(6, flat.getBalconies());
                stmt.setInt(7, flat.getDiningrooms());
                stmt.setInt(8, flat.getLivingrooms());
                stmt.setDouble(9, flat.getRent());
                stmt.setString(10, flat.getStatus());
                stmt.executeUpdate();
            }

            // 2) set property_id
            try (PreparedStatement ps = conn.prepareStatement(updatePropertySql)) {
                ps.setInt(1, propertyId);
                ps.setString(2, flat.getFlatNo());
                ps.executeUpdate();
            }

            conn.commit();

            AuditLogDAO.log(
                    AuditActions.FLAT_ADDED,
                    "Flat added. Flat No: "
                            + flat.getFlatNo()
                            + ", Meter: "
                            + flat.getMeterNo()
                            + ", Rent: "
                            + flat.getRent()
                            + ", Status: "
                            + flat.getStatus()
                            + ", Property ID: "
                            + propertyId
            );

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}