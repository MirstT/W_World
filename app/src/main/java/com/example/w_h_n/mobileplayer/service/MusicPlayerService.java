package com.example.w_h_n.mobileplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.w_h_n.mobileplayer.IMusicPlayerService;
import com.example.w_h_n.mobileplayer.R;
import com.example.w_h_n.mobileplayer.activity.AudioPlayerActivity;
import com.example.w_h_n.mobileplayer.domain.MediaItem;
import com.example.w_h_n.mobileplayer.utils.CacheUtils;

import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayerService extends Service {

    public static final String OPENAUDIO = "com.atwanghao.mobileplayer_OPENAUDIO";
    private ArrayList<MediaItem> mediaItems;
    private int position;
    private MediaItem mediaItem;//当前播放的音频文件对象
    private MediaPlayer mediaPlayer;//用于播放音乐

    public static final int REPEAT_NORMAL = 1; //顺序播放
    public static final int REPEAT_SINGLE = 2;//单曲循环
    public static final int REPEAT_ALL = 3;//循环播放

    private int playmode = REPEAT_NORMAL;

    @Override
    public void onCreate() {
        super.onCreate();
        playmode = CacheUtils.getPlaymode(this, "playmode");
        //加载音乐列表
        getDateFromLocal();
    }

    private void getDateFromLocal() {
        new Thread() {

            @Override
            public void run() {
                super.run();
                mediaItems = new ArrayList<>();
                ContentResolver resolver = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//Audio文件在sdcard的名称
                        MediaStore.Audio.Media.DURATION,//Audio总时长
                        MediaStore.Audio.Media.SIZE,//Audio文件大小
                        MediaStore.Audio.Media.DATA,//Audio的绝对地址
                        MediaStore.Audio.Media.ARTIST,//Audio的演唱者
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
            }
        }.start();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    private IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub() {
        MusicPlayerService service = MusicPlayerService.this;

        @Override
        public void openAudio(int postion) throws RemoteException {
            service.openAudio(postion);
        }

        @Override
        public void start() throws RemoteException {
            service.start();

        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public void stop() throws RemoteException {
            service.stop();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        @Override
        public String getName() throws RemoteException {
            return service.getName();
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return service.getAudioPath();
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public void setPlayMode(int playmode) throws RemoteException {
            service.setPlayMode(playmode);
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return service.getPlayMode();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            mediaPlayer.seekTo(position);
        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            return mediaPlayer.getAudioSessionId();
        }
    };


    /*
    根据位置打开对应的音频文件
     */
    private void openAudio(int postion) {
        this.position = postion;
        if (mediaItems != null && mediaItems.size() > 0) {
            mediaItem = mediaItems.get(postion);
            if (mediaPlayer != null) {
//                mediaPlayer.release();
                mediaPlayer.reset();
            }
            try {
                mediaPlayer = new MediaPlayer();
                //设置监听：播放出错，播放完成，准备好
                mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                mediaPlayer.setOnErrorListener(new MyOnErrorListener());
                mediaPlayer.setDataSource(mediaItem.getData());
                mediaPlayer.prepareAsync();

                if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                    //单曲循环播放-不会触发播放完成的回调
                    mediaPlayer.setLooping(true);
                } else {
                    //不循环播放
                    mediaPlayer.setLooping(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show();
        }

    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            next();
            return false;
        }
    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            next();
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            //通知Activity来获取信息--广播
            notifyChange(OPENAUDIO);
//            EventBus.getDefault().post(mediaItem);
            start();

        }
    }

    //根据动作发广播
    private void notifyChange(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    private NotificationManager manager;

    /*
    播放音乐
     */
    private void start() {
        mediaPlayer.start();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //最主要
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra("notification", true);//标识来自状态栏
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_music_playing)
                .setContentTitle("W视界")
                .setContentText("正在播放:" + getName())
                .setContentIntent(pendingIntent)
                .build();
        manager.notify(1, notification);
    }

    /*
    停止音乐
     */
    private void pause() {
        mediaPlayer.pause();
        manager.cancel(1);
    }

    /*
    暂停音乐
     */
    private void stop() {

    }

    /*
     得到当前的播放进度
      */
    private int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    /*
     得到当前音频总时长
      */
    private int getDuration() {
        return mediaPlayer.getDuration();
    }

    /*
      得到艺术家
       */
    private String getArtist() {
        return mediaItem.getArtist();
    }

    /*
       得到歌曲名字
        */
    private String getName() {
        return mediaItem.getName();
    }

    /*
       得到歌曲播放路径
        */
    private String getAudioPath() {
        return mediaItem.getData();
    }


    /*
    播放下一个视频
     */
    private void next() {
        //1.根据当前的播放模式，设置下一个的位置
        setNextPosition();
        //2.根据当前的播放模式和下标位置去播放音频
        openNextAudio();
    }

    private void setNextPosition() {
        int playmode = getPlayMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            position++;
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            position++;
            if (position >= mediaItems.size()) {
                position = 0;
            }
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            position++;
            if (position >= mediaItems.size()) {
                position = 0;
            }
        } else {
            position++;
        }
    }

    private void openNextAudio() {
        int playmode = getPlayMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            if (position < mediaItems.size()) {
                //正常范围
                openAudio(position);
            } else {
                position = mediaItems.size() - 1;
            }
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            openAudio(position);
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            openAudio(position);
        } else {
            if (position < mediaItems.size()) {
                //正常范围
                openAudio(position);
            } else {
                position = mediaItems.size() - 1;
            }
        }
    }

    /*
    播放上一个视频
     */
    private void pre() {
        //1.根据当前的播放模式，设置上一个的位置
        setPrePosition();
        //2.根据当前的播放模式和下标位置去播放音频
        openPreAudio();
    }

    private void openPreAudio() {
        int playmode = getPlayMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            if (position >= 0) {
                //正常范围
                openAudio(position);
            } else {
                position = 0;
            }
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            openAudio(position);
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            openAudio(position);
        } else {
            if (position >= 0) {
                //正常范围
                openAudio(position);
            } else {
                position = 0;
            }
        }
    }

    private void setPrePosition() {
        int playmode = getPlayMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            position--;
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            position--;
            if (position < 0) {
                position = mediaItems.size() - 1;
            }
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            position--;
            if (position < 0) {
                position = mediaItems.size() - 1;
            }
        } else {
            position--;
        }
    }

    /*
    设置播放模式
     */
    private void setPlayMode(int playmode) {
        this.playmode = playmode;
        CacheUtils.putPlaymode(this, "playmode", playmode);

        if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            //单曲循环播放-不会触发播放完成的回调
            mediaPlayer.setLooping(true);
        } else {
            //不循环播放
            mediaPlayer.setLooping(false);
        }

    }

    /*
    得到播放模式
     */
    private int getPlayMode() {
        return playmode;
    }


    /*
    是否在播放音频
     */
    private boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }
}
