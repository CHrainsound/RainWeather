package com.example.rainweather.view.activity;

/**
 * description ： TODO：负责展示当前天气详情、背景动态视频、多日预报列表以及空气质量入口
 * email : 3014386984@qq.com
 * date : 2/19 16:00
 */

import static com.example.rainweather.utils.DateUtils.formatDateForDisplay;
import static com.example.rainweather.utils.VideoUtils.getVideoResourceForSkycon;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rainweather.R;
import com.example.rainweather.databinding.ActivityMainBinding;
import com.example.rainweather.repository.model.DailyWeatherItem;
import com.example.rainweather.repository.model.WeatherResponse;
import com.example.rainweather.utils.Resource;
import com.example.rainweather.view.adapter.DailyWeatherAdapter;
import com.example.rainweather.viewmodel.MainViewModel;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.material.button.MaterialButton;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // ViewBinding
    private ActivityMainBinding binding;
    // ViewModel
    private MainViewModel viewModel;
    // 背景播放器
    private ExoPlayer exoPlayer;
    private int currentVideoResId = -1;

    // UI
    private TextView tvTemperature;
    private TextView tvLocation;
    private TextView tvWeatherDesc;
    private MaterialButton mbtnAqi;

    // 适配器
    private final DailyWeatherAdapter dailyAdapter = new DailyWeatherAdapter(new ArrayList<>());

    // 权限请求
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // 初始化视图和播放器
        initViews();

        // 设置监听器
        setupListeners();

        // 观察数据
        setupObservers();

        // 权限请求器
        setupPermissionLauncher();

        // 页面启动时加载
        loadWeather();
    }

    /**
     * 初始化视图组件
     */
    private void initViews() {
        // 初始化 UI 控件 (修复空指针风险)
        tvTemperature = binding.tvTemperature;
        tvLocation = binding.tvLocation;
        tvWeatherDesc = binding.tvWeatherDesc;
        mbtnAqi = binding.btnAirQuality;

        // RecyclerView
        binding.rvDailyForecast.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDailyForecast.setAdapter(dailyAdapter);

        // ExoPlayer
        exoPlayer = new ExoPlayer.Builder(this).build();
        binding.pvBackground.setPlayer(exoPlayer);
        binding.pvBackground.setKeepContentOnPlayerReset(true);
        binding.pvBackground.setControllerShowTimeoutMs(0); // 隐藏控制栏
        exoPlayer.setVolume(0f);
        exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
    }

    /**
     * 设置 UI 监听器
     */
    private void setupListeners() {
        // 空气质量点击
        mbtnAqi.setOnClickListener(v -> navigateToAirQuality());

        // 下拉刷新
        binding.swipeRefreshMain.setOnRefreshListener(() -> viewModel.refreshWeather());
        binding.swipeRefreshMain.setColorSchemeResources(android.R.color.holo_blue_light);
    }

    /**
     * 设置权限请求器
     */
    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // 权限通过，通知 ViewModel 开始干活
                        viewModel.loadWeather();
                    } else {
                        Toast.makeText(this, "定位权限被拒绝", Toast.LENGTH_SHORT).show();
                        // 即使拒绝，也通知 ViewModel 尝试读取缓存或使用默认数据
                        viewModel.loadWeather();
                    }
                }
        );
    }

    /**
     * 观察数据变化
     */
    private void setupObservers() {
        // 观察天气状态
        viewModel.getWeatherState().observe(this, resource -> {
            if (resource == null) return;

            // 处理加载状态 (控制下拉刷新动画)
            binding.swipeRefreshMain.setRefreshing(resource.status == Resource.Status.LOADING);

            switch (resource.status) {
                case SUCCESS:
                    if (resource.data != null) {
                        updateUI(resource.data);
                    }
                    break;
                case ERROR:
                    if (resource.message != null) {
                        Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        });

        // 观察城市名称
        viewModel.getLocationName().observe(this, cityName -> {
            if (cityName != null) {
                tvLocation.setText(cityName);
            }
        });
    }

    /**
     * 加载天气入口
     */
    private void loadWeather() {
        // 检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // 已授权，直接让 ViewModel 加载
            viewModel.loadWeather();
        } else {
            // 未授权，申请权限
            requestLocationPermission();
        }
    }

    /**
     * 请求定位权限
     */
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("定位权限")
                    .setMessage("需要定位权限来获取您所在城市的天气")
                    .setPositiveButton("确定", (dialog, which) -> permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION))
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /**
     * 更新 UI
     */
    private void updateUI(WeatherResponse weather) {
        // 判断日夜
        boolean isNight = isCurrentTimeNight(weather);

        // 更新温度
        double temp = weather.result.realtime.temperature;
        String skycon = weather.result.realtime.skycon;
        tvTemperature.setText(String.format("%.0f°", temp));

        // 更新 AQI
        updateAqiText(weather);

        // 更新描述
        String desc = dailyAdapter.skyconToChinese(skycon);
        if (weather.result.daily != null && !weather.result.daily.temperature.isEmpty()) {
            double max = weather.result.daily.temperature.get(0).max;
            double min = weather.result.daily.temperature.get(0).min;
            desc += String.format(" 最高%.0f° 最低%.0f°", max, min);
        }
        tvWeatherDesc.setText(desc);

        // 切换视频
        setWeatherVideo(skycon, isNight);

        // 更新列表
        updateDailyForecast(weather);
    }

    /**
     * 更新 AQI 文本
     */
    private void updateAqiText(WeatherResponse weather) {
        StringBuilder aqiText = new StringBuilder("空气");
        try {
            if (weather.result.realtime.airQuality != null) {
                if (weather.result.realtime.airQuality.description != null) {
                    aqiText.append(weather.result.realtime.airQuality.description.chn).append(" ");
                }
                if (weather.result.realtime.airQuality.aqi != null) {
                    aqiText.append(weather.result.realtime.airQuality.aqi.chn);
                }
            } else {
                aqiText.append("未知");
            }
        } catch (Exception e) {
            aqiText.append("未知");
        }
        mbtnAqi.setText(aqiText.toString());
    }

    /**
     * 判断是否夜晚
     */
    private boolean isCurrentTimeNight(WeatherResponse weather) {
        try {
            String sunsetTimeStr = weather.result.daily.astro.get(0).sunset.time;
            LocalTime sunsetTime = LocalTime.parse(sunsetTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime now = LocalTime.now(ZoneId.of("Asia/Shanghai"));
            return now.isAfter(sunsetTime);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新多日预报
     */
    private void updateDailyForecast(WeatherResponse weather) {
        List<DailyWeatherItem> dailyItems = new ArrayList<>();
        if (weather.result.daily != null) {
            List<WeatherResponse.DailyTemp> temps = weather.result.daily.temperature;
            List<WeatherResponse.DailySkycon> skycons = weather.result.daily.skycon;

            if (temps != null && skycons != null) {
                int count = Math.min(temps.size(), skycons.size());
                for (int i = 0; i < count; i++) {
                    String dateStr = temps.get(i).date;
                    String skyconValue = skycons.get(i).value;
                    double maxTemp = temps.get(i).max;
                    double minTemp = temps.get(i).min;
                    String displayDate = formatDateForDisplay(dateStr);
                    dailyItems.add(new DailyWeatherItem(displayDate, skyconValue, maxTemp, minTemp));
                }
            }
        }
        dailyAdapter.updateData(dailyItems);
    }

    /**
     * 设置背景视频
     */
    private void setWeatherVideo(String skycon, boolean isNight) {
        int videoResId = getVideoResourceForSkycon(skycon, isNight);
        if (videoResId == currentVideoResId || videoResId == 0) return;

        currentVideoResId = videoResId;
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + videoResId);
        MediaItem mediaItem = MediaItem.fromUri(uri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();
    }

    /**
     * 跳转到空气质量页面
     */
    private void navigateToAirQuality() {
        // 直接从 weatherState 里拿数据
        Resource<WeatherResponse> resource = viewModel.getWeatherState().getValue();
        if (resource != null && resource.data != null) {
            Intent intent = new Intent(MainActivity.this, AirQualityActivity.class);
            intent.putExtra("air_quality_data", resource.data.result.realtime.airQuality);
            startActivity(intent);
        } else {
            Toast.makeText(this, "数据加载中...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 页面可见时恢复播放
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.getPlaybackState();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 页面不可见时暂停
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        // 解绑视图
        if (binding != null) {
            binding.pvBackground.setPlayer(null); // 防止内存泄漏
        }
        binding = null;
    }
}