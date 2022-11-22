package com.example.daystarter.ui.weather.country.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.daystarter.ui.weather.WeatherData;
import com.example.daystarter.ui.weather.country.WeatherAreaData;

import java.util.List;

public class WeatherRepository {
    private WeatherDao weatherDao;
    private LiveData<List<WeatherData>> weatherLiveData;


    public WeatherRepository(Application application) {
        WeatherDatabase db = WeatherDatabase.getDatabase(application);
        weatherDao = db.weatherDao();
        weatherLiveData = weatherDao.getWeather();
    }

    public void insert(WeatherData weatherData) {
        WeatherDatabase.databaseWriteExecutor.execute(() -> {
            weatherDao.insert(weatherData);
        });
    }

    public void delete(WeatherData weatherData) {
        WeatherDatabase.databaseWriteExecutor.execute(() -> {
            weatherDao.delete(weatherData);
        });
    }

    public LiveData<List<WeatherData>> getWeatherLiveData(){return weatherLiveData;}
}