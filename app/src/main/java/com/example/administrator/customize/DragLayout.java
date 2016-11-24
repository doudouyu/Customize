package com.example.administrator.customize;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by wangx on 2016/7/2.
 */
public class DragLayout extends FrameLayout {

    private static final String TAG = DragLayout.class.getSimpleName();
    private ViewDragHelper viewDragHelper;
    private ViewGroup mLeftPanel;  //左面版
    private ViewGroup mMainPanel;// 主面板
    private int mWidth;
    private int mHeight;
    private int mRange;

    private OnDragStatuChangeListener onDragStatuChangeListener;
    private DragStatu statu = DragStatu.CLOSE;//默认是关闭状态
    // 3  处理触摸事件的回调
    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        /**
         * 返回值决定  是否可以拖动
         * @param child
         * @param pointerId
         * @return
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;// child == mMainPanel;
        }

        /**
         * 当view 被捕获的时候调用
         * @param capturedChild
         * @param activePointerId
         */
        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }

        /**
         * 返回拖动的位置 位置的修正
         * @param child  还没有真正的移动
         * @param left  建议达到的位置  = 当前+ 瞬间变化量  dx
         * @param dx  瞬间变化量
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            Log.d(TAG, "clampViewPositionHorizontal: 建议达到的位置:" + left + "::当前位置:" + mMainPanel.getLeft() + "::瞬间变化量:" + dx);
            // mMainPanel    0 --->mRange// 主面板的位置修正
            if (child == mMainPanel) {
                left = fixLeft(left);
            }


            return left;
        }

        private int fixLeft(int left) {
            if (left < 0) {
                left = 0;
            } else if (left > mRange) {
                left = mRange;
            }
            return left;
        }

        /**
         * 返回/横向拖动的范围
         * 1. 计算伴随动画的时长 2. 校验 最小敏感度   大于0 即可
         * @param child
         * @return
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return mRange;
        }

        /**
         * 控件的位置 已经 移动
         * 1. 伴随动画  2.  状态更新 3.  添加回调
         * @param changedView
         * @param left
         * @param top
         * @param dx
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
//   如果拖动的是左边的面板   右边的面板跟着向左走
            if (changedView == mLeftPanel) {

                //  有一点变化量 就 放回去
                mLeftPanel.layout(0, 0, 0 + mWidth, 0 + mHeight);

//                mMainPanel.offsetLeftAndRight(dx);
                //  mMainPanel 的左边位置 + dx   =新的左边位置
                int oldLeft = mMainPanel.getLeft();
                int newLeft = oldLeft + dx;

                //  重新修正
                newLeft = fixLeft(newLeft);

                // 重新给  主面板 设置位置
                mMainPanel.layout(newLeft, 0, newLeft + mWidth, 0 + mHeight);

            }

            dispathEvent(mMainPanel.getLeft());

        }

        /**
         * 手指释放的时候调用
         * @param releasedChild   释放在子view
         * @param xvel  水平方向释放时的速度   向右 +  向左  -     0 手指停止后释放
         * @param yvel   垂直方向的速度
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            Log.d(TAG, "onViewReleased: releasedChild = " + releasedChild + "::xvel = " + xvel);
            if (mMainPanel.getLeft() > mRange * 0.5f && xvel == 0) {
                open();
            } else if (xvel > 0) {
                // 向右
                open();
            } else {
                close();
            }
        }


    };

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 初始化
        //1  c创建ViewDragHelper对象

        viewDragHelper = ViewDragHelper.create(this, callback);
    }

    public OnDragStatuChangeListener getOnDragStatuChangeListener() {
        return onDragStatuChangeListener;
    }

    public void setOnDragStatuChangeListener(OnDragStatuChangeListener onDragStatuChangeListener) {
        this.onDragStatuChangeListener = onDragStatuChangeListener;
    }

    /**
     * 1.  伴随动画   主面板的 左边的坐标
     *
     * @param left
     */
    private void dispathEvent(int left) {  // 0  -mRange
        float percent = left * 1.0f / mRange;  //  0 ----1
        animViews(percent);

        // 状态的更新
        DragStatu preStatu = statu;
        statu = updateStatus(percent);
        if (onDragStatuChangeListener != null) {
            onDragStatuChangeListener.draging(percent);

            if (statu != preStatu) {
                //状态改变了
                // 通知用户状态改变
                if (statu == DragStatu.CLOSE) {
                    onDragStatuChangeListener.close();
                } else if (statu == DragStatu.OPEN) {
                    onDragStatuChangeListener.open();
                }
            }
        }

    }

    /**
     * 获取最新的状态
     *
     * @return
     */
    private DragStatu updateStatus(float percent) {
        if (percent == 0) {
            return DragStatu.CLOSE;
        } else if (percent == 1) {
            return DragStatu.OPEN;
        }
        return DragStatu.DRAGING;
    }

    private void animViews(float percent) {
        //  主面板的动画  缩小动画  1.0 ---  0.8  0.8 + (1-percent)*0.2   // 插值器    //   估值器
        // nineoldandroids   //  slidingmenu
//        mMainPanel.setScaleX(  evaluate(percent,1.0f,0.8f));
//        mMainPanel.setScaleY(  evaluate(percent,1.0f,0.8f));
        ViewHelper.setScaleX(mMainPanel, evaluate(percent, 1.0f, 0.8f));
        ViewHelper.setScaleY(mMainPanel, evaluate(percent, 1.0f, 0.8f));

        //侧边面板的动画  缩放 0.7 - 1.0    平移
        ViewHelper.setScaleX(mLeftPanel, evaluate(percent, 0.7f, 1.0f));
        ViewHelper.setScaleY(mLeftPanel, evaluate(percent, 0.7f, 1.0f));

        //平移
        ViewHelper.setTranslationX(mLeftPanel, evaluate(percent, -mWidth * 0.5f, 0));


        //背景 滤镜
        getBackground().setColorFilter((Integer) evaluateColor(percent, 0xFF000000, 0x00000000)
                , PorterDuff.Mode.SRC_OVER);
    }

    /**
     * 颜色的值
     *
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public Object evaluateColor(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int) ((startA + (int) (fraction * (endA - startA))) << 24) |
                (int) ((startR + (int) (fraction * (endR - startR))) << 16) |
                (int) ((startG + (int) (fraction * (endG - startG))) << 8) |
                (int) ((startB + (int) (fraction * (endB - startB))));
    }

    /**
     * 估值器
     *
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        // 多次调用
        boolean b = viewDragHelper.continueSettling(true);
        // 是否继续触发动画
        if (b) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void open(boolean isSmooth) {
        if (isSmooth) {
            boolean b = viewDragHelper.smoothSlideViewTo(mMainPanel, mRange, 0);
            // 返回值决定是否触发动画
            if (b) {
                //执行动画
                ViewCompat.postInvalidateOnAnimation(this);   // ---->多次调用 computeScroll
            }
        } else {
            mMainPanel.layout(mRange, 0, mRange + mWidth, 0 + mHeight);
        }
    }

    /**
     * 打开
     */
    private void open() {
        open(true);//默认平滑打开
    }

    public void close(boolean isSmooth) {
        if (isSmooth) {
            boolean b = viewDragHelper.smoothSlideViewTo(mMainPanel, 0, 0);
            // 返回值决定是否触发动画
            if (b) {
                //执行动画
                ViewCompat.postInvalidateOnAnimation(this);   // ---->多次调用 computeScroll
            }
        } else {
            // 主面板位置
            mMainPanel.layout(0, 0, 0 + mWidth, 0 + mHeight);
        }
    }

    /**
     * 关闭
     */
    public void close() {

        close(true);//默认平滑关闭
    }


    //  2. 事件的拦截  & 事件的处理

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        // 决定是否拦截事件
        // 转交给viewdragHelper
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 处理触摸事件
//        转交给ViewDragHelper
        // ctrl + alt + T
        try {
            viewDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    //测量 子view
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 只有改变的时候才会调用
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        // 获取拖动的范围   60%
        mRange = (int) (mWidth * 0.6);

    }

    // xml   填充为view 的时候调用
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();


        mLeftPanel = (ViewGroup) findViewById(R.id.layout_left);
        mMainPanel = (ViewGroup) findViewById(R.id.layout_main);

    }

    public DragStatu getStatu() {
        return statu;
    }

    public void setStatu(DragStatu statu) {
        this.statu = statu;
    }

    enum DragStatu {
        OPEN, CLOSE, DRAGING
    }

    public interface OnDragStatuChangeListener {
        void open();

        void close();

        void draging(float percent);
    }
}
