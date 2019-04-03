package com.example.w_h_n.mobileplayer.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.w_h_n.mobileplayer.Pager.AudioPager;
import com.example.w_h_n.mobileplayer.Pager.ExploreWorldPager;
import com.example.w_h_n.mobileplayer.Pager.NetVideoPager;
import com.example.w_h_n.mobileplayer.Pager.VideoPager;
import com.example.w_h_n.mobileplayer.R;
import com.example.w_h_n.mobileplayer.base.BasePager;
import com.example.w_h_n.mobileplayer.utils.FileStorageHelper;

import java.util.ArrayList;



//主页面
public class MainActivity extends FragmentActivity {
    private static final int MY_PERMISSION_REQUEST_CODE = 161;
    private RadioGroup rg_bottopm_tag;
    private ArrayList<BasePager> basePagers;
    private int position;// 选中的位置
    boolean isAllGranted;//应用权限
    private String path = "/storage/emulated/0/DCIM";//媒体库


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideBottomUIMenu();//隐藏虚拟按键，并且全屏
        setContentView(R.layout.activity_main);

        isAllGranted = checkPermissionAllGranted(new String[]//获取权限
                {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.RECORD_AUDIO
                }
        );

        rg_bottopm_tag = (RadioGroup) findViewById(R.id.rg_bottom_tag);
        basePagers = new ArrayList<>();
        basePagers.add(new VideoPager(this)); //添加本地视频页面-0
        basePagers.add(new AudioPager(this)); //添加本地音频页面-1
        basePagers.add(new NetVideoPager(this)); //添加网络视频页面-2
        basePagers.add(new ExploreWorldPager(this)); //添加探索世界页面-3
        rg_bottopm_tag.setOnCheckedChangeListener(new MyOnCheckedChangeListener());//设置点击监听

        // 第1步:如果这些权限全都拥有, 则直接执行代码
        if (isAllGranted) {
            rg_bottopm_tag.check(R.id.rb_video);  //默认选中首页
            copyAndRegisterTest();//测试专用
        }

        //第 2 步:请求权限，一次请求多个权限, 如果其他有权限是已经授予的将会自动忽略掉
        else {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.INTERNET,
                            Manifest.permission.RECORD_AUDIO
                    },
                    MY_PERMISSION_REQUEST_CODE
            );
        }

    }

    private void copyAndRegisterTest() {
        FileStorageHelper.copyFilesFromAssets(this, "audio", path);//copy Assets下的 音频（仅供测试用）
        FileStorageHelper.copyFilesFromAssets(this, "video", path);//copy Assets下的 视频（仅供测试用）
//        register();//注册本地演示文件到媒体库（仅供测试用）
    }

    class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, final int checkedId) {

            switch (checkedId) {
                default:
                    position = 0; //本地视频
                    break;
                case R.id.rb_audio: // 本地音频
                    position = 1;
                    break;
                case R.id.rb_netvideo: // 网络视频
                    position = 2;
                    break;
                case R.id.rb_netaudio: //探索世界
                    position = 3;
                    break;
            }
            setFragmet();
        }
    }

    //把页面添加到fragmentz中
    private void setFragmet() {
        //1.得到FragmentManager()
        FragmentManager manager = getSupportFragmentManager();

        //2.开启事务
        android.support.v4.app.FragmentTransaction ft = manager.beginTransaction();

        //3.替换
        ft.replace(R.id.fl_main_content, new ReplaceFragment(getBasePager()));

        //4.提交事务
        ft.commit();
    }

    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }


    //第 3 步: 申请权限结果返回处理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // 如果所有的权限都授予了, 则执行备份代码
                System.exit(0);
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                openAppDetails();
            }
        }
    }


    //打开 APP 的详情设置
    private void openAppDetails() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("本地视频需要访问“外部存储器”，请到 “应用信息 -> 权限” 中授予！");
        builder.setPositiveButton("手动授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
                System.exit(0);
            }
        });
        builder.setNegativeButton("退出程序", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.exit(0);
            }
        });
        builder.show();
    }


    @SuppressLint("ValidFragment")
    public static class ReplaceFragment extends Fragment {
        private BasePager currPager;

        public ReplaceFragment(BasePager pager) {
            this.currPager = pager;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return currPager.rootView;
        }
    }


    private BasePager getBasePager() {
        BasePager basePager = basePagers.get(position);
        if (basePager != null && !basePager.isInitData) {
            basePager.initData();//联网请求或者数据绑定
            basePager.isInitData = true;
        }
        return basePager;
    }


    private boolean isExit = false;//是否已经退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (position != 0) {//不是第一页面
                position = 0;
                rg_bottopm_tag.check(R.id.rb_video);
                return true;
            } else if (!isExit) {
                isExit = true;
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit = false;
                    }
                }, 2000);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    //注册媒体文件
    private void updateMediaDataBase(String filename) {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;// 获得当前sdk版本
        if (currentApiVersion < 19) {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + filename)));
        } else {
            MediaScannerConnection.scanFile(this, new String[]{filename},
                    null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        }
    }


    //注册本地自带的音视频文件到媒体库
    private void register() {
        updateMediaDataBase("/storage/emulated/0/DCIM/T1.mp4");
        updateMediaDataBase("/storage/emulated/0/DCIM/T2.mp4");
        updateMediaDataBase("/storage/emulated/0/DCIM/T3.mp4");
        updateMediaDataBase("/storage/emulated/0/DCIM/Test_3.mp4");
        updateMediaDataBase("/storage/emulated/0/DCIM/This_is_for_you.mp4");
        updateMediaDataBase("/storage/emulated/0/DCIM/拆分.mp4");
        updateMediaDataBase("/storage/emulated/0/DCIM/推箱子.mp4");
        updateMediaDataBase("/storage/emulated/0/DCIM/16124278-王浩.mp4");
        updateMediaDataBase("/storage/emulated/0/DCIM/移动商务系统设计与开发.mp4");
        updateMediaDataBase("/storage/emulated/0/DCIM/程序.mp4");
        updateMediaDataBase("/storage/emulated/0/DCIM/90_of_us_complete.mp4");
        updateMediaDataBase("/storage/emulated/0/DCIM/Sample.mp4");
        updateMediaDataBase("/storage/emulated/0/DCIM/star.mp4");
        updateMediaDataBase("/storage/emulated/0/DCIM/Legends Never Die.mp3");
        updateMediaDataBase("/storage/emulated/0/DCIM/MKJ - Time.mp3");
        updateMediaDataBase("/storage/emulated/0/DCIM/光良-童话.mp3");
        updateMediaDataBase("/storage/emulated/0/DCIM/光良-童话.txt");
        updateMediaDataBase("/storage/emulated/0/DCIM/其实，我就在你方圆几里.mp3");
        updateMediaDataBase("/storage/emulated/0/DCIM/周杰伦 - 青花瓷.mp3");
        updateMediaDataBase("/storage/emulated/0/DCIM/周杰伦 - 青花瓷.txt");
        updateMediaDataBase("/storage/emulated/0/DCIM/好想你.mp3");
        updateMediaDataBase("/storage/emulated/0/DCIM/好想你.txt");
        updateMediaDataBase("/storage/emulated/0/DCIM/宇多田ヒカル - あなた.mp3");
        updateMediaDataBase("/storage/emulated/0/DCIM/尹姝贻 - 白夜.mp3");
        updateMediaDataBase("/storage/emulated/0/DCIM/尹姝贻 - 白夜.txt");
        updateMediaDataBase("/storage/emulated/0/DCIM/最好的未来.mp3");
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


    @Override
    protected void onRestart() {
        super.onRestart();
        hideBottomUIMenu();
    }


    @Override
    protected void onResume() {
        super.onResume();
        hideBottomUIMenu();
    }
}


