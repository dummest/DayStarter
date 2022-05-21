package com.example.daystarter.ui.groupSchedule.myClass;

public class Comment {
    public String writerUid;
    public String contents;
    public long writingTime;

    public Comment(){

    }

    public Comment(String writerUid, String contents, long writingTime){
        this.writerUid = writerUid;
        this.contents = contents;
        this.writingTime = writingTime;
    }


}
