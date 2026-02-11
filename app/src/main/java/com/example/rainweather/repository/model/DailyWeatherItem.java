package com.example.rainweather.repository.model;
/**
 * description ： TODO:item模型类
 * email : 3014386984@qq.com
 * date : 2/11 12:00
 */
public class DailyWeatherItem {
    public String date;
    public String skycon;
    public double maxTemp;
    public double minTemp;

    public DailyWeatherItem(String date, String skycon, double max, double min) {
        this.date = date;
        this.skycon = skycon;
        this.maxTemp = max;
        this.minTemp = min;
    }
}