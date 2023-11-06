package com.javajedis.bookit.model;

public class BookingsModel {
    String room;
    int image;
    String timeSlot;
    String date;
    String action;

    public BookingsModel(String room, int image, String timeSlot, String date, String action) {
        this.room = room;
        this.image = image;
        this.timeSlot = timeSlot;
        this.date = date;
        this.action = action;
    }

    public String getRoom() {
        return room;
    }

    public int getImage() {
        return image;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public String getDate() {
        return date;
    }

    public String getAction() {
        return action;
    }
}
