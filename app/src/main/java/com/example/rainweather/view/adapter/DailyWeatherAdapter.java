package com.example.rainweather.view.adapter;
/**
 * description ：TODO:在RecyclerView中动态更新数据列表展示未来多日天气预报的适配器
 * email : 3014386984@qq.com
 * date : 2/11 14:00
 */

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.rainweather.R;
import com.example.rainweather.repository.model.DailyWeatherItem;

import java.util.ArrayList;
import java.util.List;

public class DailyWeatherAdapter extends RecyclerView.Adapter<DailyWeatherAdapter.ViewHolder> {

    private List<DailyWeatherItem> dataList = new ArrayList<>();

    public DailyWeatherAdapter(List<DailyWeatherItem> initialData) {
        this.dataList = initialData != null ? new ArrayList<>(initialData) : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_weather, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyWeatherItem item = dataList.get(position);

        holder.tvDate.setText(item.date);
        holder.tvWeather.setText(skyconToChinese(item.skycon));
        holder.tvTempRange.setText(String.format("%.0f° / %.0f°", item.minTemp, item.maxTemp));

        // 只有当动画文件改变时才重新设置
        String newLottieFile = getLottieFileName(item.skycon);
        if (!newLottieFile.equals(holder.currentLottieFile)) {
            holder.currentLottieFile = newLottieFile;
            holder.animationView.setAnimation(newLottieFile);
            holder.animationView.playAnimation();
        } else {
            // 如果已在播放，无需重复操作
            if (holder.animationView.isAnimating()) {
            } else {
                holder.animationView.playAnimation();
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.animationView.pauseAnimation();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void updateData(List<DailyWeatherItem> newData) {
        this.dataList.clear();
        if (newData != null) {
            this.dataList.addAll(newData);
        }
        notifyDataSetChanged();
    }

    // —————— 工具方法 ——————
//↓天气现象绑定
    public String skyconToChinese(String skycon) {
        switch (skycon) {
            case "CLEAR_DAY":
                return "晴";
            case "CLEAR_NIGHT":
                return "晴";
            case "PARTLY_CLOUDY_DAY":
                return "多云";
            case "PARTLY_CLOUDY_NIGHT":
                return "多云";
            case "CLOUDY":
                return "阴";
            case "LIGHT_RAIN":
                return "小雨";
            case "MODERATE_RAIN":
                return "中雨";
            case "HEAVY_RAIN":
                return "大雨";
            case "STORM_RAIN":
                return "暴雨";
            case "LIGHT_SNOW":
                return "小雪";
            case "MODERATE_SNOW":
                return "中雪";
            case "HEAVY_SNOW":
                return "大雪";
            case "STORM_SNOW":
                return "暴雪";
            case "LIGHT_HAZE":
                return "轻度雾霾";
            case "MODERATE_HAZE":
                return "中度雾霾";
            case "HEAVY_HAZE":
                return "重度污染";
            case "WIND":
                return "大风";
            case "FOG":
                return "雾";
            case "HAIL":
                return "冰雹";
            case "SLEET":
                return "雨夹雪";
            case "DUST":
            case "SAND":
                return "沙尘";
            case "HAZE":
                return "霾";
            default:
                Log.w("SKYCON", "Unknown skycon: " + skycon);
                return "未知";
        }
    }

    //↓天气图标绑定
    private String getLottieFileName(String skycon) {
        switch (skycon) {
            case "CLEAR_DAY":
                return "clear_day.json";
            case "CLEAR_NIGHT":
                return "clear_night.json";
            case "PARTLY_CLOUDY_DAY":
            case "CLOUDY":
                return "partly_cloudy_day.json";
            case "PARTLY_CLOUDY_NIGHT":
                return "partly_cloudy_night.json";
            case "LIGHT_RAIN":
            case "MODERATE_RAIN":
            case "HEAVY_RAIN":
            case "STORM_RAIN":
                return "rain.json";
            case "LIGHT_SNOW":
            case "MODERATE_SNOW":
            case "HEAVY_SNOW":
            case "STORM_SNOW":
                return "snow.json";
            case "WIND":
                return "wind.json";
            case "FOG":
                return "fog.json";
            case "LIGHT_HAZE":
            case "MODERATE_HAZE":
            case "HEAVY_HAZE":
                return "haze_day.json";
            default:
                return "clear_day.json";
        }
    }

    // —————— ViewHolder ——————
    static class ViewHolder extends RecyclerView.ViewHolder {
        LottieAnimationView animationView;
        TextView tvDate;
        TextView tvWeather;
        TextView tvTempRange; // 合并后的温度显示
        String currentLottieFile = "";

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            animationView = itemView.findViewById(R.id.animation_view);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvWeather = itemView.findViewById(R.id.tv_weather);
            tvTempRange = itemView.findViewById(R.id.tv_temp_range);
        }
    }
}