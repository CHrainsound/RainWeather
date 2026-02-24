package com.example.rainweather.repository.model;
/**
 * description ：TODO:24h气温图模型类，数据格式化
 * email : 3014386984@qq.com
 * date : 2/21 12:00
 */

import static com.example.rainweather.utils.DateUtils.parseISO8601ToMillis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 用于 HourlyTemperatureChart 的简化数据模型
 */
public class HourlyChartData {
    public String displayTime;
    public long timestamp;
    public float temperature;
    public String skycon;
    public String windSpeed;

    public boolean isNow;
    public boolean isSunEvent = false;
    public String sunEventType; // "sunrise" or "sunset"
    public String sunEventTime; // 真实日出/日落时间

    public HourlyChartData(long timestamp, float temperature, String skycon, String windSpeed) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.skycon = skycon;
        this.windSpeed = windSpeed;
        this.isNow = false;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        this.displayTime = sdf.format(new java.util.Date(timestamp));
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


    //主方法
    public static List<HourlyChartData> fromWeatherResponse(WeatherResponse response) {
        List<HourlyChartData> list = new ArrayList<>();
        if (response == null || response.result == null || response.result.hourly == null) {
            return list;
        }

        // 添加常规小时数据
        List<WeatherResponse.HourlyValue> temps = response.result.hourly.temperature;
        List<WeatherResponse.HourlySkycon> skycons = response.result.hourly.skycon;
        List<WeatherResponse.HourlyWind> winds = response.result.hourly.wind;

        int size = Math.min(Math.min(temps.size(), skycons.size()), winds.size());
        size = Math.min(size, 24);

        for (int i = 0; i < size; i++) {
            String datetime = temps.get(i).datetime;
            long timestamp = parseISO8601ToMillis(datetime);
            float temp = (float) temps.get(i).value;
            String skycon = skycons.get(i).value;
            String windSpeed = formatWindSpeed((float) winds.get(i).speed);

            list.add(new HourlyChartData(timestamp, temp, skycon, windSpeed));
        }

        if (!list.isEmpty()) {
            list.get(0).isNow = true;
        }

        //处理日出/日落（也转为时间戳）
        WeatherResponse.DailyAstro todayAstro = response.getTodayAstro();
        if (todayAstro != null) {
            long now = System.currentTimeMillis();
            java.util.TimeZone tz = java.util.TimeZone.getDefault();

            // 日出
            if (todayAstro.sunrise != null && todayAstro.sunrise.time != null) {
                long sunriseTimestamp = parseSunEventToTimestamp(todayAstro.sunrise.time, now, tz);
                if (sunriseTimestamp > 0) {
                    // 找最接近的时间点温度（按 timestamp）
                    float temp = findNearestTemperatureByTimestamp(list, sunriseTimestamp);
                    HourlyChartData sunrise = new HourlyChartData(sunriseTimestamp, temp, "", "");
                    sunrise.isSunEvent = true;
                    sunrise.sunEventType = "sunrise";
                    sunrise.sunEventTime = todayAstro.sunrise.time;
                    list.add(sunrise);
                }
            }

            // 日落
            if (todayAstro.sunset != null && todayAstro.sunset.time != null) {
                long sunsetTimestamp = parseSunEventToTimestamp(todayAstro.sunset.time, now, tz);
                if (sunsetTimestamp > 0) {
                    float temp = findNearestTemperatureByTimestamp(list, sunsetTimestamp);
                    HourlyChartData sunset = new HourlyChartData(sunsetTimestamp, temp, "", "");
                    sunset.isSunEvent = true;
                    sunset.sunEventType = "sunset";
                    sunset.sunEventTime = todayAstro.sunset.time;
                    list.add(sunset);
                }
            }
        }

        // 按时间排序（确保 sunrise/sunset 插入到正确位置）
        Collections.sort(list, (a, b) -> Long.compare(a.timestamp, b.timestamp));

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

    // 将 "06:48" 转为今天或明天的时间戳
    private static long parseSunEventToTimestamp(String sunTime, long now, java.util.TimeZone tz) {
        try {
            String[] parts = sunTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            java.util.Calendar cal = java.util.Calendar.getInstance(tz);
            cal.setTimeInMillis(now);
            cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
            cal.set(java.util.Calendar.MINUTE, minute);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);

            long candidate = cal.getTimeInMillis();
            // 如果日出时间已过（比如现在 10:00，日出 06:48），则加一天
            if (candidate < now) {
                candidate += 24 * 60 * 60 * 1000L;
            }
            return candidate;
        } catch (Exception e) {
            return -1;
        }
    }

    private static float findNearestTemperatureByTimestamp(List<HourlyChartData> list, long targetTs) {
        if (list.isEmpty()) return 0f;
        HourlyChartData closest = list.get(0);
        long minDiff = Math.abs(closest.timestamp - targetTs);
        for (HourlyChartData item : list) {
            long diff = Math.abs(item.timestamp - targetTs);
            if (diff < minDiff) {
                minDiff = diff;
                closest = item;
            }
        }
        return closest.temperature;
    }

}