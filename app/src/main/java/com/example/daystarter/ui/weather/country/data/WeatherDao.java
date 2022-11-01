package com.example.daystarter.ui.weather.country.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.daystarter.ui.weather.country.WeatherAreaData;

import java.util.List;

@Dao
public interface WeatherDao {
    @Insert
    void insert(WeatherAreaData weatherAreaData);

    @Query("DELETE FROM weather_table")
    void deleteAll();

    @Query("SELECT * FROM weather_table ORDER BY weatherId ASC")
    LiveData<List<WeatherAreaData>> getWeather();

    @Update
    void update(WeatherAreaData weatherAreaData);

    @Delete
    void delete(WeatherAreaData weatherAreaData);
}
