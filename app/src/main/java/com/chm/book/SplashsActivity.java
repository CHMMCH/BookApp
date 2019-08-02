package com.chm.book;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.lang.ref.WeakReference;

public class SplashsActivity extends AppCompatActivity {

    private Button bt_time;
    private Handler mHandler;
    private TimeAsync timeAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashs);

        initView();

        initEvent();

        timeAsync = new TimeAsync(this);
        timeAsync.execute(3);


    }

    private void initEvent() {
        bt_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeAsync.cancel(true);
                Intent intent = new Intent(SplashsActivity.this, BookListActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initView() {
        bt_time = findViewById(R.id.bt_time);
    }


    public class TimeAsync extends AsyncTask<Integer,Integer,Integer>{

        WeakReference<SplashsActivity> weakReference;

        public TimeAsync(SplashsActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }


        @Override
        protected Integer doInBackground(Integer... integers) {
            int time = integers[0];
            for(int i=0;time>i;time--){

                if(isCancelled()){
                    break;
                }

                publishProgress(time);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int time = values[0];
            bt_time.setText(time+"秒 点击跳过");
        }


        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            Intent intent = new Intent(SplashsActivity.this, BookListActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
