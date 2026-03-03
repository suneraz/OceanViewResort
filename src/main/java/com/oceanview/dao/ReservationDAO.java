package com.oceanview.dao;

import com.oceanview.model.Reservation;
import com.oceanview.util.DBConnection;
import com.oceanview.util.FileStorage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    private Connection conn;

    public ReservationDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    public String generateReservationNumber() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        rs.next();
        int count = rs.getInt(1) + 1;
        return String.format("RES-%04d", count);
    }

    /** assign room */
    public String autoAssignRoom(String roomType) throws SQLException {
        String sql = "SELECT room_number FROM rooms WHERE room_type = ? AND is_available = TRUE LIMIT 1";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, roomType);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getString("room_number");
        return null;
    }

    public List<String> getAvailableRooms(String roomType) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT room_number FROM rooms WHERE room_type = ? AND is_available = TRUE";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, roomType);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(rs.getString("room_number"));
        return list;
    }

    /**room unavailable */
    private void markRoomUnavailable(String roomNumber) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("UPDATE rooms SET is_available = FALSE WHERE room_number = ?");
        ps.setString(1, roomNumber);
        ps.executeUpdate();
    }

    /** Mark room available */
    private void markRoomAvailable(String roomNumber) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("UPDATE rooms SET is_available = TRUE WHERE room_number = ?");
        ps.setString(1, roomNumber);
        ps.executeUpdate();
    }

    public boolean addReservation(Reservation r) throws SQLException {
        if (r.getRoomNumber() == null || r.getRoomNumber().isEmpty()) {
            String assigned = autoAssignRoom(r.getRoomType());
            if (assigned == null) throw new SQLException("No available rooms for type: " + r.getRoomType());
            r.setRoomNumber(assigned);
        }
        String sql = "INSERT INTO reservations (reservation_number, guest_name, address, contact_number, "
                + "room_number, room_type, check_in_date, check_out_date, status) VALUES (?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, r.getReservationNumber());
        ps.setString(2, r.getGuestName());
        ps.setString(3, r.getAddress());
        ps.setString(4, r.getContactNumber());
        ps.setString(5, r.getRoomNumber());
        ps.setString(6, r.getRoomType());
        ps.setString(7, r.getCheckInDate());
        ps.setString(8, r.getCheckOutDate());
        ps.setString(9, r.getStatus());
        boolean saved = ps.executeUpdate() > 0;
        if (saved) {
            markRoomUnavailable(r.getRoomNumber());
            FileStorage.saveReservation(r);
        }
        return saved;
    }

    public List<Reservation> getAllReservations() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY created_at DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) list.add(mapRow(rs));
        return list;
    }

    public List<Reservation> searchByName(String name) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE guest_name LIKE ? ORDER BY created_at DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + name + "%");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapRow(rs));
        return list;
    }

    public Reservation getReservation(String number) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE reservation_number = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, number);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return mapRow(rs);
        return null;
    }

    public boolean cancelReservation(String number) throws SQLException {
        
        Reservation r = getReservation(number);
        if (r != null && r.getRoomNumber() != null) markRoomAvailable(r.getRoomNumber());
        String sql = "UPDATE reservations SET status = 'Cancelled' WHERE reservation_number = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, number);
        return ps.executeUpdate() > 0;
    }

    public boolean deleteReservation(String number) throws SQLException {
        
        Reservation r = getReservation(number);
        if (r != null && r.getRoomNumber() != null) markRoomAvailable(r.getRoomNumber());
        String sql = "DELETE FROM reservations WHERE reservation_number = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, number);
        return ps.executeUpdate() > 0;
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationNumber(rs.getString("reservation_number"));
        r.setGuestName(rs.getString("guest_name"));
        r.setAddress(rs.getString("address"));
        r.setContactNumber(rs.getString("contact_number"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setRoomType(rs.getString("room_type"));
        r.setCheckInDate(rs.getString("check_in_date"));
        r.setCheckOutDate(rs.getString("check_out_date"));
        r.setStatus(rs.getString("status"));
        return r;
    }
}
