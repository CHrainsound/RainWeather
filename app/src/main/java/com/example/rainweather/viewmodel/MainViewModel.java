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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.rainweather.repository.WeatherRepository;
import com.example.rainweather.repository.model.WeatherResponse;
import com.example.rainweather.utils.Resource;

public class MainViewModel extends AndroidViewModel {

    private final WeatherRepository repository;

    //内部持有 MutableLiveData
    private final MutableLiveData<Resource<WeatherResponse>> weatherState = new MutableLiveData<>();
    private final MutableLiveData<String> locationName = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.repository = WeatherRepository.getInstance(application);
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

        // 获取 Repository 返回的 LiveData
        LiveData<Resource<WeatherResponse>> repoLiveData = repository.getWeather(forceRefresh);

        // 使用 MediatorLiveData 或直接 observeForever (此处为了简单演示，直接 observeForever，但需注意移除)
        // 更好的做法是使用 Transformations，但为了清晰，我们手动处理
        repoLiveData.observeForever(new Observer<Resource<WeatherResponse>>() {
            @Override
            public void onChanged(Resource<WeatherResponse> result) {
                if (result != null) {
                    // 转发天气状态
                    weatherState.setValue(result);
                }
            }
        });

        //观察 Repository的locationName 变化，并同步给ViewModel的locationName

        repository.getLocationName().observeForever(new Observer<String>() {
            @Override
            public void onChanged(String cityName) {
                // 当 Repository 解析出城市名时，ViewModel 也更新自己的 locationName
                locationName.setValue(cityName);
            }
        });
    }

    // --- 暴露给 Activity 观察 ---

    public LiveData<Resource<WeatherResponse>> getWeatherState() {
        return weatherState;
    }

    public LiveData<String> getLocationName() {
        return locationName;
    }


    public LiveData<WeatherResponse> getWeatherData() {
        // 返回一个 LiveData，它只包含数据本身，不包含加载/错误状态
        return new LiveData<WeatherResponse>() {
            @Override
            protected void onActive() {
                super.onActive();
                // 观察内部的 weatherState
                weatherState.observeForever(resource -> {
                    if (resource != null && resource.status == Resource.Status.SUCCESS) {
                        // 只有在成功时才发送数据
                        setValue(resource.data);
                    }
                });
            }

            @Override
            protected void onInactive() {
                super.onInactive();
            }
        };
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 通知 Repository 清理线程池
        repository.cleanup();
    }
}