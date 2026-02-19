package com.example.rainweather.repository.model;

import java.util.List;

/**
 * description ：高德逆地理编码模型类
 * email : 3014386984@qq.com
 * date : 2/19 14:00
 */
public class MapResponse {

    public String status;
    public String info;
    public String infocode;
    public Regeocode regeocode;

    public static class Regeocode {
        public AddressComponent addressComponent;
        public String formatted_address; // 格式化后的完整地址
    }

    public static class AddressComponent {
        // 注意：JSON中businessAreas是一个包含数组的数组，这里用List<List<String>>接收
        public List<List<String>> businessAreas;
        public String address; // 门牌地址
        public String city;    // 城市名
        public String province; // 省份名
        public String district; // 区/县名
        public String township; // 乡镇/街道
        public String adcode;   // 行政区划代码 (非常重要，用于精准定位)
        public String citycode; // 城市编码 (电话区号类)
    }


}