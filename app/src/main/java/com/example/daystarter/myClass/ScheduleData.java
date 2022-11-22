package com.example.daystarter.myClass;

import java.net.URI;

public class ScheduleData {
    private int scheduleId;
    private String title;
    private long startTime;
    private long endTime;
    private String memo;
    private String address;
    private double latitude;
    private double longitude;
    private String imgPath;

    public ScheduleData(){}

    public ScheduleData(String title, long startTime, long endTime, String memo, String address, double latitude, double longitude, String imgPath) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imgPath = imgPath;
    }

    public ScheduleData(int scheduleId, String title, long startTime, long endTime, String memo, String address, double latitude, double longitude, String imgPath) {
        this.scheduleId = scheduleId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imgPath = imgPath;
    }







    public int getScheduleId() { return scheduleId; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }
}
