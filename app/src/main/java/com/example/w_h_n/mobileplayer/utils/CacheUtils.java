package com.example.w_h_n.mobileplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.w_h_n.mobileplayer.service.MusicPlayerService;


//缓存工具类
public class CacheUtils {

    //保存播放模式
    public static void putPlaymode(Context context, String key, int values) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("atWanghao", Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(key, values).commit();

    }

    //得到播放模式
    public static int getPlaymode(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("atWanghao", Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, MusicPlayerService.REPEAT_NORMAL);
    }

    //保持数据
    public static void putString(Context context, String key, String values) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("atWanghao", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key, values).commit();
    }

    //得到缓存的数据
    public static String getString(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("atWanghao", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }

}
