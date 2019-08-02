package com.chm.book.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chm.book.R;
import com.chm.book.view.BookPageBezierHelper;
import com.chm.book.view.BookPageView;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 作者: CHM
 * 创建时间: 2019/7/31 15:50
 */
public class BookActivity extends AppCompatActivity {

    private static final String TAG = "BookActivity";
    public static final String FILE_PATH = "file_name";
    public static final String BOOKMARK = "bookmark";
    private TextView mProgressTextView;
    private BookPageView mBookPageView;
    private View mSettingView;
    private RecyclerView mRecylerView;
    private int mCurrentLength;
    private int width;
    private int height;
    private LinearLayoutManager linearLayoutManager;
    private BookPageBezierHelper helper;
    private Bitmap currentPageBitmap;
    private Bitmap nextPageBitmep;
    private DisplayMetrics displayMetrics;
    private boolean backId = true;
    private TextToSpeech mTTs;
    private SeekBar mSeekbar;
    private String filepath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //看书的全屏效果设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_book);


        initView();

        //打开书本方法
        openBookProgress(R.drawable.book_bg, 0);


        //触摸菜单列表弹出回调接口
        mBookPageView.setUserTouchSettingListener(new BookPageView.userTouchSetting() {
            @Override
            public void userTouch() {
                mSettingView.setVisibility(mSettingView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });
        //触摸屏幕任何位置，回收菜单列表回调接口
        mBookPageView.setUserCloseSetting(new BookPageView.userCloseSetting() {
            @Override
            public void userColse() {
                mSettingView.setVisibility(View.GONE);
                mSeekbar.setVisibility(View.GONE);
            }
        });

        //设置RecyclerView
        List<String> settingStr = new ArrayList<>();
        settingStr.add("添加书签");
        settingStr.add("读取书签");
        settingStr.add("切换背景");
        settingStr.add("语音朗读");
        settingStr.add("跳转进度");

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecylerView.setLayoutManager(linearLayoutManager);
        mRecylerView.setAdapter(new HorizintalAdapter(this, settingStr));

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int filepro = (int) new File(filepath).length();
                 openBookProgress(R.drawable.book_bg,seekBar.getProgress() *filepro / 100);
            }
        });


    }

    //绘制页面方法添加进度方法，并且打包
    private void openBookProgress(int backgroundId, int progress) {
        //获取屏幕尺寸
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;

        //开源框架类 拿到屏幕尺寸
        helper = new BookPageBezierHelper(width, height, progress);
        mBookPageView.setBookPageBezierHelper(helper);

        //当前页面和下张页面
        currentPageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        nextPageBitmep = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBookPageView.setBitmaps(currentPageBitmap, nextPageBitmep);

        //设置背景
        helper.setBackground(this, backgroundId);

        //设置进度
        helper.setOnProgressChangedListener(new BookPageBezierHelper.OnProgressChangedListener() {
            @Override
            public void setProgress(int currentLength, int totalLength) {
                mCurrentLength = currentLength;
                float progress = mCurrentLength * 100 / totalLength;
                mProgressTextView.setText(String.format("%s%%", progress));
            }
        });


        if (getIntent() != null) {

            if (!TextUtils.isEmpty(filepath)) {
                try {
                    //打开书本方法  打开txt
                    helper.openBook(filepath);
                    //绘制页面方法
                    helper.draw(new Canvas(currentPageBitmap));
                    //用书签必须重写刷新
                    mBookPageView.invalidate();
                } catch (IOException e) {
                    e.printStackTrace();
                    //TODO 找不到书籍 获取失败
                }
            } else {
                //TODO 找不到书籍 获取失败
            }
        } else {
            //TODO 找不到书籍 获取失败
        }


    }


    private void initView() {
        filepath = getIntent().getStringExtra(FILE_PATH);
        mSettingView = findViewById(R.id.setting_view);
        mBookPageView = findViewById(R.id.book_page_view);
        mProgressTextView = findViewById(R.id.tv_progress);
        mRecylerView = findViewById(R.id.setting_RecyclerView);
        mSeekbar = findViewById(R.id.seekBar_progress);
    }

    //页面跳转方法 Intent带上书本地址
    public static void start(Context context, String filepath) {
        Intent intent = new Intent(context, BookActivity.class);
        intent.putExtra(FILE_PATH, filepath);
        context.startActivity(intent);
    }


    //设置列表的REcyclerView适配器
    private class HorizintalAdapter extends RecyclerView.Adapter {
        private Context mContext;
        private List<String> mData = new ArrayList<>();

        public HorizintalAdapter(Context context, List<String> settingStr) {
            this.mContext = context;
            this.mData = settingStr;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            TextView textView = new TextView(mContext);
            return new ViewHodler(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
            TextView textView = (TextView) viewHolder.itemView;
            textView.setWidth(250);
            textView.setHeight(200);
            textView.setTextSize(16);
            textView.setTextColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            textView.setText(mData.get(i));


            final SharedPreferences sharedPreferences = getSharedPreferences("chm", MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            //功能列表
            textView.setOnClickListener(new View.OnClickListener() {
                private int lastlength;

                @Override
                public void onClick(View v) {
                    switch (i) {
                        case 0:
                            //存储书签页
                            //利用SharedPreferences
                            editor.putInt(BOOKMARK, mCurrentLength);
                            editor.apply();
                            Toast.makeText(mContext, "书签保存成功", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            //读取书签
                            lastlength = sharedPreferences.getInt(BOOKMARK, 0);
                            openBookProgress(R.drawable.book_bg, lastlength);
                            Toast.makeText(mContext, "书签读取成功", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            openBookProgress(R.drawable.book_bg2, lastlength);
                            Toast.makeText(mContext, "切换背景成功", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            if (mTTs == null) {
                                mTTs = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
                                    @Override
                                    public void onInit(int status) {
                                        if (status == TextToSpeech.SUCCESS) {
                                            //默认中文
                                            int result = mTTs.setLanguage(Locale.CHINA);
                                            //判断数据丢失或者不支持 即失败情况
                                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                                Log.d(TAG, "onInit: 语音获取失败");
                                                Uri uri = Uri.parse("http://acj2.pc6.com/pc6_soure/2017-6/com.iflytek.vflynote_208.apk");
                                                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                                                startActivity(intent);

                                            } else {
                                                mTTs.speak(helper.getTTsText(),TextToSpeech.QUEUE_FLUSH,null);
                                            }
                                        }
                                    }
                                });
                            }else{
                                if(mTTs.isSpeaking()){
                                    mTTs.stop();
                                }else{
                                    mTTs.speak(helper.getTTsText(),TextToSpeech.QUEUE_FLUSH,null);
                                }
                            }
                            break;
                        case 4:
                            mSeekbar.setVisibility(mSeekbar.getVisibility() == View.GONE  ? View.VISIBLE : View.GONE);
                            break;

                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }



        public class ViewHodler extends RecyclerView.ViewHolder {
            private TextView mTextView;
            public ViewHodler(@NonNull TextView itemView) {
                super(itemView);
                this.mTextView = itemView;
            }
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTTs != null){
            mTTs.shutdown();
        }
    }
}
