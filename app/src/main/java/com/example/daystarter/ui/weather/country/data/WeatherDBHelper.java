package com.example.daystarter.ui.weather.country.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WeatherDBHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "weather.db";
    private static final String TAG = "WeatherDBHelper";

    public WeatherDBHelper(Context context) {
        super(context,DATABASE_NAME,null,3);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS WeatherTBL("+
                "weatherId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "area TEXT, " +
                "description TEXT , " +
                "weathertemp TEXT, " +
                "imgPath Text" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS WeatherTBL;");
        Log.d(TAG, "onUpgrade");
        onCreate(db);
    }

    public void deleteWeather(int scheduleId){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM WeatherTBL WHERE scheduleId = ?", new Object[]{scheduleId});
        db.close();
    }

    public void insertWeather(CountryData data){
        String area= data.getArea();
        String description = data.getDescription();
        String imgPath = data.getImgPath();
        double weatherTemp = data.getTemp();
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO WeatherTBL(area,description,weatherTemp,imgPath) VALUES(?, ?, ?, ?);",
                new Object[]{area,description,weatherTemp,imgPath});
        db.close();
    }

    public CountryData getWeather(int id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM WeatherTBL WHERE weatherId = ?", new String[]{Integer.toString(id)});
        while (cursor.moveToNext()){
            int weatherId = cursor.getInt(0);
            String area= cursor.getString(1);
            String description = cursor.getString(2);
            double weatherTemp = cursor.getDouble(3);
            String imgPath = cursor.getString(4);

            CountryData countryData = new CountryData(weatherId,area,description,weatherTemp,imgPath);
            return countryData;
        }
        return null;
    }
}
