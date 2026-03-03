package com.oceanview.servlet;

import com.oceanview.util.DBConnection;
import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@WebServlet("/api/staff")
public class StaffServlet extends HttpServlet {

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
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("role") == null) {
                result.put("success", false); result.put("message", "Not logged in.");
                out.print(gson.toJson(result)); return;
            }

            Connection conn = DBConnection.getInstance().getConnection();

            if ("list".equals(action)) {
                String sql = "SELECT username, full_name, role FROM users ORDER BY role, full_name";
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql);
                List<Map<String, String>> list = new ArrayList<>();
                while (rs.next()) {
                    Map<String, String> s = new HashMap<>();
                    s.put("username", rs.getString("username"));
                    s.put("fullName", rs.getString("full_name"));
                    s.put("role",     rs.getString("role"));
                    list.add(s);
                }
                result.put("success", true);
                result.put("data", list);
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
            HttpSession session = req.getSession(false);
            if (session == null || !"admin".equals(session.getAttribute("role"))) {
                result.put("success", false); result.put("message", "Admin access required.");
                out.print(gson.toJson(result)); return;
            }

            Connection conn = DBConnection.getInstance().getConnection();

            if ("add".equals(action)) {
                String fullName = req.getParameter("fullName");
                String username = req.getParameter("username");
                String password = req.getParameter("password");

                if (isBlank(fullName) || isBlank(username) || isBlank(password)) {
                    result.put("success", false); result.put("message", "All fields required.");
                    out.print(gson.toJson(result)); return;
                }

                // Check username
                PreparedStatement check = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
                check.setString(1, username.trim());
                if (check.executeQuery().next()) {
                    result.put("success", false); result.put("message", "Username already taken.");
                    out.print(gson.toJson(result)); return;
                }

                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (username, password, full_name, role) VALUES (?, SHA2(?,256), ?, 'staff')");
                ps.setString(1, username.trim());
                ps.setString(2, password);
                ps.setString(3, fullName.trim());
                ps.executeUpdate();
                result.put("success", true);
                result.put("message", "Staff account created.");

            } else if ("delete".equals(action)) {
                String username = req.getParameter("username");
                PreparedStatement check = conn.prepareStatement("SELECT role FROM users WHERE username = ?");
                check.setString(1, username);
                ResultSet rs = check.executeQuery();
                if (rs.next() && "admin".equals(rs.getString("role"))) {
                    result.put("success", false); result.put("message", "Cannot remove admin accounts.");
                    out.print(gson.toJson(result)); return;
                }
                PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE username = ?");
                ps.setString(1, username);
                boolean ok = ps.executeUpdate() > 0;
                result.put("success", ok);
                result.put("message", ok ? "Staff removed." : "Not found.");
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
