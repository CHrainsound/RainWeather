package com.example.rainweather.view.activity;

/**
 * description ： TODO：负责污染详情页ui加载
 * email : 3014386984@qq.com
 * date : 2/19 17:00
 */

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.rainweather.R;
import com.example.rainweather.repository.model.WeatherResponse;
import com.example.rainweather.utils.PolutionColorUtils;

public class AirQualityActivity extends AppCompatActivity {


    // UI
    private TextView tvAqiValue, tvAqiLevel, tvPm25, tvPm10, tvSo2, tvNo2, tvO3, tvCo;
    private ImageButton btnReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airquality);

        // 初始化视图和数据
        initViews();
        Intent intent = getIntent();

        // 注意：这里接收的是你刚才 put 进去的 AirQuality 对象
        WeatherResponse.AirQuality airQuality = (WeatherResponse.AirQuality) intent.getSerializableExtra("air_quality_data");

        if (airQuality != null) {
            // 2. 更新 UI
            updateAqiUI(airQuality);
        } else {
            Toast.makeText(this, "获取数据失败", Toast.LENGTH_SHORT).show();
            finish(); // 结束当前 Activity
        }
    }

    /**
     * 绑定布局中的控件
     */
    private void initViews() {
        tvAqiValue = findViewById(R.id.tv_aqi);
        tvAqiLevel = findViewById(R.id.tv_aqi_level);
        tvPm25 = findViewById(R.id.tv_pm2_5);
        tvPm10 = findViewById(R.id.tv_pm10);
        tvSo2 = findViewById(R.id.tv_so2);
        tvNo2 = findViewById(R.id.tv_no2);
        tvO3 = findViewById(R.id.tv_o3);
        tvCo = findViewById(R.id.tv_co);
        btnReturn = findViewById(R.id.btn_return_airquality);

        // 设置返回按钮点击事件
        btnReturn.setOnClickListener(v -> finish());
    }

    /**
     * 更新 AQI 相关的 UI
     * 注意：DailyAqi 包含 max, min, avg
     * 这里我们展示最大值 (max)
     */
    private void updateAqiUI(WeatherResponse.AirQuality airQuality) {
        Log.d("AirQuality", "AirQuality Object: " + airQuality.toString());
        Log.d("AirQuality", "AQI Object: " + airQuality.aqi); // 看看是不是 null
        Log.d("AirQuality", "Description Object: " + airQuality.description); // 看看是不是 null
        // 处理AQI
        if (airQuality.aqi != null) {
            tvAqiValue.setText(String.valueOf(airQuality.aqi.chn));
        } else {
            tvAqiValue.setText("N/A");
        }

        // 处理Description
        if (airQuality.description != null) {
            tvAqiLevel.setText(airQuality.description.chn);
        } else {
            tvAqiLevel.setText("未知等级");
        }
        // 处理具体的污染物数值
        tvPm25.setText(String.valueOf(airQuality.pm25));
        tvPm10.setText(String.valueOf(airQuality.pm10));
        tvSo2.setText(String.valueOf(airQuality.so2));
        tvNo2.setText(String.valueOf(airQuality.no2));
        tvO3.setText(String.valueOf(airQuality.o3));
        tvCo.setText(String.valueOf(airQuality.co));
        setColorByLevelAndValue(airQuality);
    }


    /**
     * 显示错误信息
     */
    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    /**
     * 根据等级和数值设置颜色
     */
    private void setColorByLevelAndValue(WeatherResponse.AirQuality airQuality) {
        // 1. 根据 Description (等级) 设置 AQI 颜色
        String level = airQuality.description.chn;
        int aqiColor;

        if ("优".equals(level)) {
            aqiColor = ContextCompat.getColor(this, R.color.aqi_good);
        } else if ("良".equals(level)) {
            aqiColor = ContextCompat.getColor(this, R.color.aqi_moderate);
        } else if ("轻度污染".equals(level)) {
            aqiColor = ContextCompat.getColor(this, R.color.aqi_unhealthy_for_sensitive);
        } else if ("中度污染".equals(level)) {
            aqiColor = ContextCompat.getColor(this, R.color.aqi_unhealthy);
        } else if ("重度污染".equals(level)) {
            aqiColor = ContextCompat.getColor(this, R.color.aqi_very_unhealthy);
        } else { // 严重污染 或其他
            aqiColor = ContextCompat.getColor(this, R.color.aqi_hazardous);
        }
        tvAqiValue.setTextColor(aqiColor);
        tvAqiLevel.setTextColor(aqiColor); // 等级文字用同色

        // 设置污染物的颜色
        setColorForPollutant(tvPm25, airQuality.pm25,"pm25");
        setColorForPollutant(tvPm10, airQuality.pm10,"pm10");
        setColorForPollutant(tvSo2, airQuality.so2,"so2");
        setColorForPollutant(tvNo2, airQuality.no2,"no2");
        setColorForPollutant(tvO3, airQuality.o3,"o3");
        setColorForPollutant(tvCo, airQuality.co,"co");
    }

    //调用工具类改色
    private void setColorForPollutant(TextView tv, double value, String type) {
        int color = PolutionColorUtils.getColorByValue(this, value, type);
        tv.setTextColor(color);
    }
}