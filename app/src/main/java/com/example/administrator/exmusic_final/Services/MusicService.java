package com.example.administrator.exmusic_final.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.example.administrator.exmusic_final.Activities.LockActivity;
import com.example.administrator.exmusic_final.Activities.PlayMusicActivity;
import com.example.administrator.exmusic_final.App;
import com.example.administrator.exmusic_final.ModelsTest.MusicData;
import com.example.administrator.exmusic_final.R;
import com.example.administrator.exmusic_final.db.Music;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Random;
import java.util.logging.Handler;

/**
 * Created by Administrator on 2017/9/29.
 */

public class MusicService extends Service implements MediaPlayer.OnCompletionListener, CacheListener {

    /*操作指令*/
    public static final String ACTION_OPT_MUSIC_PLAY = "ACTION_OPT_MUSIC_PLAY";
    public static final String ACTION_OPT_MUSIC_PAUSE = "ACTION_OPT_MUSIC_PAUSE";
    public static final String ACTION_OPT_MUSIC_NEXT = "ACTION_OPT_MUSIC_NEXT";
    public static final String ACTION_OPT_MUSIC_LAST = "ACTION_OPT_MUSIC_LAST";
    public static final String ACTION_OPT_MUSIC_SEEK_TO = "ACTION_OPT_MUSIC_SEEK_TO";


    /*状态指令*/
    public static final String ACTION_STATUS_MUSIC_PLAY = "ACTION_STATUS_MUSIC_PLAY";
    public static final String ACTION_STATUS_MUSIC_PAUSE = "ACTION_STATUS_MUSIC_PAUSE";
    public static final String ACTION_STATUS_MUSIC_COMPLETE = "ACTION_STATUS_MUSIC_COMPLETE";
    public static final String ACTION_STATUS_MUSIC_DURATION = "ACTION_STATUS_MUSIC_DURATION";
    public static final String ACTION_CACHE_PROGRESS = "ACTION_CACHE_PROGRESS";
    public static final String REFRESH_ID = "REFRESH_ID";

    public static final String PARAM_MUSIC_DURATION = "PARAM_MUSIC_DURATION";
    public static final String PARAM_MUSIC_SEEK_TO = "PARAM_MUSIC_SEEK_TO";
    public static final String PARAM_MUSIC_CURRENT_POSITION = "PARAM_MUSIC_CURRENT_POSITION";
    public static final String PARAM_MUSIC_IS_OVER = "PARAM_MUSIC_IS_OVER";
    public static final String CACHE_PROGRESS = "CACHE_PROGRESS";

    public static final String ACTION_BUTTON = "ACTION_BUTTON";

    private int musicId;
    private boolean ifLocal = false;
    private boolean ifLocalChange = false;
    //    private boolean ifJustLocal = false;
    private int mCurrentMusicIndex = -1;
    private boolean mIsMusicPause = false;
    private boolean nIsMusicPause = false;
    private List<MusicData> mMusicDatas = new ArrayList<>();
    private ArrayList<Music> musicList = new ArrayList<Music>();

    private MusicReceiver mMusicReceiver = new MusicReceiver();
    private MediaPlayer mMediaPlayer = new MediaPlayer();

    private NotificationManager manager;
    private RemoteViews bigRemoteViews, smallRemoteViews;
//    public android.os.Handler handler = new Handler() {
//
//        public void handleMessage(android.os.Message msg) {
//
//
//            // 设置通知栏的图片文字
//            remoteViews = new RemoteViews(getPackageName(),
//                    R.layout.notification_layout);
//            remoteViews.setImageViewResource(R.id.widget_album, R.raw.ic_music1);
//            remoteViews.setTextViewText(R.id.widget_title, "music1");
//            remoteViews.setTextViewText(R.id.widget_artist, "artist1");
//            if (!App.isPause) {
//                remoteViews.setImageViewResource(R.id.widget_play, R.drawable.ic_pause);
//            }else {
//                remoteViews.setImageViewResource(R.id.widget_play, R.drawable.ic_play);
//            }
//
//            setNotification();
//        };
//    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        musicId = intent.getIntExtra("musicId", 0);
        musicList = (ArrayList<Music>) intent.getSerializableExtra("localMusics");
        if (musicList != null) {
            if (ifLocal) {
                ifLocalChange = false;
            } else {
                ifLocalChange = true;
            }
            ifLocal = true;
        } else {
            if (!ifLocal) {
                ifLocalChange = false;
            } else {
                ifLocalChange = true;
            }
            ifLocal = false;
            musicList = (ArrayList<Music>) DataSupport.findAll(Music.class);
        }
        Log.d(App.TAG, action + "onstart");
//        initMusicDatas(intent);
//        else if (ACTION_SIOP_SERVICE.equals(action)) {
//            stopService();
//        } else if (ACTION_FAVORITE.equals(action)) {
//            isFavorite = isFavorite ? false : true;
//            showNotification();
//        } else if (ACTION_LYRIC.equals(action)) {
//            isShowLyric = isShowLyric ? false : true;
//            showNotification();
//        }
        play(musicId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        initBoardCastReceiver();
    }

    private void showNotification() {
        Log.d(App.TAG, "notify");

//        PendingIntent pendingIntent = PendingIntent.getActivity(this, new Random().nextInt(), new Intent(this, PlayMusicActivity.class), 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.play)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_MAX)
//                .setContentIntent(pendingIntent)
                .setCustomBigContentView(getBigNotificationView())
                .setCustomContentView(getSmallNotificationView())
                .setOngoing(true)
                .build();
//        startForeground(100, notification);

        manager.notify(100, notification);
    }


    RemoteViews getBigNotificationView() {
        if (bigRemoteViews == null) {
            bigRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
            initNotificationView(bigRemoteViews);
        }
        updateNotificationView(bigRemoteViews);
        return bigRemoteViews;
    }

    RemoteViews getSmallNotificationView() {
        if (smallRemoteViews == null) {
            smallRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout_small);
            initNotificationView(smallRemoteViews);
        }
        updateNotificationView(smallRemoteViews);
        return smallRemoteViews;
    }


    void initNotificationView(RemoteViews remoteViews) {
//        remoteViews.setOnClickPendingIntent(R.id.close, getPendingIntent(ACTION_SIOP_SERVICE));
        remoteViews.setOnClickPendingIntent(R.id.last, getPendingIntent(ACTION_OPT_MUSIC_LAST));
        remoteViews.setOnClickPendingIntent(R.id.play, getPendingIntent(ACTION_BUTTON));
        remoteViews.setOnClickPendingIntent(R.id.next, getPendingIntent(ACTION_OPT_MUSIC_NEXT));
//        remoteViews.setOnClickPendingIntent(R.id.favorite, getPendingIntent(ACTION_FAVORITE));
//        remoteViews.setOnClickPendingIntent(R.id.lyric, getPendingIntent(ACTION_LYRIC));
    }

    void updateNotificationView(RemoteViews remoteViews) {
//        Song song = mPlayList.getCurrentSong();
//        Bitmap thumb = FileUtils.parseThumbToBitmap(song);
        Bitmap thumb;
        File imageFile = new File(musicList.get(mCurrentMusicIndex).getImageURL());
        if (imageFile.exists()&&imageFile.length()>2048) {
            thumb = BitmapFactory.decodeFile(musicList.get(mCurrentMusicIndex).getImageURL());
        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.test_back);
        }
//        if (thumb == null) {
//            remoteViews.setImageViewResource(R.id.thumb, R.drawable.pic_error_150);
//        } else {
        remoteViews.setImageViewBitmap(R.id.widget_album, thumb);
//        }
        remoteViews.setTextViewText(R.id.song_name, musicList.get(mCurrentMusicIndex).getName());
//        remoteViews.setTextViewText(R.id.info, getString(R.string.song_info, song.getArtist(), song.getAlbum()));
        remoteViews.setTextViewText(R.id.artist, musicList.get(mCurrentMusicIndex).getArtist());
//        remoteViews.setImageViewResource(R.id.favorite, isFavorite ? R.drawable.notification_love_checked_32 : R.drawable.notification_love_32);
        remoteViews.setImageViewResource(R.id.play, nIsMusicPause ? R.drawable.ic_play : R.drawable.ic_pause);
//        remoteViews.setImageViewResource(R.id.lyric, isShowLyric ? R.drawable.notification_lyric_checked_32 : R.drawable.notification_lyric_32);
    }

    private PendingIntent getPendingIntent(String extra) {
//        PendingIntent pi = PendingIntent.getService(this, new Random().nextInt(), new Intent(action), 0);
        Intent intent = new Intent(extra);
//        Log.d("hhh",mCurrentMusicIndex+"hhh");
//        if (extra.equals(ACTION_OPT_MUSIC_LAST)) {
//            intent.putExtra("musicId", (mCurrentMusicIndex + musicList.size() - 1) % musicList.size());
//        } else if (extra.equals(ACTION_OPT_MUSIC_NEXT)) {
//            intent.putExtra("musicId",(mCurrentMusicIndex + 1) % musicList.size());
//        }
        PendingIntent pi = PendingIntent.getBroadcast(this, new Random().nextInt(), intent, 0);
        Log.d(App.TAG, pi.toString() + "what???");
        return pi;
    }

//    private void setNotification() {
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//
//        Intent intent = new Intent(this, PlayMusicActivity.class);
//        // 点击跳转到主界面
//        PendingIntent intent_go = PendingIntent.getActivity(this, 5, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.notice, intent_go);
//
////         4个参数context, requestCode, intent, flags
////        PendingIntent intent_close = PendingIntent.getActivity(this, 0, intent,
////                PendingIntent.FLAG_UPDATE_CURRENT);
////        remoteViews.setOnClickPendingIntent(R.id.widget_close, intent_close);
//
//        // 设置上一曲
//        Intent prv = new Intent();
//        prv.setAction(MusicService.ACTION_OPT_MUSIC_LAST);
//        PendingIntent intent_prev = PendingIntent.getBroadcast(this, 1, prv,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.widget_prev, intent_prev);
//
//        // 设置播放
//        if (!App.isPause) {
//            Intent playorpause = new Intent();
//            playorpause.setAction(MusicService.ACTION_OPT_MUSIC_PAUSE);
//            PendingIntent intent_play = PendingIntent.getBroadcast(this, 2,
//                    playorpause, PendingIntent.FLAG_UPDATE_CURRENT);
//            remoteViews.setOnClickPendingIntent(R.id.widget_play, intent_play);
//        }
//        if (App.isPause) {
//            Intent playorpause = new Intent();
//            playorpause.setAction(MusicService.ACTION_OPT_MUSIC_PLAY);
//            PendingIntent intent_play = PendingIntent.getBroadcast(this, 6,
//                    playorpause, PendingIntent.FLAG_UPDATE_CURRENT);
//            remoteViews.setOnClickPendingIntent(R.id.widget_play, intent_play);
//        }
//
//        // 下一曲
//        Intent next = new Intent();
//        next.setAction(MusicService.ACTION_OPT_MUSIC_NEXT);
//        PendingIntent intent_next = PendingIntent.getBroadcast(this, 3, next,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.widget_next, intent_next);
//
//        // 设置收藏
////        PendingIntent intent_fav = PendingIntent.getBroadcast(this, 4, intent,
////                PendingIntent.FLAG_UPDATE_CURRENT);
////        remoteViews.setOnClickPendingIntent(R.id.widget_fav, intent_fav);
//
//        builder.setSmallIcon(R.drawable.ic_play); // 设置顶部图标
//
//        Notification notify = builder.build();
//        notify.contentView = remoteViews; // 设置下拉图标
//        notify.bigContentView = remoteViews; // 防止显示不完全,需要添加apisupport
//        notify.flags = Notification.FLAG_ONGOING_EVENT;
//        notify.icon = R.drawable.play;
//
//        manager.notify(100, notify);
//    }

    private void initMusicDatas(Intent intent) {
        if (intent == null) return;
        List<MusicData> musicDatas = (List<MusicData>) intent.getSerializableExtra(PlayMusicActivity.PARAM_MUSIC_LIST);
        mMusicDatas.addAll(musicDatas);
    }

    private void initBoardCastReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);

        intentFilter.addAction(ACTION_OPT_MUSIC_PLAY);
        intentFilter.addAction(ACTION_OPT_MUSIC_PAUSE);
        intentFilter.addAction(ACTION_OPT_MUSIC_NEXT);
        intentFilter.addAction(ACTION_OPT_MUSIC_LAST);
        intentFilter.addAction(ACTION_OPT_MUSIC_SEEK_TO);

        intentFilter.addAction(ACTION_BUTTON);

//        LocalBroadcastManager.getInstance(this).registerReceiver(mMusicReceiver, intentFilter);
        registerReceiver(mMusicReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        Log.d("serviceonde", "afsdjk");
        mMediaPlayer.release();
        mMediaPlayer = null;
        App.getProxy(this).unregisterCacheListener(this);
        unregisterReceiver(mMusicReceiver);
//        if (remoteViews != null) {
//            manager.cancel(100);
//        }
        SharedPreferences sp = getSharedPreferences("currentStatus", MODE_APPEND);
        SharedPreferences.Editor serviceEditor = sp.edit();
        serviceEditor.putBoolean("ifServiceRunning", false);
        serviceEditor.apply();
        super.onDestroy();
    }

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
        Log.d("cachecache", percentsAvailable + "");
        sendCacheProgressBroadcast(percentsAvailable);
    }

    private void play(final int index) {
        Log.d(App.TAG, mIsMusicPause + "");
        if (ifLocal) {
            Log.d("locallocal", mCurrentMusicIndex + "***" + index);
            if (nIsMusicPause && mCurrentMusicIndex == index) {
                mMediaPlayer.start();
                Log.d(App.TAG, "from out");
//            mIsMusicPause = false;
                App.isPause = false;
                nIsMusicPause = false;
                int duration = mMediaPlayer.getDuration();
                sendMusicDurationBroadCast(duration);
            } else if (mCurrentMusicIndex != index || ifLocalChange) {
                try {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(musicList.get(index).getMusicURL());
                    mMediaPlayer.prepareAsync();
                    Log.d(App.TAG, "im prepared?");
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.start();
                            Log.d(App.TAG, "im prepared.");
                            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    Log.d("comcom1", "dfjald");
                                }
                            });
                            mIsMusicPause = false;
                            App.isPause = false;
                            nIsMusicPause = false;
//                            mCurrentMusicIndex = index;

                            int duration = mMediaPlayer.getDuration();
                            sendMusicDurationBroadCast(duration);
                            sendMusicStatusBroadCast(REFRESH_ID);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                int duration = mMediaPlayer.getDuration();
                sendMusicDurationBroadCast(duration);
            }

        } else {
            Log.d("indexindex", mCurrentMusicIndex + ",,," + index);
            if (nIsMusicPause && mCurrentMusicIndex == index) {
                mMediaPlayer.start();
                Log.d(App.TAG, "from out");
//            mIsMusicPause = false;
                App.isPause = false;
                nIsMusicPause = false;
                int duration = mMediaPlayer.getDuration();
                sendMusicDurationBroadCast(duration);
            } else if (mCurrentMusicIndex != index || ifLocalChange) {
                try {
                    mMediaPlayer.reset();
                    HttpProxyCacheServer proxy = App.getProxy(this);
//                String url = "http://172.25.107.112:8080/app/Easily.mp3";
                    Log.d("wtf", musicId + "");
                    String url = "http:/172.25.107.112:8080/ExMusic/TestMusic?msg=music&id=" + musicList.get(index).getQueryId();
                    proxy.registerCacheListener(this, url);
                    String proxyURL = proxy.getProxyUrl(url);
                    Log.d("urlurl", proxyURL);
                    mMediaPlayer.setDataSource(proxyURL);
                    mMediaPlayer.prepareAsync();
                    Log.d(App.TAG, "im prepared?");
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.start();
                            Log.d(App.TAG, "im prepared.");
                            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    Log.d("comcom2", "dfjald");
                                }
                            });
                            mIsMusicPause = false;
                            App.isPause = false;
                            nIsMusicPause = false;
//                            mCurrentMusicIndex = index;

                            int duration = mMediaPlayer.getDuration();
                            sendMusicDurationBroadCast(duration);
                            sendMusicStatusBroadCast(REFRESH_ID);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                int duration = mMediaPlayer.getDuration();
                sendMusicDurationBroadCast(duration);
            }
        }
        mCurrentMusicIndex = index;
        showNotification();
        sendMusicStatusBroadCast(ACTION_STATUS_MUSIC_PLAY);
    }

    private void pause() {
        mMediaPlayer.pause();
        mIsMusicPause = true;
        App.isPause = true;
        nIsMusicPause = true;
        showNotification();
        sendMusicStatusBroadCast(ACTION_STATUS_MUSIC_PAUSE);
        Log.d(App.TAG, "pause");
    }

    private void stop() {
        mMediaPlayer.stop();
    }

    private void next() {
//        if (mCurrentMusicIndex + 1 < mMusicDatas.size()) {
        mMediaPlayer.reset();
        play((mCurrentMusicIndex + 1) % musicList.size());
//        } else {
//            stop();
//        }
    }

    private void last() {
//        if (mCurrentMusicIndex != 0) {
        mMediaPlayer.reset();
        play((mCurrentMusicIndex - 1) % musicList.size());
//        }
    }

    private void seekTo(Intent intent) {
        if (mMediaPlayer.isPlaying()) {
            int position = intent.getIntExtra(PARAM_MUSIC_SEEK_TO, 0);
            mMediaPlayer.seekTo(position);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
//        sendMusicCompleteBroadCast();
        mMediaPlayer.reset();
        Log.d("comcom", mCurrentMusicIndex + "");
        play((mCurrentMusicIndex + 1) % musicList.size());
    }

    class MusicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(App.TAG, "receive" + action);
            int index = intent.getIntExtra("musicId", mCurrentMusicIndex);
            if (action.equals(ACTION_OPT_MUSIC_PLAY)) {
                play(index);
            } else if (action.equals(ACTION_OPT_MUSIC_PAUSE)) {
                pause();
            } else if (action.equals(ACTION_OPT_MUSIC_LAST)) {
                mMediaPlayer.reset();
                play((mCurrentMusicIndex + musicList.size() - 1) % musicList.size());
            } else if (action.equals(ACTION_OPT_MUSIC_NEXT)) {
                mMediaPlayer.reset();
                play((mCurrentMusicIndex + 1) % musicList.size());
            } else if (action.equals(ACTION_OPT_MUSIC_SEEK_TO)) {
                seekTo(intent);
            } else if (action.equals(ACTION_BUTTON)) {
                if (nIsMusicPause) {
                    play(mCurrentMusicIndex);
                } else {
                    pause();
                }
            }
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                Intent lockscreen = new Intent(MusicService.this, LockActivity.class);
                lockscreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                lockscreen.putExtra("musicName", musicList.get(mCurrentMusicIndex).getName());
//                lockscreen.putExtra("musicArtist", musicList.get(mCurrentMusicIndex).getArtist());
                lockscreen.putExtra("position", mMediaPlayer.getCurrentPosition());
                lockscreen.putExtra("ifPause", nIsMusicPause);
                lockscreen.putExtra("localMusics", musicList);
                lockscreen.putExtra("musicId", mCurrentMusicIndex);
                startActivity(lockscreen);
            }
        }
    }

    private void sendMusicCompleteBroadCast() {
        Intent intent = new Intent(ACTION_STATUS_MUSIC_COMPLETE);
        intent.putExtra(PARAM_MUSIC_IS_OVER, (mCurrentMusicIndex == mMusicDatas.size() - 1));
        sendBroadcast(intent);
    }

    private void sendCacheProgressBroadcast(int cacheProgress) {
        Intent intent = new Intent(ACTION_CACHE_PROGRESS);
        intent.putExtra(CACHE_PROGRESS, cacheProgress);
        sendBroadcast(intent);

    }

    private void sendMusicDurationBroadCast(int duration) {
        Intent intent = new Intent(ACTION_STATUS_MUSIC_DURATION);
        intent.putExtra(PARAM_MUSIC_DURATION, duration);
        sendBroadcast(intent);
    }

    private void sendMusicStatusBroadCast(String action) {
        Intent intent = new Intent(action);
        if (action.equals(ACTION_STATUS_MUSIC_PLAY)) {
            intent.putExtra(PARAM_MUSIC_CURRENT_POSITION, mMediaPlayer.getCurrentPosition());
            intent.putExtra("musicName", musicList.get(mCurrentMusicIndex).getName());
            intent.putExtra("musicArtist", musicList.get(mCurrentMusicIndex).getArtist());
            intent.putExtra("musicId",mCurrentMusicIndex);
            intent.putExtra("ifPause", nIsMusicPause);
        } else if (action.equals(REFRESH_ID)) {
            Log.d("indexplay", mCurrentMusicIndex + "");
            intent.putExtra("musicId", mCurrentMusicIndex);
        }
        sendBroadcast(intent);
    }

}
