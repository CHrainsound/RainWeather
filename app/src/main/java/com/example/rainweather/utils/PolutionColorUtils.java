package com.example.rainweather.utils;

/**
 * description ：空气污染物变色工具类
 * toDo：根据污染物的数值和类型，返回对应的颜色
 * email : 3014386984@qq.com
 * date : 2/19 19:00
 */

import android.content.Context;
import androidx.core.content.ContextCompat;

import com.example.rainweather.R;

public class PolutionColorUtils {

    public static int getColorByValue(Context context, double value, String pollutantType) {
        // 1. 根据类型获取对应的阈值数组 [L1, L2, L3]
        int[] levels = getLevelThresholds(context, pollutantType);
        if (levels == null) return ContextCompat.getColor(context, R.color.gray); // 默认灰色

        int l1 = levels[0];
        int l2 = levels[1];
        int l3 = levels[2];

        // 2. 根据数值范围返回颜色
        if (value <= l1) {
            return ContextCompat.getColor(context, R.color.aqi_good); // 优
        } else if (value <= l2) {
            return ContextCompat.getColor(context, R.color.aqi_moderate); // 良
        } else if (value <= l3) {
            return ContextCompat.getColor(context, R.color.aqi_unhealthy); // 轻度/中度污染
        } else {
            return ContextCompat.getColor(context, R.color.aqi_very_unhealthy); // 重度及以上
        }
    }

    /**
     * 私有方法：根据污染物类型获取 XML 中定义的阈值
     */
    private static int[] getLevelThresholds(Context context, String pollutantType) {
        try {
            // 读取 PM2.5 的阈值
            if ("pm25".equals(pollutantType)) {
                return new int[]{
                        context.getResources().getInteger(R.integer.pm2_5_l1),
                        context.getResources().getInteger(R.integer.pm2_5_l2),
                        context.getResources().getInteger(R.integer.pm2_5_l3)
                };
            }
            // 读取 PM10 的阈值
            else if ("pm10".equals(pollutantType)) {
                return new int[]{
                        context.getResources().getInteger(R.integer.pm10_l1),
                        context.getResources().getInteger(R.integer.pm10_l2),
                        context.getResources().getInteger(R.integer.pm10_l3)
                };
            }
            // 读取 SO2 的阈值
            else if ("so2".equals(pollutantType)) {
                return new int[]{
                        context.getResources().getInteger(R.integer.so2_l1),
                        context.getResources().getInteger(R.integer.so2_l2),
                        context.getResources().getInteger(R.integer.so2_l3)
                };
            }
            // 读取 NO2 的阈值
            else if ("no2".equals(pollutantType)) {
                return new int[]{
                        context.getResources().getInteger(R.integer.no2_l1),
                        context.getResources().getInteger(R.integer.no2_l2),
                        context.getResources().getInteger(R.integer.no2_l3)
                };
            }
            // 读取 O3 的阈值
            else if ("o3".equals(pollutantType)) {
                return new int[]{
                        context.getResources().getInteger(R.integer.o3_l1),
                        context.getResources().getInteger(R.integer.o3_l2),
                        context.getResources().getInteger(R.integer.o3_l3)
                };
            }
            // 读取 CO 的阈值
            else if ("co".equals(pollutantType)) {
                return new int[]{
                        context.getResources().getInteger(R.integer.co_l1),
                        context.getResources().getInteger(R.integer.co_l2),
                        context.getResources().getInteger(R.integer.co_l3)
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}