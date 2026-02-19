package com.example.rainweather.repository.network;

import com.example.rainweather.utils.ApiConstants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    // 1. 声明为接口类型，而不是 Retrofit 类型
    private static CaiyunApiService apiService;

    public static CaiyunApiService getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConstants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // 需要添加 Gson 依赖
                    .build();
        }

        // 2. 使用 create 方法生成接口实例
        // 如果 apiService 已经创建过，直接返回；否则创建新的
        if (apiService == null) {
            apiService = retrofit.create(CaiyunApiService.class);
        }
        return apiService;
    }
}