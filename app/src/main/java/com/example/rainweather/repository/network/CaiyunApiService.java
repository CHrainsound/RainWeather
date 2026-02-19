package com.example.rainweather.repository.network;
import com.example.rainweather.repository.model.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
/**
 * description ： TODO：发送彩云天气网络请求
 * email : 3014386984@qq.com
 * date : 2/8 14:00
 */
public interface CaiyunApiService {
    @GET("v2.6/{token}/{location}/weather")
    Call<WeatherResponse> getWeather(
            @Path(value = "token",encoded = true) String token,
            @Path(value = "location",encoded = true) String location, // 格式: 经度,纬度
            @Query("hourlysteps") int hourlySteps,
            @Query("dailysteps") int dailySteps,
            @Query("alert") boolean alert
    );
}
