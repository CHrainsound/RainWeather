package com.example.rainweather.repository.model;
/**
 * description ：TODO:空气质量详情页柱状图模型类
 * email : 3014386984@qq.com
 * date : 2/20 14:00
 */

public class AirQualityItem {
        private String time;
        private int aqi;

        public AirQualityItem(String time, int aqi) {
            this.time = time;
            this.aqi = aqi;
        }

        public String getTime() {
            return time;
        }

        public int getAqi() {
            return aqi;
        }
    }