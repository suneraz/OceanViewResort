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

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = resp.getWriter();
        Map<String, Object> result = new HashMap<>();

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.trim().isEmpty()) {
            result.put("success", false); result.put("message", "Username is required.");
            out.print(gson.toJson(result)); return;
        }
        if (password == null || password.trim().isEmpty()) {
            result.put("success", false); result.put("message", "Password is required.");
            out.print(gson.toJson(result)); return;
        }

        try {
            Connection conn = DBConnection.getInstance().getConnection();
            String sql = "SELECT * FROM users WHERE username = ? AND password = SHA2(?, 256)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username.trim());
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                HttpSession session = req.getSession(true);
                session.setAttribute("username", rs.getString("username"));
                session.setAttribute("fullName", rs.getString("full_name"));
                session.setAttribute("role", rs.getString("role"));
                session.setMaxInactiveInterval(30 * 60);

                result.put("success", true);
                result.put("username", rs.getString("username"));
                result.put("fullName", rs.getString("full_name"));
                result.put("role", rs.getString("role"));
            } else {
                result.put("success", false);
                result.put("message", "Invalid username or password.");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Server error: " + e.getMessage());
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
}
