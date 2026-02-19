package com.example.rainweather.repository.network;

/**
 * description ： TODO：发送高德api网络请求
 * email : 3014386984@qq.com
 * date : 2/19 14:00
 */

import com.example.rainweather.repository.model.MapResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MapService {
    // 逆地理编码接口路径
    @GET("v3/geocode/regeo")
    Call<MapResponse> getRegeo(
            @Query("key") String key,
            @Query("location") String location// 经度,纬度
    );
}