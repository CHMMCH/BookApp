package com.chm.book;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.lang.ref.WeakReference;

public class SplashActivity extends AppCompatActivity {

    public static final int TOTAL_TIME = 3000;
    public static final int CODE = 10010;
    public static final int Time_1 = 1000;
    private Button bt_time;
    private SplashActivity.mHandler timeHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //初始化控件
        initView();
        //初始化事件
        initEvent();

        //创建Handler
        timeHandler = new mHandler(this);
        //发送单次消息 传入倒计时间TOTAL_TIME
        Message message = Message.obtain();
        message.what= CODE;
        message.arg1= TOTAL_TIME;
        timeHandler.sendMessage(message);

    }

    private void initEvent() {
        bt_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果点击按钮 直接结束消息发送 跳转页面
                timeHandler.removeMessages(CODE);
                BookListActivity.start(SplashActivity.this);
                finish();
            }
        });
    }


    private void initView() {
        bt_time = findViewById(R.id.bt_time);
    }

    //自定义Handler类
    public static class mHandler extends Handler{
        //弱引用
        WeakReference<SplashActivity>  weakReference;


        public mHandler(SplashActivity acticty) {
            this.weakReference = new WeakReference<>(acticty);
        }

        //接收message
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SplashActivity activity = weakReference.get();
            //判断指令是否正确
            if(msg.what == CODE){
                if(activity != null){

                    //打开页面先显示倒计时
                    activity.bt_time.setText(msg.arg1/Time_1+"秒 点击跳过");


                    //新建消息重复发送，时间-1s
                    Message message = Message.obtain();
                    message.what=10010;
                    message.arg1=msg.arg1 - Time_1;

                    //如果倒计时为0 则跳转页面
                    //否则继续发送，并且用的是延迟发送sendMessageDelayed()，延迟1s
                    if(msg.arg1 > 0 ){
                        sendMessageDelayed(message,Time_1);
                    }else{
                        BookListActivity.start(activity);
                        activity.finish();
                    }
                }
            }
        }
    }
}
