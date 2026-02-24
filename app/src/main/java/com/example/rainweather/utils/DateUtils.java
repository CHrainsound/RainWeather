package com.example.rainweather.utils;

/**
 * description ：日期与时间工具类
 * toDo：日期格式化，夜晚判断
 * email : 3014386984@qq.com
 * date : 2/11 12:00
 */

import android.text.TextUtils;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    /**
     *将iso0861转化为时间戳
     * @param iso8601
     * @return
     */
    public static long parseISO8601ToMillis(String iso8601) {
        try {
            // ISO8601 示例: "2025-02-21T14:00+08:00"
            java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(iso8601);
            return odt.toInstant().toEpochMilli();
        } catch (Exception e) {
            // 兼容旧 Android（< API 26）
            try {
                // 手动处理 "+08:00" → "+0800"
                String fixed = iso8601.replaceAll(":(\\d{2})$", "$1");
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", java.util.Locale.getDefault());
                return sdf.parse(fixed).getTime();
            } catch (Exception ex) {
                return System.currentTimeMillis(); // fallback
            }
        }
    }

    /**
     * 将iso0861转化为月/日
     * @param isoDateTime
     * @return
     */
    public static String formatDatetomonthday(String isoDateTime){
        if (TextUtils.isEmpty(isoDateTime)) return "";

        try {
            // 1. 解析原始 ISO 时间字符串
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmXXX");
            var zonedDateTime = java.time.ZonedDateTime.parse(isoDateTime, formatter);
            ZoneId beijing = ZoneId.of("Asia/Shanghai");
            LocalDate targetDate = zonedDateTime.withZoneSameInstant(beijing).toLocalDate();
            DateTimeFormatter monthDayFormatter = DateTimeFormatter.ofPattern("M/d");
            String monthDayStr = targetDate.format(monthDayFormatter);
            return monthDayStr; // 示例2/20
        } catch (Exception e) {
            return isoDateTime.substring(0, 10).replace("-", "/");
        }
    }

    /**
     * 将iso0861转化为对应的今天、明天和星期
     * @param isoDateTime
     * @return
     */
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

    /**
     * 英文转化为中文
     * @param englishDay
     * @return
     */
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

    /**
     * iso0861转化为 时:分钟
     * @param isoTime
     * @return
     */
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