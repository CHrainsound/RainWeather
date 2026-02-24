package com.example.rainweather.view;
/**
 * description ：TODO:对横向滑动友好，让24h气温图可以滑动
 * email : 3014386984@qq.com
 * date : 2/21 10:00
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import androidx.core.widget.NestedScrollView;

public class HorizontalScrollFriendlyScrollView extends NestedScrollView {

    private float mLastX, mLastY;
    private int mTouchSlop;

    public HorizontalScrollFriendlyScrollView(Context context) {
        super(context);
        init(context);
    }

    public HorizontalScrollFriendlyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HorizontalScrollFriendlyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = ev.getX();
                mLastY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = Math.abs(ev.getX() - mLastX);
                float deltaY = Math.abs(ev.getY() - mLastY);

                // 如果横向滑动距离 > 纵向，并且超过了 touch slop，就认为是横向滑动
                if (deltaX > mTouchSlop && deltaX > deltaY) {
                    // 让子 View 处理这个事件，自己不拦截
                    return false;
                }
                break;
        }
        // 其他情况，按默认逻辑处理
        return super.onInterceptTouchEvent(ev);
    }
}