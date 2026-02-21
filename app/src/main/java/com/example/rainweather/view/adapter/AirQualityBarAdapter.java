package com.example.rainweather.view.adapter;
/**
 * description ：TODO:在RecyclerView中动态更新数据列表展示24h空气质量和15day柱状图的适配器
 * email : 3014386984@qq.com
 * date : 2/19 19：30
 */

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rainweather.R;
import com.example.rainweather.repository.model.AirQualityItem;

import java.util.ArrayList;
import java.util.List;

public class AirQualityBarAdapter extends RecyclerView.Adapter<AirQualityBarAdapter.BarViewHolder> {

    private List<AirQualityItem> dataList = new ArrayList<>();
    private Context context;

    // 最大柱状图高度（dp），根据你的 item 高度 140dp 减去文本空间后设定
    private static final int MAX_BAR_HEIGHT_DP = 110;
    public AirQualityBarAdapter(Context context) {
        this.context = context;
    }


    @NonNull
    @Override
    public BarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_airquality_bar, parent, false);
        return new BarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BarViewHolder holder, int position) {
        AirQualityItem item = dataList.get(position);

        // 1. 设置时间文本
        holder.tvTime.setText(item.getTime());
        holder.tvAqi.setText(String.valueOf(item.getAqi()));


        // 2. 计算柱状图高度（像素）
        int maxBarHeightPx = (int) (MAX_BAR_HEIGHT_DP * context.getResources().getDisplayMetrics().density);
        int calculatedHeight = (int) ((Math.min(item.getAqi(),500) / 500.0f) * maxBarHeightPx);
        int height = Math.max(calculatedHeight, 1); // 至少 3px

        // 动态设置 View 高度
        ViewGroup.LayoutParams params = holder.viewAqiBar.getLayoutParams();
        params.height = height;
        holder.viewAqiBar.setLayoutParams(params);
        holder.viewAqiBar.requestLayout();

        // 3. 根据 AQI 设置颜色（可选，增强可视化）
        int color;
        if (item.getAqi() <= 50) {
            color = ContextCompat.getColor(context, R.color.good);
        } else if (item.getAqi() <= 100) {
            color = ContextCompat.getColor(context, R.color.moderate);
        } else if(item.getAqi()<=150){
            color = ContextCompat.getColor(context, R.color.light_polution);
        }else{
            color = ContextCompat.getColor(context,R.color.aqi_hazardous);
        }
        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.bg_aqi_bar_rounded).mutate();

// 2. 动态设置颜色（保留圆角）
        drawable.setColor(color);

// 3. 应用到 View
        holder.viewAqiBar.setBackground(drawable);
        holder.tvAqi.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // 外部调用：更新数据
    public  void submitData(List<AirQualityItem> newData) {
        this.dataList.clear();
        this.dataList.addAll(newData);
        notifyDataSetChanged();
    }

    // ViewHolder 内部类
    static class BarViewHolder extends RecyclerView.ViewHolder {
        View viewAqiBar;
        TextView tvTime,tvAqi;

        public BarViewHolder(@NonNull View itemView) {
            super(itemView);
            viewAqiBar = itemView.findViewById(R.id.view_aqi_bar);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvAqi = itemView.findViewById(R.id.tv_hour_aqi);
        }
    }
}
