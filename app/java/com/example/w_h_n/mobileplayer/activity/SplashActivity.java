package com.example.w_h_n.mobileplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.w_h_n.mobileplayer.R;

public class SplashActivity extends Activity {
    private static final String TAG = SplashActivity.class.getSimpleName();//"SplashActivity"
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideBottomUIMenu();//隐藏虚拟按键，并且全屏
        setContentView(R.layout.activity_splash);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //两秒后才执行到这里
                //执行在主线程中
                startMainActivity();
                Log.e(TAG, "当前线程名称==" + Thread.currentThread().getName());
            }
        }, 1400);
    }


    //跳转到主页面，并且关闭当前页面
    //<activity android:name=".activity.MainActivity" android:launchMode="singleTask"/> <!--注册并设置单例模式-->
    private boolean isStartMain = false; //可代替单例模式，防止多次启动！
    private void startMainActivity() {
        if (!isStartMain) {
            isStartMain = true;
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            //关闭当前页
            finish();
        }
    }

    @Override
    //提前进入
    public boolean onTouchEvent(MotionEvent event) {
        Log.e(TAG, "onTouchEvent==Action" + event.getAction());
        startMainActivity();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);  //提前进入后移除handler
        super.onDestroy();
    }


    //隐藏虚拟按键，并且全屏
    protected void hideBottomUIMenu() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
