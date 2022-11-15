package com.example.daystarter.ui.groupSchedule.myClass;


public class GroupScheduleModel {
    public String key;
    public String writerUid;
    public long writingTime;
    public String title;
    public long startTime;
    public long endTime;
    public String contents;
    public String address;
    public double latitude;
    public double longitude;

    public GroupScheduleModel(){

    }

    public GroupScheduleModel(String key, String writerUid, long writingTime, String title, long startTime, long endTime, String contents, String address, double latitude, double longitude) {
        this.key = key;
        this.writerUid = writerUid;
        this.writingTime = writingTime;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.contents = contents;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
