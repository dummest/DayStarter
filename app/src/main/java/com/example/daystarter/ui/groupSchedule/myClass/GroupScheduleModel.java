package com.example.daystarter.ui.groupSchedule.myClass;


public class GroupScheduleModel {
    public String key;
    public String writerUid;
    public long writingTime;
    public String title;
    public long startTime;
    public long endTime;
    public String contents;
    public String imagePath;

    public GroupScheduleModel(){

    }



    public GroupScheduleModel(String key, String writerUid, long writingTime, String title, long startTime, long endTime, String contents, String imagePath) {
        this.key = key;
        this.writerUid = writerUid;
        this.writingTime = writingTime;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.contents = contents;
        this.imagePath = imagePath;
    }

    public GroupScheduleModel(String key, String writerUid, long writingTime, String title, long startTime, long endTime, String contents) {
        this.key = key;
        this.writerUid = writerUid;
        this.writingTime = writingTime;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.contents = contents;
    }
}
