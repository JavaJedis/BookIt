package com.javajedis.bookit;

public class RoomModel {
    String roomName;
    int image;

    public RoomModel(String roomName, int image) {
        this.roomName = roomName;
        this.image = image;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getImage() {
        return image;
    }
}
