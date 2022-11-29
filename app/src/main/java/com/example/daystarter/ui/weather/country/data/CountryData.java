package com.example.daystarter.ui.weather.country.data;

public class CountryData {
    private int weatherId;
    private String description;
    private String area;
    private String imgPath;
    private double temp;

    public CountryData(){}

    public CountryData(int weatherId,String area, String description,double temp,String imgPath){
        this.weatherId= weatherId;
        this.area= area;
        this.description= description;
        this.temp=temp;
        this.imgPath=imgPath;
    }

    public int getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(int weatherId) {
        this.weatherId = weatherId;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }
}
