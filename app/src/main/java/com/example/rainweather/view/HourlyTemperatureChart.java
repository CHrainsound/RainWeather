
package com.example.rainweather.view;
/**
 * description ：TODO:生成气温折线图
 * email : 3014386984@qq.com
 * date : 2/21 10:00
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;
import androidx.annotation.Nullable;
import com.example.rainweather.R;
import com.example.rainweather.repository.model.HourlyChartData;
import java.util.ArrayList;
import java.util.List;

public class HourlyTemperatureChart extends View {
    private OverScroller mScroller;
    private static final int PADDING_LEFT_RIGHT =90;
    private final Paint mSunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<HourlyChartData> mData;
    private Paint mLinePaint;
    private Paint mTextPaint;
    private Paint mCirclePaint;
    private GestureDetector mGestureDetector;
    private int mItemWidth = 0;

    private float mTranslateX = 0;
    private int mVisibleItemCount = 7; // 一屏显示的项目数

    // 颜色资源
    private int mLineColor;
    private int mTextColor;
    private int mNowPointColor;

    public HourlyTemperatureChart(Context context) {
        super(context);
        init();
    }

    public HourlyTemperatureChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 从主题或资源中获取颜色，这里先写死
        mLineColor = Color.parseColor("#81D8D0");
        mTextColor = Color.WHITE;
        mNowPointColor = Color.YELLOW;

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeWidth(4f);
        mLinePaint.setStyle(Paint.Style.STROKE);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(mLineColor);
        mCirclePaint.setStyle(Paint.Style.FILL);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mGestureDetector = new GestureDetector(getContext(), new ChartGestureListener());
        mScroller = new OverScroller(getContext());

        mSunPaint.setColor(Color.parseColor("#FFD700"));
        mSunPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mTranslateX = mScroller.getCurrX();
            invalidate(); // 触发 onDraw
            // 继续下一帧
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateItemWidth();
    }

    private void updateItemWidth() {
        if (mData == null || mData.isEmpty()) {
            mItemWidth = 0;
            return;
        }
        int width = getWidth();
        if (width <= 0) return;
        // 动态显示 5~8 个点
        int displayCount = Math.min(Math.max(5, mData.size()), 8);
        mItemWidth = width / Math.max(1, displayCount - 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mData == null || mData.isEmpty() || mItemWidth <= 0) return;
        int width = getWidth();
        int height = getHeight();
        int paddingTop = 0;
        int paddingBottom = 330; // 底部留出空间给文字
        int contentHeight = height - paddingTop - paddingBottom;
        int baselineY = height - paddingBottom;

        TemperatureRange tempRange = calculateTemperatureRange(mData);
        float minTemp = tempRange.min;
        float maxTemp = tempRange.max;
        float tempSpan = maxTemp - minTemp;
        if (tempSpan <= 0) tempSpan = 1f;
        // 绘制折线和点
        float chartHeightRatio = 0.55f;
        Path path = new Path();
        boolean firstPoint = true;
        for (int i = 0; i < mData.size(); i++) {
            HourlyChartData data = mData.get(i);

            float x = PADDING_LEFT_RIGHT + i * mItemWidth + mTranslateX;
            float tempOffset = (data.temperature - minTemp) / tempSpan;
            float y = baselineY - tempOffset * (contentHeight * chartHeightRatio);

            if (firstPoint) {
                path.moveTo(x, y);
                firstPoint = false;
            } else {
                path.lineTo(x, y);
            }

            if (data.isNow) {
                mCirclePaint.setColor(mNowPointColor);
                canvas.drawCircle(x, y, 10, mCirclePaint);
                mCirclePaint.setColor(mLineColor);
            } else if (data.isSunEvent) {
                // 日出/日落点：稍大一点，或用不同颜色
                Paint sunPaint = new Paint(mCirclePaint);
                sunPaint.setColor(Color.parseColor("#FFD700")); // 金色
                canvas.drawCircle(x, y, 10, mSunPaint);
            } else {
                canvas.drawCircle(x, y, 6, mCirclePaint);
            }

            mTextPaint.setTextSize(60f);
            String tempText = Math.round(data.temperature) + "°";
            canvas.drawText(tempText, x, y - 25, mTextPaint);
        }
        canvas.drawPath(path, mLinePaint);

// 再遍历所有数据，统一画底部信息（包括太阳事件）
        for (int i = 0; i < mData.size(); i++) {
            HourlyChartData data = mData.get(i);
            float x = PADDING_LEFT_RIGHT + i * mItemWidth + mTranslateX;
            drawBottomInfo(canvas, data, x, height);
        }
    }

    private void drawBottomInfo(Canvas canvas, HourlyChartData data, float x, int viewHeight) {
        mTextPaint.setTextSize(45f);
        float y = viewHeight - 20;

        // 时间：如果是日出/日落，显示精确时间（如 06:48），否则显示原始时间（如 06:00）
        String displayTime = data.isSunEvent && data.sunEventTime != null
                ? data.sunEventTime
                : data.time;
        canvas.drawText(displayTime, x, y - mTextPaint.getTextSize() * 0.8f, mTextPaint);

        // 天气图标
        mTextPaint.setTextSize(80f);
        float lineHeight = mTextPaint.getTextSize() * 1.3f;
        canvas.drawText(getIconTextFromSkycon(data.skycon), x, y - mTextPaint.getTextSize() * 0.8f - lineHeight, mTextPaint);

        // 风力 or 日出/日落
        mTextPaint.setTextSize(40f);
        String windOrSunLabel = data.isSunEvent
                ? ("sunrise".equals(data.sunEventType) ? "日出" : "日落")
                : data.windSpeed;
        canvas.drawText(windOrSunLabel, x, y - mTextPaint.getTextSize() * 0.8f - 2 * lineHeight, mTextPaint);
    }

    private String getIconTextFromSkycon(String skycon) {
        switch (skycon) {
            case "CLEAR_DAY":
                return "☀️";
            case "CLEAR_NIGHT":
                return "🌙";
            case "PARTLY_CLOUDY_DAY":
                return "⛅";
            case "CLOUDY":
                return "☁";
            case "PARTLY_CLOUDY_NIGHT":
                return "⛅";
            case "LIGHT_RAIN":
            case "MODERATE_RAIN":
            case "HEAVY_RAIN":
            case "STORM_RAIN":
                return "🌧";
            case "LIGHT_SNOW":
            case "MODERATE_SNOW":
            case "HEAVY_SNOW":
            case "STORM_SNOW":
                return "❄";
            case "WIND":
                return "💨";
            case "FOG":
                return "🌫️";
            case "LIGHT_HAZE":
            case "MODERATE_HAZE":
            case "HEAVY_HAZE":
                return "🌫️";
            default:
                return "☀️";
        }
    }


    public void setData(List<HourlyChartData> data) {
        if (data == null || data.isEmpty()) {
            this.mData = new ArrayList<>();
        } else {
            // 直接使用数据（HourlyChartData 已包含所有信息）
            this.mData = new ArrayList<>(data);
        }
        updateItemWidth();
        invalidate();
        applyBoundaryLimit();
    }

    private class ChartGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // 拖拽时停止 fling 动画
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }

            mTranslateX -= distanceX;

            // 边界限制（实时）
            applyBoundaryLimit();

            invalidate();
            return true;
        }


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // 停止当前动画
            mScroller.abortAnimation();

            int spanWidth = (mData.size() - 1) * mItemWidth;
            int visibleSpan = (mVisibleItemCount - 1) * mItemWidth;
            float maxX = Math.max(0, spanWidth - visibleSpan);


            // 设置 fling 参数
            // startX: 当前 mTranslateX（注意 Scroller 的 startX 是起始位置）
            // velocityX: 水平速度（像素/秒），需取反因为滑动方向与 translate 方向相反
            mScroller.fling(
                    (int) mTranslateX,
                    0,
                    (int) velocityX,
                    0,
                    (int) -maxX,
                    0,
                    0, 0,
                    0, 0
            );

            // 触发动画
            postInvalidateOnAnimation();
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    private void applyBoundaryLimit() {
        if (mData == null || mData.isEmpty() || mItemWidth <= 0) return;

        // 真实内容总宽度 = (总项数 - 1) * mItemWidth
        int spanWidth = (mData.size() - 1) * mItemWidth;
        // 动态计算可视宽度（与 updateItemWidth 一致）
        int displayCount = Math.min(Math.max(5, mData.size()), 8);
        int visibleSpan = (displayCount - 1) * mItemWidth;

        float maxX = Math.max(0, spanWidth - visibleSpan);

        if (mTranslateX > 0) {
            mTranslateX = 0;
        } else if (mTranslateX < -maxX) {
            mTranslateX = -maxX;
        }
    }

    private static class TemperatureRange {
        float min;
        float max;

        TemperatureRange(float min, float max) {
            this.min = min;
            this.max = max;
        }
    }

    private TemperatureRange calculateTemperatureRange(List<HourlyChartData> data) {
        if (data == null || data.isEmpty()) {
            return new TemperatureRange(0, 10);
        }

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        for (HourlyChartData item : data) {
            float t = item.temperature;
            if (t < min) min = t;
            if (t > max) max = t;
        }

        // 如果所有温度相同，手动扩展范围
        if (min == max) {
            min -= 3;
            max += 3;
        }

        // 添加 padding（例如 8%）
        float range = max - min;
        float padding = range * 0.08f;
        min -= padding;
        max += padding;

        // 对齐到“好看”的数字（如 5 的倍数，或 1 的倍数）
        float niceMin = (float) Math.floor(min);
        float niceMax = (float) Math.ceil(max);

        // 确保至少有 2 度范围（避免除零或压缩过度）
        if (niceMax - niceMin < 2f) {
            niceMin = (float) Math.floor((niceMin + niceMax) / 2f) - 1f;
            niceMax = niceMin + 2f;
        }

        return new TemperatureRange(niceMin, niceMax);
    }
}