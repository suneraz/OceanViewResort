package com.oceanview.model;

public class Reservation {
    private String reservationNumber;
    private String guestName;
    private String address;
    private String contactNumber;
    private String roomNumber;
    private String roomType;
    private String checkInDate;
    private String checkOutDate;
    private String status;

    public Reservation() {}

    public String getReservationNumber() { return reservationNumber; }
    public void setReservationNumber(String v) { this.reservationNumber = v; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String v) { this.guestName = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String v) { this.contactNumber = v; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String v) { this.roomNumber = v; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String v) { this.roomType = v; }
    public String getCheckInDate() { return checkInDate; }
    public void setCheckInDate(String v) { this.checkInDate = v; }
    public String getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(String v) { this.checkOutDate = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
}
