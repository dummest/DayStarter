package com.example.daystarter.ui.weather.country.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.daystarter.ui.weather.country.WeatherAreaData;

import java.util.List;

public class WeatherRepository {
    private WeatherDao weatherDao;
    private LiveData<List<WeatherAreaData>> weatherLiveData;


    public WeatherRepository(Application application) {
        WeatherDatabase db = WeatherDatabase.getDatabase(application);
        weatherDao = db.weatherDao();
        weatherLiveData = weatherDao.getWeather();
    }

    public void insert(WeatherAreaData weatherAreaData) {
        WeatherDatabase.databaseWriteExecutor.execute(() -> {
            weatherDao.insert(weatherAreaData);
        });
    }

    public void delete(WeatherAreaData weatherAreaData) {
        WeatherDatabase.databaseWriteExecutor.execute(() -> {
            weatherDao.delete(weatherAreaData);
        });
    }

    public LiveData<List<WeatherAreaData>> getWeatherLiveData(){return weatherLiveData;}
}