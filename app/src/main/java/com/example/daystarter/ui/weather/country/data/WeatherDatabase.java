package com.example.daystarter.ui.weather.country.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.daystarter.ui.weather.WeatherData;
import com.example.daystarter.ui.weather.country.WeatherAreaData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {WeatherData.class}, version = 2, exportSchema = false)
public abstract class WeatherDatabase extends RoomDatabase {
    public abstract WeatherDao weatherDao();
    private static volatile WeatherDatabase weatherDatabase;
    private static final int NUMBER_OF_THREADS = 5;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static WeatherDatabase getDatabase(final Context context) {
        if (weatherDatabase == null) {
            synchronized (WeatherDatabase.class) {
                if (weatherDatabase == null) {
                    weatherDatabase = Room.databaseBuilder(
                            context.getApplicationContext(),
                            WeatherDatabase.class,
                            "weather_database"
                    ).build();
                }
            }
        }
        return weatherDatabase;
    }
}
