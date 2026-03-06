# OceanView Resort Management System

A web-based resort management system built with Java, JSP, and Apache Tomcat.

## Features
- Admin/Staff login with authentication
- Dashboard for managing reservations
- Add, view, and manage guest reservations
- Display reservation details
- Calculate guest bills
- Add Staff (Admin only)
- CSV-based data storage

## Technologies Used
- Java
- JSP / HTML / CSS / JavaScript
- Apache Tomcat
- Maven
- NetBeans IDE

## Project Structure
OceanViewResort/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/oceanview/
│       │       ├── dao/
│       │       │   └── ReservationDAO.java
│       │       ├── model/
│       │       │   └── Reservation.java
│       │       ├── servlet/
│       │       │   ├── GuestServlet.java
│       │       │   ├── LoginServlet.java
│       │       │   ├── LogoutServlet.java
│       │       │   ├── ReservationServlet.java
│       │       │   └── StaffServlet.java
│       │       └── util/
│       │           ├── DBConnection.java
│       │           └── FileStorage.java
│       └── webapp/
│           ├── login.html
│           ├── login.css
│           ├── login.js
│           ├── dashboard.html
│           ├── dashboard.css
│           └── dashboard.js
├── oceanview_reservations.csv
└── pom.xml

## How to Run
1. Clone the repository
2. Open project in NetBeans
3. Make sure Apache Tomcat is configured
4. Run the project
5. Open browser and go to `http://localhost:8080/OceanViewResort`

## How It Works
1. Admin/Staff logs in through the login page
2. After authentication, redirected to the dashboard
3. Dashboard displays all current reservations
4. Staff can:
   - Add new reservations
   - View all reservations
   - Display reservation details
   - Calculate guest bills
5. Admin can additionally:
   - Add new staff members
6. All reservation data is saved to `oceanview_reservations.csv`

## User Roles
| Role | Access |
|------|--------|
| Staff | Dashboard, Reservations, Bills |
| Admin | All features + Add Staff |

## Version History
- v1.0 - Initial release
- v1.2 - Final release, all features complete
- v1.3 - Updated file path and reservations data
