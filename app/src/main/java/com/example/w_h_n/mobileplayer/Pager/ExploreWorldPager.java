package com.example.w_h_n.mobileplayer.Pager;

import android.content.Context;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.w_h_n.mobileplayer.R;
import com.example.w_h_n.mobileplayer.adapter.ExploreWorldPagerAdapter;
import com.example.w_h_n.mobileplayer.base.BasePager;
import com.example.w_h_n.mobileplayer.domain.ExploreWorldPagerData;
import com.example.w_h_n.mobileplayer.utils.CacheUtils;
import com.example.w_h_n.mobileplayer.utils.Constants;
import com.example.w_h_n.mobileplayer.utils.LogUtil;
import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

//探索世界页面


public class ExploreWorldPager extends BasePager {

    @ViewInject(R.id.listview)
    private ListView mListView;

    @ViewInject(R.id.tv_nonet)
    private TextView tv_nonet;

    @ViewInject(R.id.pb_loading)
    private ProgressBar pb_loading;

    //页面的数据
    private List<ExploreWorldPagerData.ListEntity> datas;

    private ExploreWorldPagerAdapter adapter;

    public ExploreWorldPager(Context context) {
        super(context);
    }

    public View initView() {
        View view = View.inflate(context, R.layout.exploreworld_pager, null);
        x.view().inject(this, view);
        return view;
    }

    public void initData() {
        super.initData();
        LogUtil.e("探索世界页面的数据被初始化了");
        //联网
        getDataFromNet();
    }

    private void getDataFromNet() {
        RequestParams params = new RequestParams(Constants.ALL_RES_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("请求数据成功==" + result);
                //保存数据
                CacheUtils.putString(context, Constants.ALL_RES_URL, result);
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("请求数据失败==" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled==" + cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");
            }
        });
    }

    //解析json数据和显示数据
    //解析数据：1.GsonFormat生成bean对象；2.用gson解析数据
    private void processData(String json) {
        //解析数据
        ExploreWorldPagerData data = parsedJson(json);
        datas = data.getList();

        if (datas != null && datas.size() > 0) {
            //有数据
            tv_nonet.setVisibility(View.GONE);
            //设置适配器
            adapter = new ExploreWorldPagerAdapter(context, datas);
            mListView.setAdapter(adapter);
        } else {
            tv_nonet.setText("没有对应的数据...");
            //没有数据
            tv_nonet.setVisibility(View.VISIBLE);
        }
        pb_loading.setVisibility(View.GONE);
    }


    //Gson解析数据
    private ExploreWorldPagerData parsedJson(String json) {
        return new Gson().fromJson(json, ExploreWorldPagerData.class);
    }
}

