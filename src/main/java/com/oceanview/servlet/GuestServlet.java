package com.oceanview.servlet;

import com.oceanview.util.DBConnection;
import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/guest")
public class GuestServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = resp.getWriter();
        Map<String, Object> result = new HashMap<>();
        String action = req.getParameter("action");

        try {
            Connection conn = DBConnection.getInstance().getConnection();

            if ("register".equals(action)) {
                String fullName  = req.getParameter("fullName");
                String username  = req.getParameter("username");
                String phone     = req.getParameter("phone");
                String nic       = req.getParameter("nic");
                String address   = req.getParameter("address");
                String password  = req.getParameter("password");

                if (isBlank(fullName) || isBlank(username) || isBlank(phone) || isBlank(nic) || isBlank(password)) {
                    result.put("success", false); result.put("message", "All fields are required.");
                    out.print(gson.toJson(result)); return;
                }

                // Check username exists
                PreparedStatement check = conn.prepareStatement("SELECT id FROM guests WHERE username = ?");
                check.setString(1, username.trim());
                if (check.executeQuery().next()) {
                    result.put("success", false); result.put("message", "Username already taken. Choose another.");
                    out.print(gson.toJson(result)); return;
                }

                String sql = "INSERT INTO guests (full_name, username, phone, nic, address, password) VALUES (?,?,?,?,?,SHA2(?,256))";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, fullName.trim());
                ps.setString(2, username.trim());
                ps.setString(3, phone.trim());
                ps.setString(4, nic.trim());
                ps.setString(5, address.trim());
                ps.setString(6, password);
                ps.executeUpdate();
                result.put("success", true);
                result.put("message", "Account created successfully!");

            } else if ("login".equals(action)) {
                String username = req.getParameter("username");
                String password = req.getParameter("password");

                String sql = "SELECT * FROM guests WHERE username = ? AND password = SHA2(?, 256)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, username.trim());
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    result.put("success", true);
                    result.put("id",       rs.getInt("id"));
                    result.put("fullName", rs.getString("full_name"));
                    result.put("username", rs.getString("username"));
                    result.put("phone",    rs.getString("phone"));
                    result.put("address",  rs.getString("address"));
                } else {
                    result.put("success", false);
                    result.put("message", "Invalid username or password.");
                }
            }

        } catch (Exception e) {
            result.put("success", false); result.put("message", "Error: " + e.getMessage());
        }
        out.print(gson.toJson(result));
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(200);
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
