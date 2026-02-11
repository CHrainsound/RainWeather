package com.example.rainweather.repository.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
/**
 * description ： TODO:彩云天气 API v2.6 模型类
 * email : 3014386984@qq.com
 * date : 2/8 14:00
 */
public class WeatherResponse {

    @SerializedName("status")
    public String status; // "ok" 表示成功

    @SerializedName("api_version")
    public String apiVersion;

    @SerializedName("api_status")
    public String apiStatus; // "active" 表示服务正常

    @SerializedName("lang")
    public String lang; // 语言，如 "zh_CN"

    @SerializedName("unit")
    public String unit; // 单位制，"metric" 为公制

    @SerializedName("tzshift")
    public int tzShift; // 时区偏移（秒），+28800 = UTC+8

    @SerializedName("timezone")
    public String timezone; // 时区 ID，如 "Asia/Shanghai"

    @SerializedName("server_time")
    public long serverTime; // 服务器时间戳（秒）

    @SerializedName("location")
    public double[] location; //[经度，纬度]

    @SerializedName("result")
    public Result result;

    @SerializedName("primary")
    public int primary; // 主要天气现象编码（0=晴/少云, 3=雨, 4=雪等）

    @SerializedName("forecast_keypoint")
    public String forecastKeypoint; // 预报关键描述，如 "多云，今天夜里23点后转阴..."


    // 内部类

    /**
     * result 对象
     */
    public static class Result {
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
    public static class Realtime {
        @SerializedName("status")
        public String status;

        @SerializedName("temperature")
        public double temperature; // 当前温度（°C）

        @SerializedName("humidity")
        public double humidity; // 相对湿度（0.0 ~ 1.0）

        @SerializedName("cloudrate")
        public double cloudrate; // 云量（0.0 ~ 1.0）

        @SerializedName("skycon")
        public String skycon; // 天气图标代码，如 "CLOUDY", "CLEAR_NIGHT"

        @SerializedName("visibility")
        public double visibility; // 能见度（公里）

        @SerializedName("dswrf")
        public double dswrf; // 地表短波辐射（W/m²）

        @SerializedName("wind")
        public Wind wind;

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

    public static class Wind {
        @SerializedName("speed")
        public double speed; // 风速（m/s）

        @SerializedName("direction")
        public double direction; // 风向（角度，0=北，90=东）
    }

    public static class Precipitation {
        @SerializedName("local")
        public LocalPrecip local;

        @SerializedName("nearest")
        public NearestPrecip nearest;
    }

    public static class LocalPrecip {
        @SerializedName("status")
        public String status;

        @SerializedName("datasource")
        public String datasource; // 数据源，如 "radar"

        @SerializedName("intensity")
        public double intensity; // 降水强度（mm/h）
    }

    public static class NearestPrecip {
        @SerializedName("status")
        public String status;

        @SerializedName("distance")
        public double distance; // 最近降水距离（公里）

        @SerializedName("intensity")
        public double intensity;
    }

    public static class AirQuality {
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

    public static class Aqi {
        @SerializedName("chn")
        public int chn; // 中国 AQI

        @SerializedName("usa")
        public int usa; // 美国 AQI
    }

    public static class Description {
        @SerializedName("chn")
        public String chn; // 如 "优"

        @SerializedName("usa")
        public String usa; // 如 "良"
    }

    public static class LifeIndex {
        @SerializedName("ultraviolet")
        public Ultraviolet ultraviolet;

        @SerializedName("comfort")
        public Comfort comfort;
    }

    public static class Ultraviolet {
        @SerializedName("index")
        public int index;

        @SerializedName("desc")
        public String desc; // 如 "弱"
    }

    public static class Comfort {
        @SerializedName("index")
        public int index;

        @SerializedName("desc")
        public String desc; // 如 "冷"
    }


    /**
     * 逐小时预报
     */
    public static class Hourly {
        @SerializedName("status")
        public String status;

        @SerializedName("description")
        public String description; // 预报文字描述

        @SerializedName("precipitation")
        public List<HourlyValue> precipitation;

        @SerializedName("temperature")
        public List<HourlyValue> temperature;

        @SerializedName("apparent_temperature")
        public List<HourlyValue> apparentTemperature;

        @SerializedName("wind")
        public List<HourlyWind> wind;

        @SerializedName("humidity")
        public List<HourlyValue> humidity;

        @SerializedName("cloudrate")
        public List<HourlyValue> cloudrate;

        @SerializedName("skycon")
        public List<HourlySkycon> skycon;

        @SerializedName("pressure")
        public List<HourlyValue> pressure;

        @SerializedName("visibility")
        public List<HourlyValue> visibility;

        @SerializedName("dswrf")
        public List<HourlyValue> dswrf;

        @SerializedName("air_quality")
        public HourlyAirQuality airQuality;
    }

    public static class HourlyValue {
        @SerializedName("datetime")
        public String datetime; // ISO8601 时间，如 "2026-02-08T15:00+08:00"

        @SerializedName("value")
        public double value;
    }

    public static class HourlyWind {
        @SerializedName("datetime")
        public String datetime;

        @SerializedName("speed")
        public double speed;

        @SerializedName("direction")
        public double direction;
    }

    public static class HourlySkycon {
        @SerializedName("datetime")
        public String datetime;

        @SerializedName("value")
        public String value; // 如 "CLOUDY"
    }

    public static class HourlyAirQuality {
        @SerializedName("aqi")
        public List<HourlyAqi> aqi;

        @SerializedName("pm25")
        public List<HourlyPm25> pm25;
    }

    public static class HourlyAqi {
        @SerializedName("datetime")
        public String datetime;

        @SerializedName("value")
        public Aqi value;
    }

    public static class HourlyPm25 {
        @SerializedName("datetime")
        public String datetime;

        @SerializedName("value")
        public int value;
    }


    /**
     * 逐日预报
     */
    public static class Daily {
        @SerializedName("status")
        public String status;

        @SerializedName("astro")
        public List<DailyAstro> astro; // 日出日落

        @SerializedName("precipitation_08h_20h")
        public List<DailyPrecip> precipitation08h20h;

        @SerializedName("precipitation_20h_32h")
        public List<DailyPrecip> precipitation20h32h;

        @SerializedName("precipitation")
        public List<DailyPrecip> precipitation;

        @SerializedName("temperature")
        public List<DailyTemp> temperature;

        @SerializedName("temperature_08h_20h")
        public List<DailyTemp> temperature08h20h;

        @SerializedName("temperature_20h_32h")
        public List<DailyTemp> temperature20h32h;

        @SerializedName("wind")
        public List<DailyWind> wind;

        @SerializedName("wind_08h_20h")
        public List<DailyWind> wind08h20h;

        @SerializedName("wind_20h_32h")
        public List<DailyWind> wind20h32h;

        @SerializedName("humidity")
        public List<DailyStat> humidity;

        @SerializedName("cloudrate")
        public List<DailyStat> cloudrate;

        @SerializedName("pressure")
        public List<DailyStat> pressure;

        @SerializedName("visibility")
        public List<DailyStat> visibility;

        @SerializedName("dswrf")
        public List<DailyStat> dswrf;

        @SerializedName("air_quality")
        public DailyAirQuality airQuality;

        @SerializedName("skycon")
        public List<DailySkycon> skycon;

        @SerializedName("skycon_08h_20h")
        public List<DailySkycon> skycon08h20h;

        @SerializedName("skycon_20h_32h")
        public List<DailySkycon> skycon20h32h;

        @SerializedName("life_index")
        public DailyLifeIndex lifeIndex;
    }

    public static class DailyAstro {
        @SerializedName("date")
        public String date;

        @SerializedName("sunrise")
        public SunEvent sunrise;

        @SerializedName("sunset")
        public SunEvent sunset;
    }

    public static class SunEvent {
        @SerializedName("time")
        public String time; // 如 "07:37"
    }

    public static class DailyPrecip {
        @SerializedName("date")
        public String date;

        @SerializedName("max")
        public double max;

        @SerializedName("min")
        public double min;

        @SerializedName("avg")
        public double avg;

        @SerializedName("probability")
        public int probability; // 降水概率（%）
    }

    public static class DailyTemp {
        @SerializedName("date")
        public String date;

        @SerializedName("max")
        public double max;

        @SerializedName("min")
        public double min;

        @SerializedName("avg")
        public double avg;
    }

    public static class DailyWind {
        @SerializedName("date")
        public String date;

        @SerializedName("max")
        public WindStat max;

        @SerializedName("min")
        public WindStat min;

        @SerializedName("avg")
        public WindStat avg;
    }

    public static class WindStat {
        @SerializedName("speed")
        public double speed;

        @SerializedName("direction")
        public double direction;
    }

    public static class DailyStat {
        @SerializedName("date")
        public String date;

        @SerializedName("max")
        public double max;

        @SerializedName("min")
        public double min;

        @SerializedName("avg")
        public double avg;
    }

    public static class DailySkycon {
        @SerializedName("date")
        public String date;

        @SerializedName("value")
        public String value;
    }

    public static class DailyAirQuality {
        @SerializedName("aqi")
        public List<DailyAqi> aqi;

        @SerializedName("pm25")
        public List<DailyPm25> pm25;
    }

    public static class DailyAqi {
        @SerializedName("date")
        public String date;

        @SerializedName("max")
        public Aqi max;

        @SerializedName("avg")
        public Aqi avg;

        @SerializedName("min")
        public Aqi min;
    }

    public static class DailyPm25 {
        @SerializedName("date")
        public String date;

        @SerializedName("max")
        public int max;

        @SerializedName("avg")
        public int avg;

        @SerializedName("min")
        public int min;
    }

    public static class DailyLifeIndex {
        @SerializedName("ultraviolet")
        public List<DailyUv> ultraviolet;

        @SerializedName("carWashing")
        public List<DailyIndex> carWashing;

        @SerializedName("dressing")
        public List<DailyIndex> dressing;

        @SerializedName("comfort")
        public List<DailyIndex> comfort;

        @SerializedName("coldRisk")
        public List<DailyIndex> coldRisk;
    }

    public static class DailyUv {
        @SerializedName("date")
        public String date;

        @SerializedName("index")
        public String index; // 如 "1"

        @SerializedName("desc")
        public String desc; // 如 "最弱"
    }

    public static class DailyIndex {
        @SerializedName("date")
        public String date;

        @SerializedName("index")
        public String index;

        @SerializedName("desc")
        public String desc;
    }
}