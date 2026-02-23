// repository/WeatherRepository.java
package com.example.rainweather.repository;

/**
 * description:  TODO：逆地理编码（高德），定位，缓存
 * email:3014386984@qq.com
 * date:2/19 17:45
 */

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.rainweather.repository.model.WeatherResponse;
import com.example.rainweather.repository.network.ApiClient;
import com.example.rainweather.repository.network.CaiyunApiService;
import com.example.rainweather.repository.network.MapClient;
import com.example.rainweather.repository.network.MapService;
import com.example.rainweather.utils.ApiConstants;
import com.example.rainweather.utils.LocationUtils;
import com.example.rainweather.utils.Resource;
import com.google.gson.Gson;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {
    private MapService mapService;

    private static WeatherRepository instance;
    private final CaiyunApiService apiService;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    private final Context context;
    private final Handler mainHandler;
    private final ExecutorService executorService;

    // 缓存常量
    private static final String KEY_WEATHER_JSON = "weather_json";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_LOCATION_LATLNG = "location_latlng";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final long CACHE_VALID_MS = 30 * 60 * 1000; // 30分钟

    // 定位相关
    private LocationManager locationManager;

    private WeatherRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = ApiClient.getClient();
        this.mapService = MapClient.getClient();
        this.sharedPreferences = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public static synchronized WeatherRepository getInstance(Context context) {
        if (instance == null) {
            instance = new WeatherRepository(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * 获取天气数据（核心逻辑：先读缓存，缓存无效则走网络）
     */
    public LiveData<Resource<WeatherResponse>> getWeather(boolean forceRefresh) {
        MutableLiveData<Resource<WeatherResponse>> result = new MutableLiveData<>();

        // 1. 如果不是强制刷新，先尝试读取缓存
        if (!forceRefresh) {
            WeatherResponse cached = getCacheWeather();
            String cachedLocation = getCacheLocation();
            if (cached != null && cachedLocation != null) {
                locationName.setValue(cachedLocation);
                result.setValue(Resource.success(cached));
                return result;
            }
        }

        // 2. 走网络请求（定位 + 天气）
        fetchLocationAndWeatherAsync(result);
        return result;
    }

    /**
     * 异步获取定位并请求天气
     * 这里使用了 ExecutorService 来处理定位权限和 getLastKnownLocation
     * 但核心的网络解析交给 LocationUtils (异步)
     */
    private void fetchLocationAndWeatherAsync(MutableLiveData<Resource<WeatherResponse>> result) {
        executorService.execute(() -> {
            try {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    mainHandler.post(() -> result.setValue(Resource.error("定位权限被拒绝", null)));
                    return;
                }

                Location location = getLastKnownLocation();
                if (location == null) {
                    location = requestSingleUpdate();
                }

                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String locationStr = longitude + "," + latitude;

                    // --- 关键修改：使用 LocationUtils 异步解析 ---
                    // 切回主线程或直接在子线程调用（Retrofit 不限制调用线程）
                    mainHandler.post(() -> {
                        result.setValue(Resource.loading(null)); // 显示加载中
                        parseLocationWithUtils(latitude, longitude, locationStr, result);
                    });

                } else {
                    mainHandler.post(() -> result.setValue(Resource.error("无法获取定位", null)));
                }
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(Resource.error("定位失败: " + e.getMessage(), null)));
            }
        });
    }
    /**
     * 使用新的 LocationUtils 解析位置
     */
    private void parseLocationWithUtils(double latitude, double longitude, String locationStr, MutableLiveData<Resource<WeatherResponse>> result) {
        LocationUtils.getCityName(latitude, longitude, new LocationUtils.OnCityNameListener() {
            @Override
            public void onSuccess(String cityName, String district, String town, String adcode) {
                // 拼接显示名称：例如 "吉林市 丰满区"
                String displayCity = cityName;
                if (district != null && !district.isEmpty() && !cityName.contains(district)) {
                    displayCity +=district+ " " + town;
                }
                // 更新城市名 LiveData
                locationName.postValue(displayCity);

                // 解析成功，请求天气
                fetchWeatherFromNetwork(result, locationStr, displayCity);
            }

            @Override
            public void onError(String errorMsg) {
                // 解析失败，使用默认名或经纬度
                Log.e("WeatherRepository", "解析城市失败: " + errorMsg);
                locationName.postValue("未知城市");
                // 即使解析失败，也尝试获取天气（仅凭经纬度）
                fetchWeatherFromNetwork(result, locationStr, "未知城市");
            }
        });
    }

    /**
     * 网络请求逻辑
     */
    private void fetchWeatherFromNetwork(MutableLiveData<Resource<WeatherResponse>> result, String locationStr, String cityName) {
        Call<WeatherResponse> call = apiService.getWeather(
                ApiConstants.API_TOKEN_CaiYun,
                locationStr,
                24, 15, "metric:v2"
        );

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                mainHandler.post(() -> {
                    if (response.isSuccessful() && response.body() != null && "ok".equals(response.body().status)) {
                        WeatherResponse weather = response.body();
                        saveToCache(weather, cityName, locationStr);
                        result.setValue(Resource.success(weather));
                    } else {
                        String errorMsg = "API Error";
                        result.setValue(Resource.error(errorMsg, null));
                    }
                });
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                mainHandler.post(() -> result.setValue(Resource.error("网络错误: " + t.getMessage(), null)));
            }
        });
    }

    /**
     * 获取最后已知位置
     */
    private Location getLastKnownLocation() {
        try {
            Location bestLocation = null;
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                bestLocation = getBetterLocation(gpsLocation, bestLocation);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                bestLocation = getBetterLocation(networkLocation, bestLocation);
            }
            return bestLocation;
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 请求单次定位更新
     */
    private Location requestSingleUpdate() {
        LocationListener listener = new LocationListener();
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, Looper.getMainLooper());
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, Looper.getMainLooper());
            } else {
                return null;
            }

            synchronized (listener) {
                listener.wait(10000);
            }
            return listener.location;
        } catch (SecurityException | InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            locationManager.removeUpdates(listener);
        }
    }

    // --- 缓存辅助方法 ---

    private WeatherResponse getCacheWeather() {
        long timestamp = sharedPreferences.getLong(KEY_TIMESTAMP, 0);
        if (System.currentTimeMillis() - timestamp > CACHE_VALID_MS) {
            return null;
        }
        String json = sharedPreferences.getString(KEY_WEATHER_JSON, null);
        if (json == null) return null;
        try {
            return gson.fromJson(json, WeatherResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String getCacheLocation() {
        long timestamp = sharedPreferences.getLong(KEY_TIMESTAMP, 0);
        if (System.currentTimeMillis() - timestamp > CACHE_VALID_MS) {
            return null;
        }
        return sharedPreferences.getString(KEY_LOCATION, null);
    }

    private void saveToCache(WeatherResponse weather, String location, String latLng) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_WEATHER_JSON, gson.toJson(weather));
        editor.putString(KEY_LOCATION, location);
        editor.putString(KEY_LOCATION_LATLNG, latLng);
        editor.putLong(KEY_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
    }

    // --- 为了配合 ViewModel 更新 UI ---

    private final MutableLiveData<String> locationName = new MutableLiveData<>();

    public LiveData<String> getLocationName() {
        return locationName;
    }

    // 内部定位监听器
    private static class LocationListener implements android.location.LocationListener {
        Location location;

        @Override
        public void onLocationChanged(Location location) {
            this.location = location;
            synchronized (this) {
                notify();
            }
        }
    }

    // 辅助方法：比较两个位置，返回更精确的一个
    private Location getBetterLocation(Location newLocation, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return newLocation;
        }

        long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > 120000;
        boolean isSignificantlyOlder = timeDelta < -60000;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return newLocation;
        } else if (isSignificantlyOlder) {
            return currentBestLocation;
        }

        int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSlightlyLessAccurate = accuracyDelta > 0 && accuracyDelta < 200;

        boolean isFromSameProvider = false;
        if (newLocation.getProvider() != null && newLocation.getProvider().equals(currentBestLocation.getProvider())) {
            isFromSameProvider = true;
        }

        if (isMoreAccurate || (isSlightlyLessAccurate && isFromSameProvider)) {
            return newLocation;
        }
        return currentBestLocation;
    }
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        // 移除所有消息，防止内存泄漏
        mainHandler.removeCallbacksAndMessages(null);
    }
}