package com.example.daystarter.ui.weather.country.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.daystarter.ui.weather.WeatherData;
import com.example.daystarter.ui.weather.country.WeatherAreaData;

import java.util.List;

@Dao
public interface WeatherDao {
    @Insert
    void insert(WeatherData weatherData);

    @Query("DELETE FROM weather_table")
    void deleteAll();

    @Query("SELECT * FROM weather_table ")
    LiveData<List<WeatherData>> getWeather();

    @Update
    void update(WeatherData weatherAreaData);

    @Delete
    void delete(WeatherData weatherAreaData);
}
