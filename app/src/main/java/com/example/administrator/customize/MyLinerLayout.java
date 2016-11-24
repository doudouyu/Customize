package com.example.administrator.customize;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by wangx on 2016/7/3.
 */
public class MyLinerLayout extends LinearLayout {
    private DragLayout dragLayout;

    public MyLinerLayout(Context context) {
        super(context);
    }

    public MyLinerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 侧滑面板打开的时候  全部拦截事件    true
        if (dragLayout!= null && dragLayout.getStatu()== DragLayout.DragStatu.OPEN){
            return  true;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 手指抬起的时候   关闭
        if (dragLayout != null && dragLayout.getStatu()== DragLayout.DragStatu.CLOSE){
            return super.onTouchEvent(event);
        }else{

            if (event.getAction() == MotionEvent.ACTION_UP){
                //关闭
                dragLayout.close();
            }

            return  true;//super.onTouchEvent(event);//  move up
        }


    }

    public void setDragLayout(DragLayout dragLayout) {
        this.dragLayout = dragLayout;
    }
}
