package com.example.rainweather.utils;

/**
 * description ：逆地理编码工具类
 * toDo：将经纬度高德化为地址
 * email : 3014386984@qq.com
 * date : 2/11 12:00
 */

import com.example.rainweather.repository.model.MapResponse;
import com.example.rainweather.repository.network.MapService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationUtils {

    public static void getCityName(double latitude, double longitude, OnCityNameListener listener) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL_MAP)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MapService service = retrofit.create(MapService.class);

        // 高德坐标顺序：经度,纬度
        String location = longitude + "," + latitude;

        Call<MapResponse> call = service.getRegeo(ApiConstants.API_KEY_MAP, location);
        call.enqueue(new Callback<MapResponse>() {
            @Override
            public void onResponse(Call<MapResponse> call, Response<MapResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MapResponse mapResponse = response.body();

                    // 高德API成功状态码是1
                    if ("1".equals(mapResponse.status) && mapResponse.regeocode != null) {
                        MapResponse.AddressComponent component = mapResponse.regeocode.addressComponent;

                        // 提取行政区划代码Adcode
                        String adcode = component.adcode;

                        // 提取区县
                        String district = component.district;

                        // 回调结果
                        listener.onSuccess(district, component.township, adcode);
                    } else {
                        // 高德接口返回了错误信息
                        listener.onError("高德API错误: " + mapResponse.info);
                    }
                } else {
                    listener.onError("网络请求失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MapResponse> call, Throwable t) {
                listener.onError("网络异常: " + t.getMessage());
            }
        });
    }

    public interface OnCityNameListener {
        // 参数：区/县, 镇/街道, 行政区划代码
        void onSuccess( String district, String town, String adcode);

        void onError(String errorMsg);
    }

}