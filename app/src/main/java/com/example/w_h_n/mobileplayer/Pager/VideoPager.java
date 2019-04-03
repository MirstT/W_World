package com.example.w_h_n.mobileplayer.Pager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.w_h_n.mobileplayer.R;
import com.example.w_h_n.mobileplayer.activity.SystemVideoPlayer;
import com.example.w_h_n.mobileplayer.adapter.VideoPagerAdapter;
import com.example.w_h_n.mobileplayer.base.BasePager;
import com.example.w_h_n.mobileplayer.domain.MediaItem;
import com.example.w_h_n.mobileplayer.utils.LogUtil;

import java.util.ArrayList;

import android.os.Handler;

//本地视频页面

public class VideoPager extends BasePager {
    private ListView listview;
    private TextView tv_nomedia;
    private TextView tv_loading;
    private ProgressBar pb_loading;
    private VideoPagerAdapter videoPagerAdapter;
    //装数据集合
    private ArrayList<MediaItem> mediaItems;

    public VideoPager(Context context) {
        super(context);
    }


    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mediaItems != null && mediaItems.size() > 0) {
                //有数据
                //设置适配器
                videoPagerAdapter = new VideoPagerAdapter(context, mediaItems,true);
                listview.setAdapter(videoPagerAdapter);
                //文本隐藏
                tv_nomedia.setVisibility(View.GONE);
            } else {
                //没有数据
                //文本显示
                tv_nomedia.setVisibility(View.VISIBLE);
            }
            //rogressBar隐藏
            tv_loading.setVisibility(View.GONE);
            pb_loading.setVisibility(View.GONE);
        }
    };


    public View initView() {
        LogUtil.e("本地视频页面被初始化了");
        View view = View.inflate(context, R.layout.video_pager, null);
        listview = (ListView) view.findViewById(R.id.listview);
        tv_nomedia = (TextView) view.findViewById(R.id.tv_nomedia);
        tv_loading = (TextView) view.findViewById(R.id.tv_loading);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);


        //设置ListView的Item的点击事件
        listview.setOnItemClickListener(new MyOnItemClickListener());
        return view;
    }

    class MyOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            MediaItem mediaItem = mediaItems.get(position);

//            Toast.makeText(context, "mediaItem==" + mediaItem.toString(), Toast.LENGTH_SHORT).show();

            //1.调起系统所有的播放-隐式意图
//            Intent intent = new Intent();
//            intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/*");
//            context.startActivity(intent);

//            //2.调用自己写的播放器-显式意图
//            Intent intent = new Intent(context,SystemVideoPlayer.class);
//            intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/*");
//            context.startActivity(intent);

            //3.传递列表数据-对象-序列化
            Intent intent = new Intent(context,SystemVideoPlayer.class);
                                 Bundle bundle = new Bundle();
            bundle.putSerializable("videolist",mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position",position);
            context.startActivity(intent);
        }
    }

    public void initData() {
        super.initData();
        LogUtil.e("本地视频页面的数据被初始化了");
        getDataFormLocal();
    }


    /**
     * 从本地的sdcard得到数据
     * //1.遍历sdcard，后缀名(太慢)
     * //2.从内容提供者里面获取
     * //3. 动态权限获取
     */
    private void getDataFormLocal() {
        new Thread() {

            @Override
            public void run() {

                try {
                    Thread.sleep(2000);//第一次加载时的等待
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                super.run();
                mediaItems = new ArrayList<>();
                ContentResolver resolver = context.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Video.Media.DISPLAY_NAME,//视频文件在sdcard的名称
                        MediaStore.Video.Media.DURATION,//视频总时长
                        MediaStore.Video.Media.SIZE,//视频文件大小
                        MediaStore.Video.Media.DATA,//视频的绝对地址
                        MediaStore.Video.Media.ARTIST,//歌曲的演唱者
                };
                Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {

                        MediaItem mediaItem = new MediaItem();

                        mediaItems.add(mediaItem);

                        String name = cursor.getString(0);//视频名称
                        mediaItem.setName(name);

                        long duration = cursor.getLong(1);//视频时长
                        mediaItem.setDuration(duration);

                        long size = cursor.getLong(2);//视频文件大小
                        mediaItem.setSize(size);

                        String data = cursor.getString(3);//视频播放地址
                        mediaItem.setData(data);

                        String artist = cursor.getString(4);//艺术家
                        mediaItem.setArtist(artist);
                    }
                    cursor.close();
                }
                //发消息 避免curso为null时候的bug
                //Handler
                handler.sendEmptyMessage(10);
            }

        }.start();
    }
}
