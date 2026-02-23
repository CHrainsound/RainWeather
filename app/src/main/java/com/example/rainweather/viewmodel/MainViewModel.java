package com.example.rainweather.viewmodel;
/**
 * description ：主视图模型
 * TODO:管理天气数据的获取、状态更新以及与 UI层之间的通信。
 * email : 3014386984@qq.com
 * date : 2/19 18:15
 */

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import com.example.rainweather.repository.WeatherRepository;
import com.example.rainweather.repository.model.HourlyChartData;
import com.example.rainweather.repository.model.WeatherResponse;
import com.example.rainweather.utils.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final WeatherRepository repository;

    //内部持有 MutableLiveData
    private final MediatorLiveData<Resource<WeatherResponse>> weatherState = new MediatorLiveData<>();
    private final MediatorLiveData<String> locationName = new MediatorLiveData<>();
    private final MediatorLiveData<List<HourlyChartData>> hourlyChartData = new MediatorLiveData<>();
    private LiveData<Resource<WeatherResponse>> currentWeatherSource;
    private LiveData<String> currentLocationSource;

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.repository = WeatherRepository.getInstance(application);
    }
    private void addSource(boolean forceRefresh) {
        if (currentWeatherSource != null) {
            weatherState.removeSource(currentWeatherSource);
        }
        if (currentLocationSource != null) {
            locationName.removeSource(currentLocationSource);
        }

        currentWeatherSource = repository.getWeather(forceRefresh);
        currentLocationSource = repository.getLocationName();

        weatherState.addSource(currentWeatherSource, resource -> {
            weatherState.setValue(resource);
            if (resource != null && resource.status == Resource.Status.SUCCESS) {
                updateHourlyChartData(resource.data);
            }
        });

        locationName.addSource(currentLocationSource, locationName::setValue);
    }
    private void updateHourlyChartData(WeatherResponse weather) {
        if (weather == null || weather.result == null || weather.result.hourly == null) {
            hourlyChartData.setValue(new ArrayList<>());
            return;
        }

        List<HourlyChartData> data = HourlyChartData.fromWeatherResponse(weather);
        hourlyChartData.setValue(data);
    }

    /**
     * 供 Activity 调用：首次加载或根据权限加载
     */
    public void loadWeather() {
        // 传递false，表示允许 Repository 读取缓存
        fetchWeather(false);
    }

    /**
     * 供 UI 调用：下拉刷新
     */
    public void refreshWeather() {
        // 传递 true，表示强制从网络获取最新数据
        fetchWeather(true);
    }

    /**
     * 私有方法：执行实际的数据获取逻辑
     */
    private void fetchWeather(boolean forceRefresh) {
        // 发送加载中状态
        weatherState.setValue(Resource.loading(null));
        addSource(forceRefresh);

    }

    // 给Activity观察

    public LiveData<Resource<WeatherResponse>> getWeatherState() {
        return weatherState;
    }

    public LiveData<String> getLocationName() {
        return locationName;
    }


    public LiveData<List<HourlyChartData>> getHourlyChartData() {
        return hourlyChartData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 通知 Repository 清理线程池
        repository.cleanup();
    }
}