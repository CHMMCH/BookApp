package com.chm.book;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class BookListActivity extends AppCompatActivity {

    private static final String TAG = "BookListActivity";

    String url = "http://www.imooc.com/api/teacher?type=10";
    private ListView mListView;
    private AsyncHttpClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        mListView = findViewById(R.id.lv_book);

        //动态申请读写权限  6.0以上版本必须
        int permisson = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.d(TAG, "onCreate: " + permisson);
        if (permisson == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        //创建开源框架AsyncHttpClient对象 传入url 直接进行网络访问
        mClient = new AsyncHttpClient();
        mClient.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //拿到url传回的数据
                String result = new String(responseBody);
                //创建gson对象 并且解析传回的json数据
                Gson gson = new Gson();
                BookListResult bookListResult = gson.fromJson(result,BookListResult.class);
                //拿到二级数据 主要数据
                List<BookListResult.DataBean> books = bookListResult.getData();

                //传到Adapetr  需要传入Context 和 二级list数据 让ListViewAdapter进行适配
                mListView.setAdapter(new BookListAdapter(BookListActivity.this,books));

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });



    }

    //封装静态启动方法
    public static void start(Context context){
        Intent intent = new Intent(context,BookListActivity.class);
        context.startActivity(intent);
    }



    class BookListAdapter extends BaseAdapter {

        private List<BookListResult.DataBean> mBooks = new ArrayList<>();
        private Context context;

        public BookListAdapter(Context context, List<BookListResult.DataBean> books) {
            this.context = context;
            this.mBooks = books;
        }

        //Adapter内容

        @Override
        public int getCount() {
            return mBooks.size();
        }

        @Override
        public Object getItem(int position) {
            return mBooks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            //利用自定义缓存类进行优化ListView显示效果
            ViewHolder viewHolder = new ViewHolder();
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.item_book_listview, null);
                viewHolder.mButton = convertView.findViewById(R.id.bt_click_book);
                viewHolder.mNameTextView = convertView.findViewById(R.id.tv_book_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //显示书本列表
            viewHolder.mNameTextView.setText(mBooks.get(position).getBookname());
            viewHolder.mButton.setText(R.string.click_download);

            //创建书籍保存路径
            final String path = Environment.getExternalStorageDirectory() + "/chm/" + mBooks.get(position).getBookname() + ".txt";
            final File file = new File(path);
            viewHolder.mButton.setText(file.exists() ? R.string.click_open : R.string.click_download);

            //点击下载功能和点击打开功能，都在同一个按钮上
            final ViewHolder finalViewHolder = viewHolder;
            viewHolder.mButton.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {

                    //判断文件是否创建了 是直接打开 否则下载
                    if (file.exists()){
                        //打开书籍功能 跳转页面
                        BookActivity.start(BookListActivity.this,path);
                    }else{
                        //下载功能
                        //Http下载时候 不用压缩编码 可显示正常下载进度
                        mClient.addHeader("Accept-Encoding","identity");

                        //开源框架 下载 和 创建（保存）文件回调接口
                        mClient.get(mBooks.get(position).getBookfile(), new FileAsyncHttpResponseHandler(file) {
                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                                finalViewHolder.mButton.setText(R.string.download_failed);
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, File file) {
                                Log.d(TAG, "onSuccess: 成功下载");
                                finalViewHolder.mButton.setText(R.string.click_open);
                            }

                            @Override
                            public void onProgress(long bytesWritten, long totalSize) {
                                super.onProgress(bytesWritten, totalSize);
                                finalViewHolder.mButton.setText(bytesWritten*100/totalSize+"%");
                            }
                        });
                    }

                }
            });
            return convertView;
        }

        //Adapter缓存类
        class ViewHolder {
            private TextView mNameTextView;
            private Button mButton;
        }
    }


}
