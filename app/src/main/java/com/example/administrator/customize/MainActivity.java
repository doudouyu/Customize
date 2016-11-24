package com.example.administrator.customize;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private DragLayout dragLayout;
    private ListView lvLeft;
    private ListView lvMain;
    private ImageView imageView;
    private MyLinerLayout myLinerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dragLayout = (DragLayout) findViewById(R.id.draglayout);

        lvLeft = (ListView) findViewById(R.id.lv_left);
        lvLeft.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,Cheeses.sCheeseStrings));
        lvMain = (ListView) findViewById(R.id.lv_main);
        lvMain.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,Cheeses.NAMES){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView  = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.BLACK);
                return textView;
            }
        });
        imageView = (ImageView) findViewById(R.id.iv_head);
        myLinerLayout = (MyLinerLayout) findViewById(R.id.layout_main);
        myLinerLayout.setDragLayout(dragLayout);

        dragLayout.setOnDragStatuChangeListener(new DragLayout.OnDragStatuChangeListener() {
            @Override
            public void open() {
//                ToastUtils.showToast(MainActivity.this, "打开");
//                lvLeft.setSelection();// 自动到某一个指定的位置
                lvLeft.smoothScrollToPosition(new Random().nextInt(50));  //  0---49 // 平滑 滚动到某一个位置
            }

            @Override
            public void close() {

//                ToastUtils.showToast(MainActivity.this, "关闭");

                // 参数1 :给谁做动画 t
                // 参数2: 属性动画的  属性名称 translationX
//                imageView.setTranslationX();translationX
                ObjectAnimator animator = ObjectAnimator.ofFloat(imageView,"translationX",15);
                //插值器
                animator.setInterpolator(new CycleInterpolator(3));

                animator.setDuration(500);
                animator.start();

            }

            @Override
            public void draging(float percent) {
//                ToastUtils.showToast(MainActivity.this, "正在拖动:"+percent);
//                imageView.setAlpha(1-percent);
                ViewHelper.setAlpha(imageView,1-percent);
            }
        });
    }
}
