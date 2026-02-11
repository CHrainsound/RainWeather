package com.example.rainweather.view.activity;

/**
 * description ：
 * TODO: 定位，ui刷新，反地理编码，周期管理，日期格式化
 * email : 3014386984@qq.com
 * date : 2/11 13:00
 */

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.rainweather.R;
import com.example.rainweather.repository.model.DailyWeatherItem;
import com.example.rainweather.repository.model.WeatherResponse;
import com.example.rainweather.repository.network.ApiClient;
import com.example.rainweather.repository.network.CaiyunApiService;
import com.example.rainweather.view.adapter.DailyWeatherAdapter;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private static final String PREF_NAME = "weather_cache";
    private static final String KEY_WEATHER_JSON = "weather_json";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final long CACHE_VALID_MS = 30 * 60 * 1000; // 30分钟有效

    // 视频相关
    private StyledPlayerView playerView;
    private ExoPlayer exoPlayer;
    private int currentVideoResId = -1;

    // UI 组件
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvTemperature;
    private TextView tvLocation;
    private TextView tvWeatherDesc; // 包含最高/最低温
    private RecyclerView rvDailyForecast;
    private DailyWeatherAdapter dailyAdapter;

    // 网络 & 定位
    private LocationManager locationManager;

    private CaiyunApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视频
        playerView = findViewById(R.id.pv_background);
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);
        exoPlayer.setVolume(0f);
        exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);

        // 初始化 UI
        tvTemperature = findViewById(R.id.tv_temperature);
        tvWeatherDesc = findViewById(R.id.tv_weather_desc);
        tvLocation = findViewById(R.id.tv_location);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        rvDailyForecast = findViewById(R.id.rv_daily_forecast);
        rvDailyForecast.setLayoutManager(new LinearLayoutManager(this));
        dailyAdapter = new DailyWeatherAdapter(new ArrayList<>());
        rvDailyForecast.setAdapter(dailyAdapter);

        // 设置下拉刷新监听器
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新：不使用缓存，强制重新定位 + 获取最新天气
                requestLocationAndFetchWeather(true);
            }
        });

        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        // 初始化服务
        apiService = ApiClient.getClient().create(CaiyunApiService.class);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 首次加载（可使用缓存）
        loadWeather();
    }

    //↓停止刷新
    private void stopRefreshing() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    //↓加载天气数据：先尝试从本地缓存读取并更新UI
    private void loadWeather() {
        WeatherResponse cached = readFromCache();
        if (cached != null) {
            updateUI(cached);
        }
        requestLocationAndFetchWeather(false);
    }

    //根据经纬度反向地理编码，将坐标转换为中文城市名。使用Geocoder
    private void reverseGeocodeCity(double latitude, double longitude, OnCityResolvedListener listener) {
        new Thread(() -> {
            String resultCity = "未知位置";
            try {
                Geocoder geocoder = new Geocoder(this, Locale.CHINA);
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String city = address.getLocality();

                    if (city == null || city.trim().isEmpty()) {
                        city = address.getSubAdminArea();
                    }
                    if (city == null || city.trim().isEmpty()) {
                        city = address.getCountryName();
                    }
                    if (city != null) {
                        resultCity = city.trim(); // 最终结果赋给resultCity
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                resultCity = "定位失败";
            }
            final String finalCity = resultCity;
            runOnUiThread(() -> listener.onCityResolved(finalCity));
        }).start();
    }

    // 回调接口
    public interface OnCityResolvedListener {
        void onCityResolved(String cityName);
    }


    //请求定位权限，并根据权限状态决定是否获取位置。
    //若已有位置缓存则直接使用；否则发起单次定位更新
    private void requestLocationAndFetchWeather(boolean isFromRefresh) {
        //isFromRefresh判断是否下拉刷新触发
        // 检查定位权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            if (isFromRefresh) {
                stopRefreshing(); // 如果是下拉刷新时无权限，立即停止动画
            }
            return;
        }

        // 尝试获取最后已知位置（优先 GPS，其次 Network）
        Location lastKnown = null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (lastKnown == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (lastKnown != null) {
            // 使用缓存
            double lat = lastKnown.getLatitude();
            double lng = lastKnown.getLongitude();
            reverseGeocodeCity(lat, lng, cityName -> {
                runOnUiThread(() -> tvLocation.setText(cityName));
                fetchWeather(lng, lat, isFromRefresh);
            });
        } else {
            // 无缓存位置,请求一次定位更新
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    reverseGeocodeCity(lat, lng, cityName -> {
                        runOnUiThread(() -> tvLocation.setText(cityName));
                        fetchWeather(lng, lat, isFromRefresh);
                    });
                    // 获取到位置后移除监听器，避免重复回调
                    try {
                        locationManager.removeUpdates(this);
                    } catch (SecurityException e) {
                        // 忽略权限异常（理论上已有权限）
                    }
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    // 定位被关闭的情况
                    if (isFromRefresh) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "请开启定位服务", Toast.LENGTH_SHORT).show();
                            stopRefreshing();
                        });
                    }
                }
            };

            // 发起单次定位请求
            boolean requested = false;
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, Looper.getMainLooper());
                requested = true;
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, Looper.getMainLooper());
                requested = true;
            }

            if (!requested) {
                // 所有定位源都不可用
                runOnUiThread(() -> {
                    tvLocation.setText("定位服务未开启");
                    if (isFromRefresh) {
                        Toast.makeText(this, "请开启定位服务以刷新天气", Toast.LENGTH_SHORT).show();
                        stopRefreshing();
                    }
                });
            }
        }
    }

    //通过彩云天气 API 获取指定坐标的实时与多日天气预报
    //使用Retrofit请求，成功后更新UI并缓存数据
    private void fetchWeather(double longitude, double latitude, boolean isFromRefresh) {
        String token = "1318TA9BEUajXthz";//不要偷我的token，呜呜呜呜呜呜呜，不然我就一直哭哭哭
        String locationStr = longitude + "," + latitude;

        Log.d("DEBUG_API", "Token: [" + token + "]");
        Log.d("DEBUG_API", "Location: [" + locationStr + "]");

        Call<WeatherResponse> call = apiService.getWeather(token, locationStr, 24, 15, true);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        WeatherResponse weather = response.body();
                        if ("ok".equals(weather.status)) {
                            updateUI(weather);
                            if (!isFromRefresh) {
                                saveToCache(weather); //刷新后也更新缓存
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "API 错误: " + weather.status, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "请求失败: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                    stopRefreshing(); //无论成功失败都停止刷新
                });
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    stopRefreshing(); //失败也要停止
                });
            }
        });
    }


    //根据 WeatherResponse 数据更新主界面所有 UI 元素：
    //当前温度与天气描述
    //动态背景视频（根据天气类型和昼夜状态）
    //多日天气预报列表
    private void updateUI(WeatherResponse weather) {
        boolean isNight = false;
        try {
            // API日落时间格式"18:37"
            String sunsetTimeStr = weather.result.daily.astro.get(0).sunset.time;
            LocalTime sunsetTime = LocalTime.parse(sunsetTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime now = LocalTime.now(ZoneId.of("Asia/Shanghai")); // 使用北京时间
            isNight = now.isAfter(sunsetTime);
        } catch (Exception e) {
            e.printStackTrace();
            // 解析失败默认白天
            isNight = false;
        }

        // === 当前天气 ===
        double temp = weather.result.realtime.temperature;
        String skycon = weather.result.realtime.skycon;
        tvTemperature.setText(String.format("%.0f°", temp)); // 如 "-4°"

        // 构建描述文本："晴 最高-4° 最低-18°"
        String desc = dailyAdapter.skyconToChinese(skycon);
        if (weather.result.daily != null && !weather.result.daily.temperature.isEmpty()) {
            double max = weather.result.daily.temperature.get(0).max;
            double min = weather.result.daily.temperature.get(0).min;
            desc += String.format(" 最高%.0f° 最低%.0f°", max, min);
        }
        tvWeatherDesc.setText(desc);

        // 切换背景视频
        setWeatherVideo(skycon, isNight);

        // 多日预报
        List<DailyWeatherItem> dailyItems = new ArrayList<>();
        if (weather.result.daily != null) {
            List<WeatherResponse.DailyTemp> temps = weather.result.daily.temperature;
            List<WeatherResponse.DailySkycon> skycons = weather.result.daily.skycon;

            if (temps != null && skycons != null) {
                int count = Math.min(temps.size(), skycons.size());
                for (int i = 0; i < count; i++) {

                    String dateStr = temps.get(i).date; // "2026-02-11"
                    String skyconValue = skycons.get(i).value;
                    double maxTemp = temps.get(i).max;
                    double minTemp = temps.get(i).min;
                    Log.d("WEATHER_DEBUG", "Day " + i + ": date=" + dateStr + ", skycon=" + skyconValue);

                    //转换日期为"今天"/"明天"/"周五"
                    String displayDate = formatDateForDisplay(dateStr, i);

                    dailyItems.add(new DailyWeatherItem(displayDate, skyconValue, maxTemp, minTemp));
                }
            }
        }
        dailyAdapter.updateData(dailyItems);
    }

    // 日期格式化
    private String formatDateForDisplay(String isoDateTime, int i) {
        if (TextUtils.isEmpty(isoDateTime)) return "";

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmXXX");
            var zonedDateTime = java.time.ZonedDateTime.parse(isoDateTime, formatter);

            ZoneId beijing = ZoneId.of("Asia/Shanghai");
            LocalDate targetDate = zonedDateTime.withZoneSameInstant(beijing).toLocalDate();
            LocalDate today = LocalDate.now(beijing);

            long daysDiff = ChronoUnit.DAYS.between(today, targetDate);

            switch ((int) daysDiff) {
                case 0:
                    return "今天";
                case 1:
                    return "明天";
                default:
                    return getChineseDayOfWeek(targetDate.getDayOfWeek().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return isoDateTime.substring(0, 10).replace("-", "/");
        }
    }

    // 辅助方法：英文星期转化中文
    private String getChineseDayOfWeek(String englishDay) {
        switch (englishDay) {
            case "MONDAY":
                return "周一";
            case "TUESDAY":
                return "周二";
            case "WEDNESDAY":
                return "周三";
            case "THURSDAY":
                return "周四";
            case "FRIDAY":
                return "周五";
            case "SATURDAY":
                return "周六";
            case "SUNDAY":
                return "周日";
            default:
                return englishDay;
        }
    }

    // 视频切换逻辑
    private void setWeatherVideo(String skycon, boolean isNight) {
        int videoResId = getVideoResourceForSkycon(skycon, isNight);
        if (videoResId == currentVideoResId) return;

        currentVideoResId = videoResId;
        if (videoResId != 0) {
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + videoResId);
            MediaItem mediaItem = MediaItem.fromUri(uri);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.play();
        }
    }

    private int getVideoResourceForSkycon(String skycon, boolean isNight) {
        if (isNight) {
            // 夜晚模式
            switch (skycon) {
                case "CLEAR_DAY":
                case "CLEAR_NIGHT":
                    return R.raw.weather_clear;
                case "PARTLY_CLOUDY_DAY":
                case "PARTLY_CLOUDY_NIGHT":
                case "CLOUDY":
                    return R.raw.weather_cloudy_night;
                case "LIGHT_RAIN":
                case "MODERATE_RAIN":
                case "HEAVY_RAIN":
                    return R.raw.weather_rain_night;
                case "STORM_RAIN":
                    return R.raw.weather_thunderstorm_night;
                case "LIGHT_SNOW":
                case "MODERATE_SNOW":
                case "HEAVY_SNOW":
                case "STORM_SNOW":
                    return R.raw.weather_snow_night;
                case "FOG":
                case "LIGHT_HAZE":
                case "MODERATE_HAZE":
                case "HEAVY_HAZE":
                    return R.raw.weather_fog_night;
                case "WIND":
                    return R.raw.weather_windy_night;
                default:
                    return R.raw.weather_clear;
            }
        } else {
            // 白天模式（保持你原来的逻辑）
            switch (skycon) {
                case "CLEAR_DAY":
                    return R.raw.weather_sunny;
                case "CLEAR_NIGHT":
                    return R.raw.weather_clear; // 白天晴？但 CLEAR_NIGHT 不该出现在白天，可合并
                case "LIGHT_RAIN":
                case "MODERATE_RAIN":
                case "HEAVY_RAIN":
                    return R.raw.weather_rain_day;
                case "STORM_RAIN":
                    return R.raw.weather_thunderstorm_day;
                case "LIGHT_SNOW":
                case "MODERATE_SNOW":
                case "HEAVY_SNOW":
                case "STORM_SNOW":
                    return R.raw.weather_snow_day;
                case "CLOUDY":
                case "PARTLY_CLOUDY_DAY":
                case "PARTLY_CLOUDY_NIGHT":
                    return R.raw.weather_cloudy_day;
                case "FOG":
                case "LIGHT_HAZE":
                case "MODERATE_HAZE":
                case "HEAVY_HAZE":
                    return R.raw.weather_fog_day;
                case "WIND":
                    return R.raw.weather_windy_day;
                default:
                    return R.raw.weather_sunny;
            }
        }
    }

    // ----------工具方法

    // 缓存数据
    private void saveToCache(WeatherResponse weather) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(weather);
        editor.putString(KEY_WEATHER_JSON, json);
        editor.putLong(KEY_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
    }

    private WeatherResponse readFromCache() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_WEATHER_JSON, null);
        long timestamp = prefs.getLong(KEY_TIMESTAMP, 0);
        if (json == null || System.currentTimeMillis() - timestamp > CACHE_VALID_MS) {
            return null;
        }
        try {
            return new Gson().fromJson(json, WeatherResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationAndFetchWeather(false);
            } else {
                Toast.makeText(this, "需要定位权限才能获取天气", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Activity 暂停时暂停背景视频播放
    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    //Activity 恢复时恢复背景视频播放
    @Override
    protected void onResume() {
        super.onResume();
        if (exoPlayer != null && currentVideoResId != -1) {
            exoPlayer.play();
        }
    }


    //Activity销毁时释放 ExoPlayer
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}