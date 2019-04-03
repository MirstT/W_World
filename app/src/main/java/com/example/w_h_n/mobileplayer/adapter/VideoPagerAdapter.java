package com.example.w_h_n.mobileplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.w_h_n.mobileplayer.R;
import com.example.w_h_n.mobileplayer.domain.MediaItem;
import com.example.w_h_n.mobileplayer.utils.Utils;

import java.util.ArrayList;


//VideoPager适配器

public class VideoPagerAdapter extends BaseAdapter {

    private final boolean isVideo;
    private Context context;
    private final ArrayList<MediaItem> mediaItems;
    private Utils utils;


    public VideoPagerAdapter(Context context, ArrayList<MediaItem> mediaItems, boolean isVideo) {
        this.context = context;
        this.mediaItems = mediaItems;
        this.isVideo = isVideo;
        utils = new Utils();

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
            convertView = View.inflate(context, R.layout.item_video_pager, null);
            viewHoder = new ViewHoder();
            viewHoder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHoder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHoder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            viewHoder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);

            convertView.setTag(viewHoder);
        } else {
            viewHoder = (ViewHoder) convertView.getTag();
        }

        //根据position得到列表中对应位置的数据
        MediaItem mediaItem = mediaItems.get(position);
        viewHoder.tv_name.setText(mediaItem.getName());
        viewHoder.tv_size.setText(android.text.format.Formatter.formatFileSize(context, mediaItem.getSize()));
        viewHoder.tv_time.setText(utils.stringForTime((int) mediaItem.getDuration()));

        if (!isVideo) {
            //音频
            viewHoder.iv_icon.setImageResource(R.drawable.music_default_bg);

        }

        return convertView;
    }

    static class ViewHoder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_time;
        TextView tv_size;
    }
}