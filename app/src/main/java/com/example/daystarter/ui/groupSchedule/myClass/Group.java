package com.example.daystarter.ui.groupSchedule.myClass;

public class Group {
    public String groupId;
    public String groupName;
    public String hostEmail;
    public String imagePath;
    public String initialStatus;
    public boolean autoApprove;

    public Group(String groupId, String groupName, String hostEmail, String imagePath, String initialStatus, boolean autoApprove){
        this.groupId = groupId;
        this.groupName = groupName;
        this.hostEmail = hostEmail;
        this.imagePath = imagePath;
        this.initialStatus = initialStatus;
        this.autoApprove = autoApprove;
    }

    public Group(){

    }
}
