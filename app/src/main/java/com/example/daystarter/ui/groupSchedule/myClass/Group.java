package com.example.daystarter.ui.groupSchedule.myClass;

public class Group {
    public String groupId;
    public String groupName;
    public String hostEmail;
    public String imagePath;

    public Group(String groupId, String groupName, String hostEmail, String imagePath){
        this.groupId = groupId;
        this.groupName = groupName;
        this.hostEmail = hostEmail;
        this.imagePath = imagePath;
    }

    public Group(){

    }
}
