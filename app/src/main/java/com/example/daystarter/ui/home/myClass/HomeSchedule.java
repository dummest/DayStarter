package com.example.daystarter.ui.home.myClass;

public class HomeSchedule implements Comparable<HomeSchedule>{
    public String title;
    public long startTime;
    public long endTime;
    public String category;
    public String groupCode;
    public String scheduleId;
    public double latitude;
    public double longitude;
    public String address;

    public HomeSchedule(String title, long startTime, long endTime, String category, String scheduleId, double latitude, double longitude, String address){
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.scheduleId = scheduleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }
    public HomeSchedule(String title, long startTime, long endTime, String category, String groupCode, String scheduleId) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.groupCode = groupCode;
        this.scheduleId = scheduleId;
    }

    public HomeSchedule(String title, long startTime, long endTime, String category, String groupCode, String scheduleId, double latitude, double longitude, String address){
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.groupCode = groupCode;
        this.scheduleId = scheduleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    @Override
    public int compareTo(HomeSchedule homeSchedule) {
        if(homeSchedule.startTime < startTime){
            return 1;
        }
        else if (homeSchedule.startTime > startTime){
            return -1;
        }
        return 0;
    }
}
