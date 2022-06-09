package com.example.daystarter.ui.groupSchedule.myClass;

public class Member {
    public String name;
    public String status;
    public String email;
    public String uid;

    public Member(String name, String status, String email, String uid){
        this.name = name;
        this.status = status;
        this.email = email;
        this.uid = uid;
    }

    public Member(String name, String email, String uid){
        this.name = name;
        this.email = email;
        this.uid = uid;
    }

    public Member(){
    }
}
