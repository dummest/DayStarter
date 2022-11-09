package com.example.daystarter.myClass;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


public class PersonalScheduleDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "myDBHelper";
    private static final String DB_NAME = "PersonalSchedule.db";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", Locale.getDefault());
    private String query = "";

    public PersonalScheduleDBHelper(Context context){
        super(context, DB_NAME, null, 3);
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS PersonalScheduleTBL (" +
                "scheduleId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT DEFAULT '내 일정', " +
                "startTime INTEGER, " +
                "endTime INTEGER, " +
                "memo TEXT, " +
                "address TEXT, " +
                "latitude DOUBLE, " +
                "longitude DOUBLE, " +
                "imgPath Text" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS PersonalScheduleTBL;");
        Log.d(TAG, "onUpgrade");
        onCreate(db);
    }

    public void insertSchedule(ScheduleData data){
        String title = data.getTitle();
        long startTime = data.getStartTime();
        long endTime = data.getEndTime();
        String memo = data.getMemo();
        String address = data.getAddress();
        double latitude = data.getLatitude();
        double longitude = data.getLongitude();

        String imgPath = data.getImgPath();
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO PersonalScheduleTBL(title, startTime, endTime, memo, address, latitude, longitude, imgPath) VALUES(?, ?, ?, ?, ?, ?, ?, ?);",
                new Object[]{title, startTime, endTime, memo, address, latitude, longitude, imgPath});
        db.close();
        Date date = new Date(startTime);
    }

    public void updateSchedule(ScheduleData data){
        int scheduleId = data.getScheduleId();
        String title = data.getTitle();
        long startTime = data.getStartTime();
        long endTime = data.getEndTime();
        String memo = data.getMemo();
        String address = data.getAddress();
        double latitude = data.getLatitude();
        double longitude = data.getLongitude();
        String imgPath = data.getImgPath();

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE PersonalScheduleTBL SET title = ?, startTime = ?, endTime = ?, memo = ?, address = ?, latitude = ?, longitude = ?, imgPath = ? WHERE scheduleId = ?",
                new Object[]{title, startTime, endTime, memo, address, latitude, longitude, imgPath, scheduleId});
        db.close();
    }

    public void deleteSchedule(int scheduleId){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM PersonalScheduleTBL WHERE scheduleId = ?", new Object[]{scheduleId});
        db.close();
    }

    public ScheduleData getSchedule(int id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM PersonalScheduleTBL WHERE scheduleId = ?", new String[]{Integer.toString(id)});
        while (cursor.moveToNext()){
            int scheduleId = cursor.getInt(0);
            String title = cursor.getString(1);
            long startTime = cursor.getLong(2);
            long endTime = cursor.getLong(3);
            String memo = cursor.getString(4);
            String address = cursor.getString(5);
            double latitude = cursor.getDouble(6);
            double longitude = cursor.getDouble(7);
            String imgPath = cursor.getString(8);

            ScheduleData scheduleData = new ScheduleData(scheduleId, title, startTime, endTime, memo, address, latitude, longitude, imgPath);
            return scheduleData;
        }
        return null;
    }

    public ArrayList<ScheduleData> getScheduleList(long time){
        ArrayList<ScheduleData> dataList = new ArrayList<ScheduleData>();
        SQLiteDatabase db = getReadableDatabase();

        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        //time <= 검색범위 < calendar.getTimeinMillis()
        Cursor cursor = db.rawQuery("SELECT * FROM PersonalScheduleTBL WHERE endTime >= ? AND startTime < ?  Order BY startTime asc", new String[]{Long.toString(time), Long.toString(calendar.getTimeInMillis())});

        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            long startTime = cursor.getLong(2);
            long endTime = cursor.getLong(3);
            String memo = cursor.getString(4);
            String address = cursor.getString(5);
            double latitude = cursor.getDouble(6);
            double longitude = cursor.getDouble(7);
            String imgPath = cursor.getString(8);

            ScheduleData scheduleData = new ScheduleData(id, title, startTime, endTime, memo, address, latitude, longitude, imgPath);
            dataList.add(scheduleData);
        }
        return dataList;
    }

    public int getScheduleCount(long time){
        ArrayList<ScheduleData> dataList = new ArrayList<ScheduleData>();
        SQLiteDatabase db = getReadableDatabase();

        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        //time <= 검색범위 < calendar.getTimeinMillis()
        Cursor cursor = db.rawQuery("SELECT scheduleId FROM PersonalScheduleTBL WHERE endTime >= ? AND startTime < ?", new String[]{Long.toString(time), Long.toString(calendar.getTimeInMillis())});
        return cursor.getCount();
    }
}
