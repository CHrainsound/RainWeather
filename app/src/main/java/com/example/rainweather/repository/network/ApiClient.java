package com.example.rainweather.repository.network;

/**
 * description ：该类负责创建和管理与彩云天气交互的 Retrofit实例
 * TODO：用于执行天气数据的网络请求
 * email: 3014386984@qq.com
 * date:2/9 15:00
 */

import com.example.rainweather.utils.ApiConstants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    // 声明为接口类型
    private static CaiyunApiService apiService;

    public static CaiyunApiService getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConstants.BASE_URL_CaiYun)
                    .addConverterFactory(GsonConverterFactory.create()) // 需要添加 Gson 依赖
                    .build();
        }


        // 如果 apiService 已经创建过，直接返回；否则创建新的
        if (apiService == null) {
            apiService = retrofit.create(CaiyunApiService.class);
        }
        return apiService;
    }
}