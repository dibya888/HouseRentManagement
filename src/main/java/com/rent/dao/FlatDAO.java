package com.rent.dao;

import com.rent.model.Flat;
import com.rent.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;


import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class FlatDAO {

    private static final String INSERT_SQL =
            "INSERT INTO flats " +
                    "(flat_no, bedrooms, bathrooms, kitchens, balconies, dining_rooms, living_rooms, rent, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static boolean deleteFlat(String flatNo) {

        String sql = "DELETE FROM flats WHERE flat_no=?";

        try (Connection conn = DBUtil.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, flatNo);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateFlat(Flat flat) {

        String sql = """
        UPDATE flats SET
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

            stmt.setInt(1, flat.getBedrooms());
            stmt.setInt(2, flat.getBathrooms());
            stmt.setInt(3, flat.getKitchens());
            stmt.setInt(4, flat.getBalconies());
            stmt.setInt(5, flat.getDiningrooms());
            stmt.setInt(6, flat.getLivingrooms());
            stmt.setDouble(7, flat.getRent());
            stmt.setString(8, flat.getStatus());
            stmt.setString(9, flat.getFlatNo());

            return stmt.executeUpdate() > 0;

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


    public static boolean saveFlat(Flat flat) {

        try (Connection conn = DBUtil.connect();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            stmt.setString(1, flat.getFlatNo());
            stmt.setInt(2, flat.getBedrooms());
            stmt.setInt(3, flat.getBathrooms());
            stmt.setInt(4, flat.getKitchens());
            stmt.setInt(5, flat.getBalconies());
            stmt.setInt(6, flat.getDiningrooms());
            stmt.setInt(7, flat.getLivingrooms());
            stmt.setDouble(8, flat.getRent());
            stmt.setString(9, flat.getStatus());

            stmt.executeUpdate();
            return true;

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

}