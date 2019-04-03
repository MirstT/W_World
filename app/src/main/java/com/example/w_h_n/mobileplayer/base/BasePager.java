package com.example.w_h_n.mobileplayer.base;

import android.content.Context;
import android.view.View;

/**
 * 基类，公共类
 * 四个子页面都会继承它
 * 构造方法，视图创建，initView（）,孩子强制实现该方法
 * 在initData()，初始化子页面数据
 */

public abstract class BasePager{

    //上下文

    public final Context context;
    public View rootView;
    public boolean isInitData;

    public BasePager(Context context) {
        this.context = context;
        rootView = initView();
    }


    //强制孩子实现，实现特定的效果
    public abstract View initView();
//    private View initView() {
//        return null;
//    }

    //当子页面需要初始化数据，联网请求数据，或者绑定数据的时候需要重写该方法
    public void initData(){
    }
}

