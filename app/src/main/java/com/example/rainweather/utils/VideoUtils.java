package com.example.rainweather.utils;


/**
 * description ：视频匹配工具类
 * toDo：根据天气状况标识符和时间匹配并返回对应的背景视频资源ID
 * email : 3014386984@qq.com
 * date : 2/19 18:00
 */
import com.example.rainweather.R;

public class VideoUtils {
    public static int getVideoResourceForSkycon(String skycon, boolean isNight) {
        if (isNight) {
            switch (skycon) {
                case "CLEAR_DAY":
                case "CLEAR_NIGHT":
                    return R.raw.weather_clear;
                case "PARTLY_CLOUDY_DAY":
                case "PARTLY_CLOUDY_NIGHT":
                case "CLOUDY":
                    return R.raw.weather_cloudy_night;
                case "LIGHT_RAIN":
                case "MODERATE_RAIN":
                case "HEAVY_RAIN":
                    return R.raw.weather_rain_night;
                case "STORM_RAIN":
                    return R.raw.weather_thunderstorm_night;
                case "LIGHT_SNOW":
                case "MODERATE_SNOW":
                case "HEAVY_SNOW":
                case "STORM_SNOW":
                    return R.raw.weather_snow_night;
                case "FOG":
                case "LIGHT_HAZE":
                case "MODERATE_HAZE":
                case "HEAVY_HAZE":
                    return R.raw.weather_fog_night;
                case "WIND":
                    return R.raw.weather_windy_night;
                default:
                    return R.raw.weather_clear;
            }
        } else {
            switch (skycon) {
                case "CLEAR_DAY":
                    return R.raw.weather_sunny;
                case "CLEAR_NIGHT":
                    return R.raw.weather_clear;
                case "LIGHT_RAIN":
                case "MODERATE_RAIN":
                case "HEAVY_RAIN":
                    return R.raw.weather_rain_day;
                case "STORM_RAIN":
                    return R.raw.weather_thunderstorm_day;
                case "LIGHT_SNOW":
                case "MODERATE_SNOW":
                case "HEAVY_SNOW":
                case "STORM_SNOW":
                    return R.raw.weather_snow_day;
                case "CLOUDY":
                case "PARTLY_CLOUDY_DAY":
                case "PARTLY_CLOUDY_NIGHT":
                    return R.raw.weather_cloudy_day;
                case "FOG":
                case "LIGHT_HAZE":
                case "MODERATE_HAZE":
                case "HEAVY_HAZE":
                    return R.raw.weather_fog_day;
                case "WIND":
                    return R.raw.weather_windy_day;
                default:
                    return R.raw.weather_sunny;
            }
        }
    }
}