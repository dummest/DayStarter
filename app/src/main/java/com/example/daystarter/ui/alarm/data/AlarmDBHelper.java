package com.example.daystarter.ui.alarm.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlarmDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "myDBHelper";
    private static final String DB_NAME = "Alarm.db";

    public AlarmDBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS AlarmTBL (" +
                "alarmId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "hour INTEGER, " +
                "minute INTEGER, " +
                "title TEXT, " +
                "started BOOLEAN, " +
                "recurring BOOLEAN, " +
                "monday BOOLEAN, " +
                "tuesday BOOLEAN, " +
                "wednesday BOOLEAN, " +
                "thursday BOOLEAN, " +
                "friday BOOLEAN, " +
                "saturday BOOLEAN, " +
                "sunday BOOLEAN" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS notiTBL;");
        onCreate(db);
    }

    public void insertAlarm(Alarm alarm) {
        int alarmId =alarm.getAlarmId();
        int hour = alarm.getHour();
        int minute = alarm.getMinute();
        String title = alarm.getTitle();
        Boolean startTime = alarm.isStarted();
        Boolean recurring = alarm.isRecurring();
        Boolean monday = alarm.isMonday();
        Boolean tuesday = alarm.isTuesday();
        Boolean wednesday = alarm.isWednesday();
        Boolean thursday = alarm.isThursday();
        Boolean friday = alarm.isFriday();
        Boolean saturday = alarm.isSaturday();
        Boolean sunday = alarm.isSunday();

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO AlarmTBL(alarmId,hour, minute, title, startTime, recurring, monday,tuesday,wednesday,thursday,friday,saturday,sunday) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
                new Object[]{alarmId,hour, minute, title, startTime, recurring, monday, tuesday, wednesday, thursday, friday, saturday, sunday});
        db.close();
    }

    public void deleteAlarm(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM AlarmTBL WHERE alarmId = " + id + ";");
        db.close();
    }

    public void updateAlarm(Alarm alarm) {
        int alarmId = alarm.getAlarmId();
        int hour = alarm.getHour();
        int minute = alarm.getMinute();
        String title = alarm.getTitle();
        Boolean startTime = alarm.isStarted();
        Boolean recurring = alarm.isRecurring();
        Boolean monday = alarm.isMonday();
        Boolean tuesday = alarm.isTuesday();
        Boolean wednesday = alarm.isWednesday();
        Boolean thursday = alarm.isThursday();
        Boolean friday = alarm.isFriday();
        Boolean saturday = alarm.isSaturday();
        Boolean sunday = alarm.isSunday();

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE AlarmTBL SET hour = ?, minute = ?, title = ?, startTime = ?, recurring = ?" +
                        ",monday = ?, tuesday = ?, wednesday = ?, thursday = ?, friday = ?, saturday = ?, sunday = ? WHERE alarmId =?",
                new Object[]{hour, minute, title, startTime, recurring, monday, tuesday, wednesday, thursday, friday, saturday, sunday});
        db.close();
    }
}