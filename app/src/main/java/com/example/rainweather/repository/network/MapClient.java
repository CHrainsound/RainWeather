package com.example.rainweather.repository.network;

/**
 * description ：该类负责创建和管理与高德地图服务交互的 Retrofit实例
 * TODO：用于执行逆地理编码（经纬度转城市名）等网络请求。
 * email: 3014386984@qq.com
 * date:2/19 15:00
 */

import com.example.rainweather.utils.ApiConstants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapClient {


    private static Retrofit retrofit = null;
    private static MapService mapService;

    public static MapService getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConstants.BASE_URL_MAP)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        if (mapService == null) {
            mapService = retrofit.create(MapService.class);
        }
        return mapService;
    }
}