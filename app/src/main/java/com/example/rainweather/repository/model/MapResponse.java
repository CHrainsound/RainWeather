package com.example.rainweather.repository.model;


/**
 * description ：高德逆地理编码模型类
 * email : 3014386984@qq.com
 * date : 2/19 14:00
 */
public class MapResponse {

    public String status;
    public String info;

    public Regeocode regeocode;

    public static class Regeocode {
        public AddressComponent addressComponent;

    }

    public static class AddressComponent {
        public String city;    // 城市名
        public String province; // 省份名
        public String district; // 区/县名
        public String township; // 乡镇/街道
        public String adcode;   // 行政区划代码

    }


}