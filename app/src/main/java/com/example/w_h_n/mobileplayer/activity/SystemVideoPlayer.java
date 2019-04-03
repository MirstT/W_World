package com.example.w_h_n.mobileplayer.activity;

//系统播放器

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.w_h_n.mobileplayer.R;
import com.example.w_h_n.mobileplayer.domain.MediaItem;
import com.example.w_h_n.mobileplayer.utils.LogUtil;
import com.example.w_h_n.mobileplayer.utils.Utils;
import com.example.w_h_n.mobileplayer.view.VideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SystemVideoPlayer extends Activity implements View.OnClickListener {

    private boolean isUseSystem = true;

    private static final int PROGRESS = 1; //视频进度的更新
    private static final int HIDE_MEDIACONTROLLER = 2;//隐藏控制面板
    private static final int FULL_SCREEN = 1;//全屏
    private static final int DEFAULT_SCREEN = 2;//默认屏幕
    private static final int SHOW_SPEED = 3;//显示网速

    private VideoView videoview;
    private Uri uri;
    private LinearLayout llTop;
    private TextView tvName;
    private ImageView ivBattery;
    private TextView tvSystemTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private Button btnSwichPlayer;
    private LinearLayout llBottom;
    private RelativeLayout media_controller;
    private TextView tvCurrentTime;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnExit;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
    private Button btnVideoSwitchScreen;
    private TextView tv_buffer_netspeed;
    private LinearLayout ll_buffer;
    private TextView tv_loading_netspeed;
    private LinearLayout ll_loading;

    private Utils utils;
    private MyReceiver receiver;    //监听电量变化的广播
    private ArrayList<MediaItem> mediaItems;//传入进来的视频列表
    private int position;//要播放的列表中的具体位置
    private GestureDetector detector; //定义手势识别器
    private boolean isshowMediaController = false;//是否显示控制面板
    private boolean isFullScreen = false;//是否全屏

    private int screenWidth = 0;//屏幕的宽
    private int screenHeight = 0;//屏幕的高
    private int videoWidth;//真实视频的宽
    private int videoHeight;//真实视频的高

    private AudioManager am;//调节声音
    private int currentVoice;//当前音量
    private int maxVoice;//最大音量 0~15
    private boolean isMute = false;//是否静音

    private boolean isNetUri;//是否时网络uri
    private int preCurrentPosition; //上一次的播放进度


    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2018-10-14 19:12:29 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        hideBottomUIMenu();//隐藏虚拟按键，并且全屏
        setContentView(R.layout.activity_system_video_player);

        videoview = (VideoView) findViewById(R.id.videoview);
        llTop = (LinearLayout) findViewById(R.id.ll_top);
        tvName = (TextView) findViewById(R.id.tv_name);
        ivBattery = (ImageView) findViewById(R.id.iv_battery);
        tvSystemTime = (TextView) findViewById(R.id.tv_system_time);
        btnVoice = (Button) findViewById(R.id.btn_voice);
        seekbarVoice = (SeekBar) findViewById(R.id.seekbar_voice);
        btnSwichPlayer = (Button) findViewById(R.id.btn_swich_player);
        llBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        tvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        seekbarVideo = (SeekBar) findViewById(R.id.seekbar_video);
        tvDuration = (TextView) findViewById(R.id.tv_duration);
        btnExit = (Button) findViewById(R.id.btn_exit);
        btnVideoPre = (Button) findViewById(R.id.btn_video_pre);
        btnVideoStartPause = (Button) findViewById(R.id.btn_video_start_pause);
        btnVideoNext = (Button) findViewById(R.id.btn_video_next);
        btnVideoSwitchScreen = (Button) findViewById(R.id.btn_video_switch_screen);
        media_controller = (RelativeLayout) findViewById(R.id.media_controller);
        tv_buffer_netspeed = (TextView) findViewById(R.id.tv_buffer_netspeed);
        ll_buffer = (LinearLayout) findViewById(R.id.ll_buffer);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
        tv_loading_netspeed = (TextView) findViewById(R.id.tv_loading_netspeed);

        btnVoice.setOnClickListener(this);
        btnSwichPlayer.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnVideoPre.setOnClickListener(this);
        btnVideoStartPause.setOnClickListener(this);
        btnVideoNext.setOnClickListener(this);
        btnVideoSwitchScreen.setOnClickListener(this);

        seekbarVoice.setMax(maxVoice);//最大音量和seekBar关联
        seekbarVoice.setProgress(currentVoice);//设置当前进度-当前音量

        handler.sendEmptyMessage(SHOW_SPEED);//开始更新网速
    }


    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2018-10-14 19:12:29 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == btnVoice) {
            // Handle clicks for btnVoice
            isMute = !isMute;
            updataVoice(currentVoice, isMute);
        } else if (v == btnSwichPlayer) {
            // Handle clicks for btnSwichPlayer
            showSwichPlayerDialog();
        } else if (v == btnExit) {
            // Handle clicks for btnExit
            finish();
        } else if (v == btnVideoPre) {
            // Handle clicks for btnVideoPre
            playPreVideo();
        } else if (v == btnVideoStartPause) {
            // Handle clicks for btnVideoStartPause
            startAndPause();
        } else if (v == btnVideoNext) {
            // Handle clicks for btnVideoNext
            playNextVideo();
        } else if (v == btnVideoSwitchScreen) {
            // Handle clicks for btnVideoSwitchScreen
            setFullScreenAndDefault();
        }
        handler.removeMessages(HIDE_MEDIACONTROLLER);
        handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 3500);
    }


    private void showSwichPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("系统播放器提示");
        builder.setMessage("是否切换至强力解码器播放（万能播放）?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startVitamioPlayer();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }


    private void startAndPause() {
        if (videoview.isPlaying()) {
            //视频在播放-设置暂停
            videoview.pause();
            //按钮状态设置播放
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_start_selector);
        } else {
            //视频不在播放-设置播放
            videoview.start();
            //按钮状态设置暂停
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_pause_selector);
        }
    }


    //播放上一个视频
    private void playPreVideo() {
        if (mediaItems != null & mediaItems.size() > 0) {
            //播放上一个
            position--;
            if (position >= 0) {
                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());
                videoview.setVideoPath(mediaItem.getData());
                setButtonState();//设置按钮状态
            }

        } else if (uri != null) {
            //设置按钮状态 上一个和下一个按钮设置灰色并且不可以点击
            setButtonState();
        }
    }


    //播放下一个视频
    private void playNextVideo() {
        if (mediaItems != null & mediaItems.size() > 0) {
            //播放下一个
            position++;
            if (position < mediaItems.size()) {
                ll_loading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());
                videoview.setVideoPath(mediaItem.getData());
                setButtonState();//设置按钮状态
            }
        } else if (uri != null) {
            //设置按钮状态 上一个和下一个按钮设置灰色并且不可以点击
            setButtonState();
        }
    }


    private void setButtonState() {
        if (mediaItems != null && mediaItems.size() > 0) {
            if (mediaItems.size() == 1) {
                setEnable(false);
            } else if (mediaItems.size() == 2) {
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);
                } else if (position == mediaItems.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                } else {
                    setEnable(true);
                }
            } else {
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);
                    btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                    btnVideoNext.setEnabled(true);
                } else if (position == mediaItems.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                    btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                    btnVideoPre.setEnabled(true);
                } else {
                    setEnable(true);
                }
            }
        } else if (uri != null) {
            //两个按钮设置灰色
            setEnable(false);
        }
    }


    private void setEnable(boolean isEnable) {
        if (isEnable) {
            btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            btnVideoPre.setEnabled(true);
            btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
            btnVideoNext.setEnabled(true);
        } else {
            //两个按钮设置灰色
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnVideoPre.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoNext.setEnabled(false);
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_SPEED://显示网速
                    //得到网络速度
                    String netSpeed = utils.getNetSpeed(SystemVideoPlayer.this);
                    //显示网络速度
                    tv_loading_netspeed.setText("玩命加载中..." + netSpeed);
                    tv_buffer_netspeed.setText("缓冲中..." + netSpeed);
                    //两秒钟调用一次
                    handler.removeMessages(SHOW_SPEED);
                    handler.sendEmptyMessageDelayed(SHOW_SPEED, 2000);
                    break;
                case HIDE_MEDIACONTROLLER://隐藏控制面板
                    hideMediaController();
                    break;
                case PROGRESS:
                    //1.得到当前的视频的播放进度
                    int currentPosition = videoview.getCurrentPosition();
                    //2.SeekBar.setProgressBar(当前进度);
                    seekbarVideo.setProgress(currentPosition);
                    //更新文本播放进度
                    tvCurrentTime.setText(utils.stringForTime(currentPosition));
                    //设置系统时间
                    tvSystemTime.setText(getSystemTime());
                    //缓存进度的更新
                    if (isNetUri) {
                        //只有网络资源有缓冲效果
                        int buffer = videoview.getBufferPercentage();
                        int totalBuffer = buffer * seekbarVideo.getMax();
                        int secondaryProgress = totalBuffer / 100;
                        seekbarVideo.setSecondaryProgress(secondaryProgress);
                    } else {
                        //本地视频没有缓冲效果
                        seekbarVideo.setSecondaryProgress(0);
                    }
                    //监听卡
                    if (!isUseSystem && videoview.isPlaying()) {
                        if (videoview.isPlaying()) {
                            int buffer = currentPosition - preCurrentPosition;
                            if (buffer < 500) {
                                //视频卡了
                                ll_buffer.setVisibility(View.VISIBLE);
                            } else {
                                //视频不卡了
                                ll_buffer.setVisibility(View.GONE);
                            }
                        } else {
                            ll_buffer.setVisibility(View.GONE);
                        }
                    }
                    preCurrentPosition = currentPosition;
                    //3.每秒更新一次
                    handler.removeMessages(PROGRESS);
                    handler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
            }
        }
    };


    //得到系统时间
    public String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//初始化父类
        hideBottomUIMenu();//隐藏虚拟按键，并且全屏
        LogUtil.e("onCreate--");
        initData();
        findViews();
        setListener();
        getData();
        setData();
        //设置控制面板
        // videoview.setMediaController(new MediaController(this));
    }


    private void setData() {
        if (mediaItems != null && mediaItems.size() > 0) {
            MediaItem mediaItem = mediaItems.get(position);
            tvName.setText(mediaItem.getName()); //设置视频名称
            isNetUri = utils.isNetUri(mediaItem.getData());
            videoview.setVideoPath(mediaItem.getData());
        } else if (uri != null) {
            tvName.setText(uri.toString());//设置视频名称
            isNetUri = utils.isNetUri(uri.toString());
            videoview.setVideoURI(uri);
        } else {
            Toast.makeText(SystemVideoPlayer.this, "没有传递数据！！！", Toast.LENGTH_SHORT).show();
        }
        setButtonState();
    }


    private void getData() {
        //得到播放地址
        uri = getIntent().getData();//文件夹，图片浏览器，QQ空间
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);
    }


    private void initData() {
        utils = new Utils();
        //注册电量广播
        receiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        //当电量发生变化的时候，发这个广播
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, intentFilter);
        //实例化手势识别器，并且重写双击，单击，长按
        detector = new GestureDetector(this, new MySimpleOnGestureListener());

        //得到屏幕的宽和高
        //过时方法
        // screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        //screenHeight = getWindowManager().getDefaultDisplay().getHeight();

        //得到屏幕宽和高的最新方式
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        //得到音量
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            //Toast.makeText(SystemVideoPlayer.this,"长按", Toast.LENGTH_SHORT).show();
            startAndPause();
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Toast.makeText(SystemVideoPlayer.this, "双击", Toast.LENGTH_SHORT).show();
            setFullScreenAndDefault();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // Toast.makeText(SystemVideoPlayer.this,"单击", Toast.LENGTH_SHORT).show();
            if (isshowMediaController) {
                //隐藏
                hideMediaController();
                //把隐藏消息移除
                handler.removeMessages(HIDE_MEDIACONTROLLER);
            } else {
                //显示
                showMediaControllerSendMessage();
            }
            return super.onSingleTapConfirmed(e);
        }
    }

    private void setFullScreenAndDefault() {
        if (isFullScreen) {
            //默认
            setVideoType(DEFAULT_SCREEN);
        } else {
            //全屏
            setVideoType(FULL_SCREEN);
        }
    }

    private void setVideoType(int defaultScreen) {
        switch (defaultScreen) {
            case FULL_SCREEN://全屏
                //1.设置视频画面的大小
                videoview.setVideoSize(screenWidth, screenHeight);
                //2.设置按钮的状态--默认
                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_video_switch_screen_default_selector);
                isFullScreen = true;
                break;
            case DEFAULT_SCREEN://默认
                //1.设置视频画面的大小
                //视频真实的宽和高
                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;

                //屏幕的宽和高
                int width = screenWidth;
                int height = screenHeight;

                if (mVideoWidth * height < width * mVideoHeight) {
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoHeight * height > width * mVideoHeight) {
                    height = width * mVideoHeight / mVideoHeight;
                }

                videoview.setVideoSize(width, height);
                //2.设置按钮的状态--全屏
                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_video_switch_screen_full_selector);
                isFullScreen = false;
                break;
        }
    }


    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0); //0-100
            setBattery(level);
        }
    }


    private void setBattery(int level) {
        if (level <= 0) {
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        } else {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }


    private void setListener() {
        //准备好的监听
        videoview.setOnPreparedListener(new MyOnPreparedListener());

        //播放出错了的监听
        videoview.setOnErrorListener(new MyOnErrorListener());

        //播放完成了的监听
        videoview.setOnCompletionListener(new MyOnCompletionListener());

        //设置Seekbar状态的监听
        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());

        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());

        if (isUseSystem) {
            //监听视频播放卡-系统的api
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                videoview.setOnInfoListener(new MyOnInfoListener());
            }
        }
    }


    class MyOnInfoListener implements MediaPlayer.OnInfoListener {
        @Override
        public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START://视频卡了，拖动卡
                    ll_buffer.setVisibility(View.VISIBLE);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END://视频卡结束了，拖动卡结束了
                    ll_buffer.setVisibility(View.GONE);
                    break;
            }
            return false;
        }
    }


    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (progress > 0) {
                    isMute = false;
                } else {
                    isMute = true;
                }
                updataVoice(progress, false);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 3500);
        }
    }


    //设置音量的大小
    private void updataVoice(int progress, boolean isMute) {
        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbarVoice.setProgress(0);
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekbarVoice.setProgress(progress);
            currentVoice = progress;
        }
    }


    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        /**
         * 当手指滑动的时候，会引起SeekBar的进度变化，会回调这个方法
         *
         * @param seekBar
         * @param progress
         * @param fromUser 如果是用户引起的true，不是则为false
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoview.seekTo(progress);
            }
        }


        /*
        当手指触碰的时候回调这个方法
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        /*
           当手指离开的时候回调这个方法
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 3000);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }


    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {
        //当底层解码准备好的时候
        @Override
        public void onPrepared(MediaPlayer mp) {
            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();
            videoview.start();//开始播放
            //1.视频总时长,关联总长度
            int duration = videoview.getDuration();
            seekbarVideo.setMax(duration);
            tvDuration.setText(utils.stringForTime(duration));
            hideMediaController();//默认隐藏控制面板
            //2.发消息
            handler.sendEmptyMessage(PROGRESS);
//            videoview.setVideoSize(200,200);
//            videoview.setVideoSize(mp.getVideoWidth(),mp.getVideoHeight());

            //屏幕的默认播放
            setVideoType(DEFAULT_SCREEN);
            //把加载页面消失掉
            ll_loading.setVisibility(View.GONE);

//            mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
//                @Override
//                public void onSeekComplete(MediaPlayer mediaPlayer) {
//                    Toast.makeText(SystemVideoPlayer.this,"拖动完成", Toast.LENGTH_SHORT).show();
//                }
//            });
        }
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
            // Toast.makeText(SystemVideoPlayer.this, "播放出错!", Toast.LENGTH_SHORT).show();
            //1.播放的视频格式不支持--跳转的万能播放器继续播放
            startVitamioPlayer();
            //2.播放网络视频的时候，网络中断--1.如果网络确实断了，可以提示用户网络断了；2.网络断断续续的,重新播放
            //3.播放的时候本地文件中间有空白--下载做完成
            return false;
        }
    }

    /**
     * a.把数据按照原样传入VitamioVideoPlayer播放器
     * b.关闭系统播放器
     */
    private void startVitamioPlayer() {

        if (videoview != null) {
            videoview.stopPlayback();//停掉系统播放器
        }
        Intent intent = new Intent(this, VitamioVideoPlayer.class);
        if (mediaItems != null && mediaItems.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position);
        } else if (uri != null) {
            intent.setData(uri);
        }
        startActivity(intent);
        finish();//b.关闭系统播放器页面
    }


    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            playNextVideo();
            // Toast.makeText(SystemVideoPlayer.this, "播放完成=" + uri, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        hideBottomUIMenu();
        LogUtil.e("onRestart--");
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.e("onStart--");

    }

    @Override
    protected void onResume() {
        super.onResume();
        hideBottomUIMenu();
        LogUtil.e("onResume--");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.e("onPause--");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.e("onStop--");
    }

    @Override
    protected void onDestroy() {
        //移除所有的消息
        handler.removeCallbacksAndMessages(null);
        //释放资源的时候，先释放子类再释放父类！避免空指针异常！初始化则与之相反..
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
        LogUtil.e("onDestroy--");
    }

    private float startY;
    private float startX;
    private float touchRang;//屏幕的高
    private int mVol;//当按下的音量

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction()==MotionEvent.ACTION_DOWN){
//            Intent intent = new Intent(this, TestB.class);
//            startActivity(intent);
//            return true;
//        }
        //把事件传递给手势识别器
        detector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://手指按下
                //1.按下记录值
                startY = event.getY();
                startX = event.getX();
                mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchRang = Math.min(screenHeight, screenWidth);
                handler.removeMessages(HIDE_MEDIACONTROLLER);
                break;
            case MotionEvent.ACTION_MOVE://手指移动
                //2.移动的记录相关值
                float endY = event.getY();
                float endX = event.getX();
                float distanceY = startY - endY;
                if (endX < screenWidth / 2) {
                    //左边屏幕-调节亮度
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if (distanceY > FLING_MIN_DISTANCE
                            && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(20);
                    }
                    if (distanceY < FLING_MIN_DISTANCE
                            && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(-20);
                    }
                } else {
                    //右边屏幕-调节声音
                    //改变声音 = 滑动屏幕距离/总距离 * 音量最大值
                    float delta = (distanceY / touchRang) * maxVoice;
                    //最终声音 = 原来的 + 改变声音
                    int voice = (int) Math.min(Math.max(mVol + delta, 0), maxVoice);
                    if (delta != 0) {
                        isMute = false;
                        updataVoice(voice, isMute);
                    }
                }
                break;
            case MotionEvent.ACTION_UP://手指离开
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 3500);
                break;

        }
        return super.onTouchEvent(event);
    }


    private Vibrator vibrator;

    //设置屏幕亮度 lp = 0 全暗 ，lp= -1,根据系统设置， lp = 1; 最亮
    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {10, 200}; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, -1);
        } else if (lp.screenBrightness < 0.2) {
            lp.screenBrightness = (float) 0.2;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {10, 200}; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, -1);
        }
        getWindow().setAttributes(lp);
    }


    //显示控制面板
    private void showMediaController() {
        media_controller.setVisibility(View.VISIBLE);
        isshowMediaController = true;
    }


    //隐藏控制面板
    private void hideMediaController() {
        media_controller.setVisibility(View.GONE);
        isshowMediaController = false;
    }


    //监听物理键，
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        showMediaControllerSendMessage();
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVoice--;
            updataVoice(currentVoice, false);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 3500);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVoice++;
            updataVoice(currentVoice, false);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 3500);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showMediaControllerSendMessage() {
        //显示
        showMediaController();
        //发消息隐藏
        handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 3500);
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
