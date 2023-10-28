package com.javajedis.bookit;

public class TimeSlotsModel {
    String timeInterval;
    int image;
    String status;

    public TimeSlotsModel(String timeInterval, int image, String status) {
        this.timeInterval = timeInterval;
        this.image = image;
        this.status = status;
    }

    public String getTimeInterval() {
        return timeInterval;
    }

    public int getImage() {
        return image;
    }

    public String getStatus() {
        return status;
    }
}
