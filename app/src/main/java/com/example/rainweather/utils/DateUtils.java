package com.example.rainweather.utils;

/**
 * description ：日期与时间工具类
 * toDo：日期格式化，夜晚判断
 * email : 3014386984@qq.com
 * date : 2/11 12:00
 */
import android.content.Context;
import android.text.TextUtils;

import com.example.rainweather.R;
import com.example.rainweather.repository.model.DailyWeatherItem;
import com.example.rainweather.repository.model.WeatherResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DateUtils {

    public static boolean isNight(WeatherResponse weather) {
        try {
            String sunsetTimeStr = weather.result.daily.astro.get(0).sunset.time;
            LocalTime sunsetTime = LocalTime.parse(sunsetTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime now = LocalTime.now(ZoneId.of("Asia/Shanghai"));
            return now.isAfter(sunsetTime);
        } catch (Exception e) {
            return false;
        }
    }

    public static List<DailyWeatherItem> buildDailyItems(WeatherResponse weather, Context context) {
        List<DailyWeatherItem> dailyItems = new ArrayList<>();
        if (weather.result.daily != null) {
            List<WeatherResponse.DailyTemp> temps = weather.result.daily.temperature;
            List<WeatherResponse.DailySkycon> skycons = weather.result.daily.skycon;

            if (temps != null && skycons != null) {
                int count = Math.min(temps.size(), skycons.size());
                for (int i = 0; i < count; i++) {
                    String dateStr = temps.get(i).date;
                    String skyconValue = skycons.get(i).value;
                    double maxTemp = temps.get(i).max;
                    double minTemp = temps.get(i).min;

                    String displayDate = formatDateForDisplay(dateStr);
                    dailyItems.add(new DailyWeatherItem(displayDate, skyconValue, maxTemp, minTemp));
                }
            }
        }
        return dailyItems;
    }
    public static String formatDatetomonthday(String isoDateTime){
        if (TextUtils.isEmpty(isoDateTime)) return "";

        try {
            // 1. 解析原始 ISO 时间字符串
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmXXX");
            var zonedDateTime = java.time.ZonedDateTime.parse(isoDateTime, formatter);
            ZoneId beijing = ZoneId.of("Asia/Shanghai");
            LocalDate targetDate = zonedDateTime.withZoneSameInstant(beijing).toLocalDate();
//格式化为"月/日"
            DateTimeFormatter monthDayFormatter = DateTimeFormatter.ofPattern("M/d");
            String monthDayStr = targetDate.format(monthDayFormatter);

            return monthDayStr; // 示例输出：2/20
        } catch (Exception e) {
            return isoDateTime.substring(0, 10).replace("-", "/");
        }
    }


    public static String formatDateForDisplay(String isoDateTime) {
        if (TextUtils.isEmpty(isoDateTime)) return "";

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmXXX");
            var zonedDateTime = java.time.ZonedDateTime.parse(isoDateTime, formatter);

            ZoneId beijing = ZoneId.of("Asia/Shanghai");
            LocalDate targetDate = zonedDateTime.withZoneSameInstant(beijing).toLocalDate();
            LocalDate today = LocalDate.now(beijing);

            long daysDiff = ChronoUnit.DAYS.between(today, targetDate);

            switch ((int) daysDiff) {
                case 0:
                    return "今天";
                case 1:
                    return "明天";
                default:
                    return getChineseDayOfWeek(targetDate.getDayOfWeek().toString());
            }
        } catch (Exception e) {
            return isoDateTime.substring(0, 10).replace("-", "/");
        }
    }

    private static String getChineseDayOfWeek(String englishDay) {
        switch (englishDay) {
            case "MONDAY": return "周一";
            case "TUESDAY": return "周二";
            case "WEDNESDAY": return  "周三";
            case "THURSDAY": return "周四";
            case "FRIDAY": return "周五";
            case "SATURDAY": return "周六";
            case "SUNDAY": return "周日";
            default: return englishDay;
        }
    }


    public static String extractHourFromIso8601(String isoTime) {
        if (isoTime == null || isoTime.isEmpty()) return "--:--";
        try {
            // 找到 'T' 和 '+' 之间的部分
            int tIndex = isoTime.indexOf('T');
            int plusIndex = isoTime.indexOf('+', tIndex);
            if (tIndex != -1 && plusIndex != -1) {
                String timePart = isoTime.substring(tIndex + 1, plusIndex); // "15:00"
                return timePart;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "--:--";
    }
}