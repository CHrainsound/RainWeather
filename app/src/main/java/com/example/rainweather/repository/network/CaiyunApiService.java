package com.example.rainweather.repository.network;
import com.example.rainweather.repository.model.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CaiyunApiService {
    @GET("v2.6/{token}/{location}/weather")
    Call<WeatherResponse> getWeather(
            @Path("token") String token,
            @Path("location") String location, // 格式: 经度,纬度
            @Query("hourlysteps") int hourlySteps,
            @Query("dailysteps") int dailySteps,
            @Query("alert") boolean alert
    );
}
