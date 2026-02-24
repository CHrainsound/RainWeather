package com.example.rainweather.repository.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * description ：彩云天气 API v2.6 模型类
 * email : 3014386984@qq.com
 * date : 2/8 14:00
 */
public class WeatherResponse implements Serializable {

    @SerializedName("status")
    public String status;


    @SerializedName("result")
    public Result result;


    // 内部类

    /**
     * result 对象
     */
    public static class Result implements Serializable {
        @SerializedName("realtime")
        public Realtime realtime;

        @SerializedName("hourly")
        public Hourly hourly;

        @SerializedName("daily")
        public Daily daily;
    }


    /**
     * 实时天气数据
     */
    public static class Realtime implements Serializable {


        @SerializedName("temperature")
        public double temperature; // 当前温度（°C）

        @SerializedName("humidity")
        public double humidity; // 相对湿度（0.0 ~ 1.0）


        @SerializedName("skycon")
        public String skycon; // 天气图标代码


        @SerializedName("pressure")
        public double pressure; // 气压（Pa）

        @SerializedName("apparent_temperature")
        public double apparentTemperature; // 体感温度（°C）

        @SerializedName("precipitation")
        public Precipitation precipitation;

        @SerializedName("air_quality")
        public AirQuality airQuality;

        @SerializedName("life_index")
        public LifeIndex lifeIndex;
    }


    public static class Precipitation implements Serializable {
        @SerializedName("local")
        public LocalPrecip local;

        @SerializedName("nearest")
        public NearestPrecip nearest;
    }

    public static class LocalPrecip implements Serializable {

        @SerializedName("intensity")
        public double intensity; // 降水强度（mm/hr）
    }

    public static class NearestPrecip implements Serializable {

        @SerializedName("distance")
        public double distance; // 最近降水距离（公里）

        @SerializedName("intensity")
        public double intensity;
    }

    public static class AirQuality implements Serializable {
        //单位（ug/m3）
        @SerializedName("pm25")
        public int pm25;

        @SerializedName("pm10")
        public int pm10;

        @SerializedName("o3")
        public int o3;

        @SerializedName("so2")
        public int so2;

        @SerializedName("no2")
        public int no2;

        @SerializedName("co")
        public double co;

        @SerializedName("aqi")
        public Aqi aqi;

        @SerializedName("description")
        public Description description;
    }

    public static class Aqi implements Serializable {
        @SerializedName("chn")
        public int chn; // 中国 AQI


    }

    public static class Description implements Serializable {
        @SerializedName("chn")
        public String chn; // 如 "优"


    }

    public static class LifeIndex implements Serializable {
        @SerializedName("ultraviolet")
        public Ultraviolet ultraviolet;

        @SerializedName("comfort")
        public Comfort comfort;
    }

    public static class Ultraviolet implements Serializable {

        @SerializedName("desc")
        public String desc; // 如 "弱"
    }

    public static class Comfort implements Serializable {

        @SerializedName("desc")
        public String desc; // 如 "冷"
    }


    /**
     * 逐小时预报
     */
    public static class Hourly implements Serializable {

        @SerializedName("description")
        public String description; // 预报文字描述


        @SerializedName("temperature")
        public List<HourlyValue> temperature;


        @SerializedName("wind")
        public List<HourlyWind> wind;


        @SerializedName("skycon")
        public List<HourlySkycon> skycon;


        @SerializedName("air_quality")
        public HourlyAirQuality airQuality;
    }

    public static class HourlyValue implements Serializable {
        @SerializedName("datetime")
        public String datetime; // ISO8601 时间，如 "2026-02-08T15:00+08:00"

        @SerializedName("value")
        public double value;
    }

    public static class HourlyWind implements Serializable {

        @SerializedName("speed")
        public double speed;


    }

    public static class HourlySkycon implements Serializable {


        @SerializedName("value")
        public String value; // 如 "CLOUDY"
    }

    public static class HourlyAirQuality implements Serializable {
        @SerializedName("aqi")
        public List<HourlyAqi> aqi;


    }

    public static class HourlyAqi implements Serializable {
        @SerializedName("datetime")
        public String datetime;

        @SerializedName("value")
        public Aqi value;
    }


    /**
     * 逐日预报
     */
    public static class Daily implements Serializable {


        @SerializedName("astro")
        public List<DailyAstro> astro; // 日出日落


        @SerializedName("temperature")
        public List<DailyTemp> temperature;


        @SerializedName("air_quality")
        public DailyAirQuality airQuality;

        @SerializedName("skycon")
        public List<DailySkycon> skycon;


        @SerializedName("life_index")
        public DailyLifeIndex lifeIndex;
    }

    public static class DailyAstro implements Serializable {
        @SerializedName("date")
        public String date;

        @SerializedName("sunrise")
        public SunEvent sunrise;

        @SerializedName("sunset")
        public SunEvent sunset;
    }

    public static class SunEvent implements Serializable {
        @SerializedName("time")
        public String time;
    }



    public static class DailyTemp implements Serializable {
        @SerializedName("date")
        public String date;

        @SerializedName("max")
        public double max;

        @SerializedName("min")
        public double min;


    }





    public static class DailySkycon implements Serializable {
        @SerializedName("date")
        public String date;

        @SerializedName("value")
        public String value;
    }

    public static class DailyAirQuality implements Serializable {
        @SerializedName("aqi")
        public List<DailyAqi> aqi;


    }

    public static class DailyAqi implements Serializable {
        @SerializedName("date")
        public String date;


        @SerializedName("avg")
        public Aqi avg;


    }



    public static class DailyLifeIndex implements Serializable {


        @SerializedName("carWashing")
        public List<DailyIndex> carWashing;


    }



    public static class DailyIndex implements Serializable {
        @SerializedName("date")
        public String date;


        @SerializedName("desc")
        public String desc;
    }

    public DailyAstro getTodayAstro() {
        if (result != null && result.daily != null && !result.daily.astro.isEmpty()) {
            return result.daily.astro.get(0);
        }
        return null;
    }


}