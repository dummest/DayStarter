package com.example.daystarter.myClass;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class PersonalScheduleDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "myDBHelper";
    private static final String DB_NAME = "PersonalSchedule.db";

    private String query = "";
    public PersonalScheduleDBHelper(Context context){
        super(context, DB_NAME, null, 1);
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS PersonalScheduleTBL (" +
                "schduleId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT DEFAULT '내 일정', " +
                "startTime INTEGER, " +
                "endTime INTEGER, " +
                "memo TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS notiTBL;");
        onCreate(db);
    }

    public void insertSchedule(String title, long startTime, long endTime, String memo){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO PersonalScheduleTBL(title, startTime, endTime, memo) VALUES(?, ?, ?, ?);", new Object[]{title, startTime, endTime, memo});
        db.close();
    }

    public void deleteSchedule(int scheduleId){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM PersonalScheduleTBL WHERE scheduleId = ?", new Object[]{scheduleId});
        db.close();
    }

    public ArrayList<ScheduleData> getDateSchedules(){
        ArrayList<ScheduleData> dataList = new ArrayList<ScheduleData>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            long startTime = cursor.getLong(2);
            long endTime = cursor.getLong(3);
            String memo = cursor.getString(4);
            String imgPath = cursor.getString(5);
            ScheduleData scheduleData = new ScheduleData(id, title, startTime, endTime, memo, imgPath);
            dataList.add(scheduleData);
        }
        return dataList;
    }
}
