package com.example.w_h_n.mobileplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.w_h_n.mobileplayer.R;
import com.example.w_h_n.mobileplayer.domain.MediaItem;
import com.example.w_h_n.mobileplayer.utils.Utils;
import com.squareup.picasso.Picasso;

import org.xutils.x;

import java.util.ArrayList;

/*
VideoPager适配器
 */
public class NetVideoPagerAdapter extends BaseAdapter {

    private Context context;
    private final ArrayList<MediaItem> mediaItems;


    public NetVideoPagerAdapter(Context context, ArrayList<MediaItem> mediaItems) {
        this.context = context;
        this.mediaItems = mediaItems;

    }

    @Override
    public int getCount() {
        return mediaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHoder viewHoder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_net_video_pager, null);
            viewHoder = new ViewHoder();
            viewHoder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHoder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHoder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);

            convertView.setTag(viewHoder);
        } else {
            viewHoder = (ViewHoder) convertView.getTag();
        }

        //根据position得到列表中对应位置的数据
        MediaItem mediaItem = mediaItems.get(position);
        viewHoder.tv_name.setText(mediaItem.getName());
        viewHoder.tv_desc.setText(mediaItem.getDesc());
        //1.使用xUtils3请求图片
//        x.image().bind(viewHoder.iv_icon,mediaItem.getImageUrl());
        //2.使用Glide请求图片
        Glide.with(context).load(mediaItem.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.video_default)
                .error(R.drawable.video_default)
                .into(viewHoder.iv_icon);

//        //3.使用Picasso请求图片
//        Picasso.with(context).load(mediaItem.getImageUrl())
////                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .placeholder(R.drawable.video_default)
//                .error(R.drawable.video_default)
//                .into(viewHoder.iv_icon);


        return convertView;
    }

    static class ViewHoder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_desc;
    }
}