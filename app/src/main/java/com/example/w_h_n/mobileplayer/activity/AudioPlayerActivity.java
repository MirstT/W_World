package com.example.w_h_n.mobileplayer.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.w_h_n.mobileplayer.IMusicPlayerService;
import com.example.w_h_n.mobileplayer.R;
import com.example.w_h_n.mobileplayer.service.MusicPlayerService;
import com.example.w_h_n.mobileplayer.utils.LyricUtils;
import com.example.w_h_n.mobileplayer.utils.Utils;
import com.example.w_h_n.mobileplayer.view.BaseVisualizerView;
import com.example.w_h_n.mobileplayer.view.ShowLyricView;

import java.io.File;

public class AudioPlayerActivity extends Activity implements View.OnClickListener {
    private static final int PROGRESS = 1;//进度更新
    private static final int SHOW_LYRIC = 2; //显示歌词
    private boolean notification; //true:从状态栏进入，false:从播放列表进入
    private int position;
    private IMusicPlayerService service;//服务的代理类，通过它可以调用服务的方法
    private ImageView ivIcon;
    private TextView tvArtist;
    private TextView tvName;
    private TextView tvTime;
    private SeekBar seekbarAudio;
    private Button btnAudioPlaymode;
    private Button btnAudioPre;
    private Button btnAudioStartPause;
    private Button btnAudioNext;
    private Button btnLyrc;
    private ShowLyricView showLyricView;
    private BaseVisualizerView baseVisualizerView;
    private MyReceiver receiver;
    private Utils utils;


    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2018-10-26 13:02:12 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        setContentView(R.layout.activity_audioplayer);
        ivIcon = (ImageView) findViewById(R.id.iv_icon);
        ivIcon.setBackgroundResource(R.drawable.animation_list);
        AnimationDrawable rocketAnimation = (AnimationDrawable) ivIcon.getBackground();
        rocketAnimation.start();
        tvArtist = (TextView) findViewById(R.id.tv_artist);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvTime = (TextView) findViewById(R.id.tv_time);
        seekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);
        btnAudioPlaymode = (Button) findViewById(R.id.btn_audio_playmode);
        btnAudioPre = (Button) findViewById(R.id.btn_audio_pre);
        btnAudioStartPause = (Button) findViewById(R.id.btn_audio_start_pause);
        btnAudioNext = (Button) findViewById(R.id.btn_audio_next);
        btnLyrc = (Button) findViewById(R.id.btn_lyrc);
        btnAudioPlaymode.setOnClickListener(this);
        btnAudioPre.setOnClickListener(this);
        btnAudioStartPause.setOnClickListener(this);
        btnAudioNext.setOnClickListener(this);
        btnLyrc.setOnClickListener(this);
        showLyricView = (ShowLyricView) findViewById(R.id.showLyricView);
        baseVisualizerView = (BaseVisualizerView) findViewById(R.id.baseVisualizerView);
        seekbarAudio.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener()); //设置音频的拖动
    }


    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                //拖动记录
                try {
                    service.seekTo(progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }


    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2018-10-26 13:02:12 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == btnAudioPlaymode) {
            // Handle clicks for btnAudioPlaymode
            setPlaymode();
        } else if (v == btnAudioPre) {
            if (service != null) {
                try {
                    service.pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnAudioStartPause) {
            if (service != null) {
                try {
                    if (service.isPlaying()) {
                        //暂停
                        service.pause();
                        //按钮-播放
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                    } else {
                        //播放
                        service.start();
                        //按钮-暂停
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnAudioNext) {
            if (service != null) {
                try {
                    service.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnLyrc) {
            // Handle clicks for btnLyrc
        }
    }

    private void setPlaymode() {
        try {
            int playmode = service.getPlayMode();
            if (playmode == MusicPlayerService.REPEAT_NORMAL) {
                playmode = MusicPlayerService.REPEAT_SINGLE;
            } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                playmode = MusicPlayerService.REPEAT_ALL;
            } else if (playmode == MusicPlayerService.REPEAT_ALL) {
                playmode = MusicPlayerService.REPEAT_NORMAL;
            } else {
                playmode = MusicPlayerService.REPEAT_NORMAL;
            }
            //保持
            service.setPlayMode(playmode);
            //设置图片
            showPlaymode();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void showPlaymode() {
        try {
            int playmode = service.getPlayMode();
            if (playmode == MusicPlayerService.REPEAT_NORMAL) {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
                Toast.makeText(AudioPlayerActivity.this, "顺序播放", Toast.LENGTH_SHORT).show();
            } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
                Toast.makeText(AudioPlayerActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
            } else if (playmode == MusicPlayerService.REPEAT_ALL) {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
                Toast.makeText(AudioPlayerActivity.this, "列表循环", Toast.LENGTH_SHORT).show();
            } else {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
                Toast.makeText(AudioPlayerActivity.this, "顺序播放", Toast.LENGTH_SHORT).show();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //校验状态
    private void checkPlaymode() {
        try {
            int playmode = service.getPlayMode();
            if (playmode == MusicPlayerService.REPEAT_NORMAL) {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
            } else if (playmode == MusicPlayerService.REPEAT_ALL) {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
            } else {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            }
            //校验播放和暂停的按钮
            if (service.isPlaying()) {
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
            } else {
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_LYRIC://显示歌词
                    //1.得到当前的进度
                    try {
                        int currentPosition = service.getCurrentPosition();
                        //2.把进度传入ShowLyricView控件，并且计算该高亮哪一句
                        showLyricView.setshowNextLyric(currentPosition);
                        //3.实时的发消息
                        handler.removeMessages(SHOW_LYRIC);
                        handler.sendEmptyMessage(SHOW_LYRIC);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case PROGRESS:
                    try {
                        //1.得到当前进度
                        int currentPosition = service.getCurrentPosition();
                        //2.设置SeekBar.setProgress(进度)
                        seekbarAudio.setProgress(currentPosition);
                        //3.时间进度跟新
                        tvTime.setText(utils.stringForTime(currentPosition) + "/" + utils.stringForTime(service.getDuration()));
                        //4.每秒更新一次
                        handler.removeMessages(PROGRESS);
                        handler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideBottomUIMenu();//隐藏虚拟按键，并且全屏
        initData();
        findViews();
        getData();
        bindAndStartService();
    }

    private void initData() {
        utils = new Utils();
        //注册广播
        receiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicPlayerService.OPENAUDIO);
        registerReceiver(receiver, intentFilter);
//        //1.EventBus注册
//        EventBus.getDefault().register(this);//this是当前类
    }

    private ServiceConnection con = new ServiceConnection() {
        //连接成功回调此方法
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            service = IMusicPlayerService.Stub.asInterface(iBinder);
            if (service != null) {
                try {
                    if (!notification) {//从列表
                        service.openAudio(position);
                    } else {
                        //从状态栏
                        showViewData();
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        //断开连接回调此方法
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            try {
                if (service != null) {
                    service.stop();
                    service = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };


    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            showLyric();
            showViewData();
            checkPlaymode();
//            showData(null);
            setupVisualizerFxAndUi();
        }
    }

//    //3.订阅方法
//    @Subscribe(threadMode = ThreadMode.MAIN,sticky = false,priority = 0)
//    public void showData(MediaItem mediaItem) {
//        showViewData();
//        checkPlaymode();
//    }

    private Visualizer mVisualizer;
    //生成一个VisualizerView对象，使音频频谱的波段能够反映到 VisualizerView上
    private void setupVisualizerFxAndUi() {
        try {
            int audioSessionid = service.getAudioSessionId();
            System.out.println("audioSessionid==" + audioSessionid);
            mVisualizer = new Visualizer(audioSessionid);
            // 参数内必须是2的位数
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            // 设置允许波形表示，并且捕获它
            baseVisualizerView.setVisualizer(mVisualizer);
            mVisualizer.setEnabled(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void showLyric() {
        //解析歌词.
        LyricUtils lyricUtils = new LyricUtils();
        try {
            String path = service.getAudioPath();
            //传歌词文件
            path = path.substring(0, path.lastIndexOf("."));
            File file = new File(path + ".lrc");
            if (!file.exists()) {
                file = new File(path + ".txt");
            }
            lyricUtils.readLyricFile(file);

            showLyricView.setLyrics(lyricUtils.getLyrics());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (lyricUtils.isExistsLyric()) {
            handler.sendEmptyMessage(SHOW_LYRIC);
        }
    }

    private void showViewData() {
        try {
            tvArtist.setText(service.getArtist());
            setTvName();
            //设置进度条的最大值
            seekbarAudio.setMax(service.getDuration());
            //发消息
            handler.sendEmptyMessage(PROGRESS);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void setTvName() throws RemoteException {//去掉歌词页面的后缀名
        String tempName = service.getName();
        tempName = tempName.substring(0, tempName.lastIndexOf("."));
        tvName.setText(tempName);
    }

    private void bindAndStartService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction("com.atwanghao.mobileplayer_OPENAUDIO");
        bindService(intent, con, Context.BIND_AUTO_CREATE);
        startService(intent); //不至于实例化多个服务
    }

    //得到数据
    private void getData() {
        notification = getIntent().getBooleanExtra("notification", false);
        if (!notification) {
            position = getIntent().getIntExtra("position", 0);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        //取消注册广播
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        //2.EventBus取消注册
        // EventBus.getDefault().unregister(this);


        //解绑服务
        if (con != null) {
            unbindService(con);
            con = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVisualizer != null) {
            mVisualizer.release();
        }
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
