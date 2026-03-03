package com.oceanview.servlet;

import com.oceanview.dao.ReservationDAO;
import com.oceanview.model.Reservation;
import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/reservations")
public class ReservationServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = resp.getWriter();
        Map<String, Object> result = new HashMap<>();
        String action = req.getParameter("action");

        try {
            ReservationDAO dao = new ReservationDAO();
            if ("all".equals(action)) {
                result.put("success", true);
                result.put("data", dao.getAllReservations());
            } else if ("get".equals(action)) {
                Reservation r = dao.getReservation(req.getParameter("id"));
                if (r != null) { result.put("success", true); result.put("data", r); }
                else { result.put("success", false); result.put("message", "Reservation not found."); }
            } else if ("byGuest".equals(action)) {
                result.put("success", true);
                result.put("data", dao.searchByName(req.getParameter("name")));
            } else if ("search".equals(action)) {
                result.put("success", true);
                result.put("data", dao.searchByName(req.getParameter("name")));
            } else if ("nextNumber".equals(action)) {
                result.put("success", true);
                result.put("number", dao.generateReservationNumber());
            } else if ("rooms".equals(action)) {
                String type = req.getParameter("type");
                result.put("success", true);
                result.put("rooms", dao.getAvailableRooms(type));
            } else {
                result.put("success", false); result.put("message", "Unknown action.");
            }
        } catch (Exception e) {
            result.put("success", false); result.put("message", "Error: " + e.getMessage());
        }
        out.print(gson.toJson(result));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = resp.getWriter();
        Map<String, Object> result = new HashMap<>();
        String action = req.getParameter("action");

        try {
            ReservationDAO dao = new ReservationDAO();

            if ("add".equals(action)) {
                String guestName  = req.getParameter("guestName");
                String address    = req.getParameter("address");
                String contactNum = req.getParameter("contactNumber");
                String roomType   = req.getParameter("roomType");
                String roomNumber = req.getParameter("roomNumber");
                String checkIn    = req.getParameter("checkInDate");
                String checkOut   = req.getParameter("checkOutDate");

                if (isBlank(guestName))  { error(result, "Guest name required."); out.print(gson.toJson(result)); return; }
                if (isBlank(address))    { error(result, "Address required.");     out.print(gson.toJson(result)); return; }
                if (isBlank(contactNum)) { error(result, "Contact required.");     out.print(gson.toJson(result)); return; }
                if (!contactNum.matches("\\d{10}")) { error(result, "Contact must be 10 digits."); out.print(gson.toJson(result)); return; }
                if (isBlank(roomType))   { error(result, "Room type required.");   out.print(gson.toJson(result)); return; }
                if (isBlank(checkIn))    { error(result, "Check-in required.");    out.print(gson.toJson(result)); return; }
                if (isBlank(checkOut))   { error(result, "Check-out required.");   out.print(gson.toJson(result)); return; }

                Reservation r = new Reservation();
                r.setReservationNumber(dao.generateReservationNumber());
                r.setGuestName(guestName.trim());
                r.setAddress(address.trim());
                r.setContactNumber(contactNum.trim());
                r.setRoomType(roomType);
                r.setRoomNumber(isBlank(roomNumber) ? null : roomNumber.trim()); // null = auto-assign
                r.setCheckInDate(checkIn);
                r.setCheckOutDate(checkOut);
                r.setStatus("Confirmed");

                boolean saved = dao.addReservation(r);
                result.put("success", saved);
                result.put("reservationNumber", r.getReservationNumber());
                result.put("roomNumber", r.getRoomNumber());
                result.put("message", saved ? "Reservation confirmed!" : "Failed to save.");

            } else if ("cancel".equals(action)) {
                boolean ok = dao.cancelReservation(req.getParameter("id"));
                result.put("success", ok);
                result.put("message", ok ? "Cancelled." : "Could not cancel.");

            } else if ("delete".equals(action)) {
                boolean ok = dao.deleteReservation(req.getParameter("id"));
                result.put("success", ok);
                result.put("message", ok ? "Deleted." : "Could not delete.");
            }

        } catch (Exception e) {
            result.put("success", false); result.put("message", "Error: " + e.getMessage());
        }
        out.print(gson.toJson(result));
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private void error(Map<String, Object> r, String msg) { r.put("success", false); r.put("message", msg); }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(200);
    }
}
