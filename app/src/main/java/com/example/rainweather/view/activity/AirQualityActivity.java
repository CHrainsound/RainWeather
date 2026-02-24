package com.example.rainweather.view.activity;

/**
 * description ： TODO：负责污染详情页ui加载
 * email : 3014386984@qq.com
 * date : 2/19 17:00
 */

import static com.example.rainweather.utils.DateUtils.extractHourFromIso8601;
import static com.example.rainweather.utils.DateUtils.formatDatetomonthday;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rainweather.R;
import com.example.rainweather.repository.model.AirQualityItem;
import com.example.rainweather.repository.model.WeatherResponse;
import com.example.rainweather.utils.PolutionColorUtils;
import com.example.rainweather.view.adapter.AirQualityBarAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;

public class AirQualityActivity extends AppCompatActivity {
    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton btnHourly, btnDaily;
    private int currentCkeckId = 0;



    // UI
    private TextView tvAqiValue, tvAqiLevel, tvPm25, tvPm10, tvSo2, tvNo2, tvO3, tvCo,tvhourdayTitle;
    private ImageButton btnReturn;
    private RecyclerView recyclerViewHourly;
    private AirQualityBarAdapter hoursAdapter;
    private AirQualityBarAdapter dailyAdapter;
    private RecyclerView recyclerViewDaily;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airquality);

        // 初始化视图和数据
        initViews();
        Intent intent = getIntent();

        hoursAdapter = new AirQualityBarAdapter(this); // 传入 Context
        dailyAdapter = new AirQualityBarAdapter(this);
        recyclerViewHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewDaily.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewHourly.setAdapter(hoursAdapter);
        recyclerViewDaily.setAdapter(dailyAdapter);

        WeatherResponse airQuality = (WeatherResponse) intent.getSerializableExtra("weather_response");

        if (airQuality != null) {
            // 2. 更新 UI
            updateAqiUI(airQuality);
            loadChartData(airQuality);
        } else {
            Toast.makeText(this, "获取数据失败", Toast.LENGTH_SHORT).show();
            finish();
        }
        // 设置监听器
        toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                //保证有一个按钮是checked
                if (isChecked) {
                    // 正常切换
                    if (checkedId == R.id.btn_hourly) {
                        showHourlyChart();
                        currentCkeckId=R.id.btn_hourly;
                    } else if (checkedId == R.id.btn_daily) {
                        showDailyChart();
                        currentCkeckId=R.id.btn_daily;
                    }
                } else {group.post(() -> {
                    group.check(currentCkeckId);
                });
                }
            }
        });
    }

    /**
     * 绑定布局中的控件
     */
    private void initViews() {
        recyclerViewHourly = findViewById(R.id.recyclerViewHourly);
        recyclerViewDaily = findViewById(R.id.recyclerViewDaily);
        toggleGroup = findViewById(R.id.toggleGroup);
        btnHourly = findViewById(R.id.btn_hourly);
        btnDaily = findViewById(R.id.btn_daily);
        tvAqiValue = findViewById(R.id.tv_aqi);
        tvAqiLevel = findViewById(R.id.tv_aqi_level);
        tvPm25 = findViewById(R.id.tv_pm2_5);
        tvPm10 = findViewById(R.id.tv_pm10);
        tvSo2 = findViewById(R.id.tv_so2);
        tvNo2 = findViewById(R.id.tv_no2);
        tvO3 = findViewById(R.id.tv_o3);
        tvCo = findViewById(R.id.tv_co);
        btnReturn = findViewById(R.id.btn_return_airquality);
        tvhourdayTitle = findViewById(R.id.tv_title);

        // 设置返回按钮点击事件
        btnReturn.setOnClickListener(v -> finish());

        toggleGroup.check(R.id.btn_hourly);
        showHourlyChart();
    }

    private void showHourlyChart() {
        recyclerViewHourly.setVisibility(View.VISIBLE);
        recyclerViewDaily.setVisibility(View.GONE);
        tvhourdayTitle.setText("24小时空气质量预报");
    }

    private void showDailyChart() {
        recyclerViewHourly.setVisibility(View.GONE);
        recyclerViewDaily.setVisibility(View.VISIBLE);
        tvhourdayTitle.setText("多日空气质量预报");

    }

    /**
     * 更新 AQI 相关的 UI
     * 注意：DailyAqi 包含 max, min, avg
     * 这里我们展示最大值 (max)
     */
    private void updateAqiUI(WeatherResponse weather) {
        Log.d("AirQuality", "AirQuality Object: " + weather.result.realtime.airQuality.toString());
        Log.d("AirQuality", "AQI Object: " + weather.result.realtime.airQuality.aqi); // 看看是不是 null
        Log.d("AirQuality", "Description Object: " + weather.result.realtime.airQuality.description); // 看看是不是 null
        // 处理AQI
        if (weather.result.realtime.airQuality.aqi != null) {
            tvAqiValue.setText(String.valueOf(weather.result.realtime.airQuality.aqi.chn));
        } else {
            tvAqiValue.setText("N/A");
        }

        // 处理Description
        if (weather.result.realtime.airQuality.description != null) {
            tvAqiLevel.setText(weather.result.realtime.airQuality.description.chn);
        } else {
            tvAqiLevel.setText("未知等级");
        }
        // 处理具体的污染物数值
        tvPm25.setText(String.valueOf(weather.result.realtime.airQuality.pm25));
        tvPm10.setText(String.valueOf(weather.result.realtime.airQuality.pm10));
        tvSo2.setText(String.valueOf(weather.result.realtime.airQuality.so2));
        tvNo2.setText(String.valueOf(weather.result.realtime.airQuality.no2));
        tvO3.setText(String.valueOf(weather.result.realtime.airQuality.o3));
        tvCo.setText(String.valueOf(weather.result.realtime.airQuality.co));
        setColorByLevelAndValue(weather.result.realtime.airQuality);
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

    private void loadChartData(WeatherResponse weatherResponse) {
        // ====== 加载 24 小时 AQI ======
        List<AirQualityItem> hourlyItems = new ArrayList<>();
        if (weatherResponse.result.hourly != null &&
                weatherResponse.result.hourly.airQuality != null &&
                weatherResponse.result.hourly.airQuality.aqi != null) {

            for (WeatherResponse.HourlyAqi hourly : weatherResponse.result.hourly.airQuality.aqi) {
                String time = extractHourFromIso8601(hourly.datetime); // 提取 "15:00"
                int aqiValue = hourly.value.chn; // 中国 AQI
                hourlyItems.add(new AirQualityItem(time, aqiValue));
            }
        }
        hoursAdapter.submitData(hourlyItems);

        List<AirQualityItem> dailyItems = new ArrayList<>();
        for (WeatherResponse.DailyAqi daily : weatherResponse.result.daily.airQuality.aqi) {
            String time = formatDatetomonthday(daily.date); // 提取 "15:00"
            int aqiValue = daily.avg.chn; // 中国 AQI
            dailyItems.add(new AirQualityItem(time, aqiValue));
        }
        dailyAdapter.submitData(dailyItems);
    }

}