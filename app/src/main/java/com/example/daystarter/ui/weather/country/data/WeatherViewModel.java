package com.example.daystarter.ui.weather.country.data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.daystarter.ui.alarm.data.Alarm;
import com.example.daystarter.ui.weather.country.WeatherAreaData;

import java.util.List;

public class WeatherViewModel extends AndroidViewModel {
    private WeatherRepository weatherRepository;
    private LiveData<List<WeatherAreaData>> weatherLiveData;

    public WeatherViewModel(@NonNull Application application) {
        super(application);

        weatherRepository = new WeatherRepository(application);
        weatherLiveData = weatherRepository.getWeatherLiveData();
    }

    public void insert(WeatherAreaData weatherAreaData) {
        weatherRepository.insert(weatherAreaData);
    }

    public void delete(WeatherAreaData weatherAreaData) {
        weatherRepository.delete(weatherAreaData);
    }

    public LiveData<List<WeatherAreaData>> getWeatherLiveData() {
        return weatherLiveData;
    }
}