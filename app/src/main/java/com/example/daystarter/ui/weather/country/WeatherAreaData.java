package com.example.daystarter.ui.weather.country;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

public class WeatherAreaData {

    int weatherId;
    String area;
    String areas;
    double lat;
    double lng;

    public WeatherAreaData(int weatherId, String area, String areas,double lat, double lng) {
        this.weatherId = weatherId;
        this.area = area;
        this.areas= areas;
        this.lat =lat;
        this.lng = lng;
    }


    public String getArea() {
        return area;
    }
    public double getLat() {
        return lat;
    }
    public void setLat(double lat) { this.lat = lat;}
    public double getLng() {  return lng; }
    public void setLng(double lng) {this.lng = lng; }
    public void setArea(String area) {this.area = area; }
    public void setAreas(String areas) {
        this.areas = areas;
    }
    public String getAreas() {
        return areas;
    }

    public int getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(int weatherId) {
        this.weatherId = weatherId;
    }
}