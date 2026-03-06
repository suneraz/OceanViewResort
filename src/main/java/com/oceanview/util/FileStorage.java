package com.oceanview.util;

import com.oceanview.model.Reservation;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileStorage {

    private static final String FILE_PATH = System.getProperty("user.dir") + "/oceanview_reservations.csv";
    private static final String HEADER    = "ReservationNumber,GuestName,Address,ContactNumber,RoomNumber,RoomType,CheckInDate,CheckOutDate,Status";

    public static void saveReservation(Reservation r) {
        try {
            File file = new File(FILE_PATH);
            boolean isNew = !file.exists() || file.length() == 0;

            try (FileWriter fw = new FileWriter(file, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {

                if (isNew) {
                    bw.write(HEADER);
                    bw.newLine();
                }

                bw.write(
                    csv(r.getReservationNumber()) + "," +
                    csv(r.getGuestName())         + "," +
                    csv(r.getAddress())            + "," +
                    csv(r.getContactNumber())      + "," +
                    csv(r.getRoomNumber())          + "," +
                    csv(r.getRoomType())            + "," +
                    csv(r.getCheckInDate())         + "," +
                    csv(r.getCheckOutDate())        + "," +
                    csv(r.getStatus())
                );
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("CSV write error: " + e.getMessage());
        }
    }

    public static List<Reservation> readAllReservations() {
        List<Reservation> list = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                String[] parts = line.split(",", -1);
                if (parts.length >= 9) {
                    Reservation r = new Reservation();
                    r.setReservationNumber(unquote(parts[0]));
                    r.setGuestName(unquote(parts[1]));
                    r.setAddress(unquote(parts[2]));
                    r.setContactNumber(unquote(parts[3]));
                    r.setRoomNumber(unquote(parts[4]));
                    r.setRoomType(unquote(parts[5]));
                    r.setCheckInDate(unquote(parts[6]));
                    r.setCheckOutDate(unquote(parts[7]));
                    r.setStatus(unquote(parts[8]));
                    list.add(r);
                }
            }
        } catch (IOException e) {
            System.err.println("CSV read error: " + e.getMessage());
        }
        return list;
    }

    private static String csv(String val) {
        if (val == null) return "\"\"";
        return "\"" + val.replace("\"", "\"\"") + "\"";
    }

    private static String unquote(String val) {
        if (val == null) return "";
        val = val.trim();
        if (val.startsWith("\"") && val.endsWith("\""))
            val = val.substring(1, val.length() - 1).replace("\"\"", "\"");
        return val;
    }
}