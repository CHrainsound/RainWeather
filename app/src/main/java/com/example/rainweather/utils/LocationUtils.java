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

                    // 核心逻辑：高德API成功状态码是字符串 "1"
                    if ("1".equals(mapResponse.status) && mapResponse.regeocode != null) {
                        MapResponse.AddressComponent component = mapResponse.regeocode.addressComponent;

                        // 提取行政区划代码Adcode
                        String adcode = component.adcode;

                        // 提取城市名
                        String province = component.province;
                        String city = component.city;
                        String district = component.district;

                        // 高德对于直辖市（如上海），city字段可能为空或者和province一样，此时用 province 作为 city 显示
                        String finalCity;
                        if (city == null || city.isEmpty() || "[]".equals(city)) {
                            finalCity = province;
                        } else {
                            finalCity = city;
                        }

                        // 回调结果
                        // 这里返回了 城市名、区县名、乡镇街道名
                        listener.onSuccess(finalCity, district, component.township, adcode);
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

    // 修正后的回调接口
    public interface OnCityNameListener {
        // 参数：城市名, 区/县, 镇/街道, 行政区划代码
        void onSuccess(String cityName, String district, String town, String adcode);

        void onError(String errorMsg);
    }

}