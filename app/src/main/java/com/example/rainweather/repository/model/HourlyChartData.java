package com.example.rainweather.repository.model;
/**
 * description ：TODO:24h气温图模型类，数据格式化
 * email : 3014386984@qq.com
 * date : 2/21 12:00
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 用于 HourlyTemperatureChart 的简化数据模型
 */
public class HourlyChartData {
    public String time; // 格式: "14:00"（用于 X 轴定位）
    public float temperature;
    public String skycon; // 如 "CLEAR_DAY"
    public String windSpeed; // 格式: "3级"

    public boolean isNow; // 是否是当前时间点
    public boolean isSunEvent = false;
    public String sunEventType; // "sunrise" or "sunset"
    public String sunEventTime; // 真实日出/日落时间，如 "06:48"

    public HourlyChartData(String time, float temperature, String skycon, String windSpeed) {
        this.time = time;
        this.temperature = temperature;
        this.skycon = skycon;
        this.windSpeed = windSpeed;
        this.isNow = false;
    }

    // === 静态工具方法 ===
    private static String convertWindSpeedToLevel(float windSpeedKmh) {
        if (windSpeedKmh <= 0) return "静风";
        float windSpeedMs = windSpeedKmh / 3.6f; // km/h → m/s
        if (windSpeedMs < 0.3f) return "0级";
        else if (windSpeedMs < 1.6f) return "1级";
        else if (windSpeedMs < 3.4f) return "2级";
        else if (windSpeedMs < 5.5f) return "3级";
        else if (windSpeedMs < 8.0f) return "4级";
        else if (windSpeedMs < 10.8f) return "5级";
        else if (windSpeedMs < 13.9f) return "6级";
        else if (windSpeedMs < 17.2f) return "7级";
        else if (windSpeedMs < 20.8f) return "8级";
        else if (windSpeedMs < 24.5f) return "9级";
        else if (windSpeedMs < 28.5f) return "10级";
        else if (windSpeedMs < 32.7f) return "11级";
        else return "12级";
    }

    private static String formatWindSpeed(float speed) {
        try {
            return convertWindSpeedToLevel(speed);
        } catch (Exception e) {
            return "微风";
        }
    }

    /**
     * 将日出/日落时间（如 "06:48"）转换为整点时间（如 "07:00"）用于 X 轴对齐
     */
    private static String roundToNearestHour(String sunTime) {
        if (sunTime == null || !sunTime.contains(":")) return "06:00";
        String[] parts = sunTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        if (minute >= 30) {
            hour = (hour + 1) % 24;
        }
        return String.format("%02d:00", hour);
    }

    private static String extractTimeFromISO8601(String iso8601) {
        int tIndex = iso8601.indexOf('T');
        int plusIndex = iso8601.indexOf('+', tIndex);
        if (tIndex != -1 && plusIndex != -1) {
            return iso8601.substring(tIndex + 1, plusIndex);
        }
        return "00:00";
    }

    // === 主方法 ===
    public static List<HourlyChartData> fromWeatherResponse(WeatherResponse response) {
        List<HourlyChartData> list = new ArrayList<>();
        if (response == null || response.result == null || response.result.hourly == null) {
            return list;
        }

        // 1. 添加常规小时数据
        List<WeatherResponse.HourlyValue> temps = response.result.hourly.temperature;
        List<WeatherResponse.HourlySkycon> skycons = response.result.hourly.skycon;
        List<WeatherResponse.HourlyWind> winds = response.result.hourly.wind;

        int size = Math.min(Math.min(temps.size(), skycons.size()), winds.size());
        size = Math.min(size, 24);

        for (int i = 0; i < size; i++) {
            String datetime = temps.get(i).datetime;
            String time = extractTimeFromISO8601(datetime);
            float temp = (float) temps.get(i).value;
            String skycon = skycons.get(i).value;
            String windSpeed = formatWindSpeed((float) winds.get(i).speed);

            list.add(new HourlyChartData(time, temp, skycon, windSpeed));
        }

        if (!list.isEmpty()) {
            list.get(0).isNow = true;
        }

        // 2. 注入日出/日落事件（关键修复：插入到正确位置）
        WeatherResponse.DailyAstro todayAstro = response.getTodayAstro();
        if (todayAstro != null) {
            // 日出
            if (todayAstro.sunrise != null && todayAstro.sunrise.time != null) {
                String realTime = todayAstro.sunrise.time;
                String chartTime = roundToNearestHour(realTime); // 对齐到整点
                HourlyChartData sunrise = new HourlyChartData(chartTime, 0, "", "");
                sunrise.isSunEvent = true;
                sunrise.sunEventType = "sunrise";
                sunrise.sunEventTime = realTime;
                list.add(sunrise);
            }
            // 日落
            if (todayAstro.sunset != null && todayAstro.sunset.time != null) {
                String realTime = todayAstro.sunset.time;
                String chartTime = roundToNearestHour(realTime);
                HourlyChartData sunset = new HourlyChartData(chartTime, 0, "", "");
                sunset.isSunEvent = true;
                sunset.sunEventType = "sunset";
                sunset.sunEventTime = realTime;
                list.add(sunset);
            }
        }

        // 3. 按时间排序（确保 sunrise/sunset 插入到正确位置）
        Collections.sort(list, new Comparator<HourlyChartData>() {
            @Override
            public int compare(HourlyChartData a, HourlyChartData b) {
                String timeA = a.isSunEvent ? a.time : a.time;
                String timeB = b.isSunEvent ? b.time : b.time;
                return timeToMinutes(timeA) - timeToMinutes(timeB);
            }
        });

        return list;
    }

    // 辅助方法：HH:mm → 分钟数（用于排序）
    private static int timeToMinutes(String timeStr) {
        if (timeStr == null || !timeStr.contains(":")) return 0;
        String[] parts = timeStr.split(":");
        try {
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }
}