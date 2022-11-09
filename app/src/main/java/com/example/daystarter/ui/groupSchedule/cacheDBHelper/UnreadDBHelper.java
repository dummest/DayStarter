package com.example.daystarter.ui.groupSchedule.cacheDBHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class UnreadDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "UnreadDBHelper";

    public UnreadDBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS unreadTable(" +
                "groupId TEXT PRIMARY KEY, " +
                "scheduleUnreadCount INTEGER DEFAULT 0, " +
                "chatUnreadCount INTEGER DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS unreadTable;");
        onCreate(sqLiteDatabase);
    }

    public void increaseScheduleCounter(String groupId){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("UPDATE unreadTable SET scheduleUnreadCount = scheduleUnreadCount + 1 WHERE groupId = ?", new Object[]{groupId});

    }

    public void increaseChatCounter(String groupId){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("UPDATE unreadTable SET chatUnreadCount = chatUnreadCount + 1 WHERE groupId = ?", new Object[]{groupId});
    }

    public void scheduleCounterReset(String groupId){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("UPDATE unreadTable SET scheduleUnreadCount = 0 WHERE groupId = ?", new Object[]{groupId});
    }

    public void chatCounterReset(String groupId){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("UPDATE unreadTable SET chatUnreadCount = 0 WHERE groupId = ?", new Object[]{groupId});
    }

    public boolean searchGroup(String groupId){
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM unreadTable WHERE groupId = ?", new String[]{groupId});
        while (cursor.moveToNext()){
            return true;
        }
        return false;
    }

    public void insertGroup(String groupId){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("INSERT INTO unreadTable(groupId, scheduleUnreadCount, chatUnreadCount) VALUES(?, ?, ?)", new Object[]{groupId, 0, 0});
    }

    public void deleteGroup(String groupId){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM unreadTable WHERE groupId = ?", new Object[]{groupId});
    }

    public int getUnreadScheduleCount(String groupId){
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT scheduleUnreadCount FROM unreadTable WHERE groupId = ?", new String[]{groupId});
        cursor.moveToNext();
        return cursor.getInt(0);
    }

    public int getUnreadChatCount(String groupId){
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT chatUnreadCount FROM unreadTable WHERE groupId = ?", new String[]{groupId});
        cursor.moveToNext();
        return cursor.getInt(0);
    }
}
