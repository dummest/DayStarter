package com.example.daystarter.ui.weather;

public class WeatherWeekData {
    String NameDate;
    String Weather;
    double MaxTemp;
    double MinTemp;

    public String getNameDate() {
        return NameDate;
    }

    public void setNameDate(String nameDate) {
        NameDate = nameDate;
    }

    public String getWeather() {
        return Weather;
    }

    public void setWeather(String weather) {
        Weather = weather;
    }

    public double getMaxTemp(double max) {
        return MaxTemp;
    }

    public void setMaxTemp(double maxTemp) {
        MaxTemp = maxTemp;
    }

    public double getMinTemp(double min) {
        return MinTemp;
    }

    public void setMinTemp(double minTemp) {
        MinTemp = minTemp;
    }
}
