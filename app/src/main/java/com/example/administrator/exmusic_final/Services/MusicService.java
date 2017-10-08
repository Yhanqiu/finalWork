package com.example.administrator.exmusic_final.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.example.administrator.exmusic_final.Activities.PlayMusicActivity;
import com.example.administrator.exmusic_final.App;
import com.example.administrator.exmusic_final.ModelsTest.MusicData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public static final String PARAM_MUSIC_DURATION = "PARAM_MUSIC_DURATION";
    public static final String PARAM_MUSIC_SEEK_TO = "PARAM_MUSIC_SEEK_TO";
    public static final String PARAM_MUSIC_CURRENT_POSITION = "PARAM_MUSIC_CURRENT_POSITION";
    public static final String PARAM_MUSIC_IS_OVER = "PARAM_MUSIC_IS_OVER";
    public static final String CACHE_PROGRESS = "CACHE_PROGRESS";

    private int mCurrentMusicIndex = 0;
    private boolean mIsMusicPause = false;
    private List<MusicData> mMusicDatas = new ArrayList<>();

    private MusicReceiver mMusicReceiver = new MusicReceiver();
    private MediaPlayer mMediaPlayer = new MediaPlayer();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initMusicDatas(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initBoardCastReceiver();
    }

    private void initMusicDatas(Intent intent) {
        if (intent == null) return;
        List<MusicData> musicDatas = (List<MusicData>) intent.getSerializableExtra(PlayMusicActivity.PARAM_MUSIC_LIST);
        mMusicDatas.addAll(musicDatas);
    }

    private void initBoardCastReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(ACTION_OPT_MUSIC_PLAY);
        intentFilter.addAction(ACTION_OPT_MUSIC_PAUSE);
        intentFilter.addAction(ACTION_OPT_MUSIC_NEXT);
        intentFilter.addAction(ACTION_OPT_MUSIC_LAST);
        intentFilter.addAction(ACTION_OPT_MUSIC_SEEK_TO);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMusicReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        mMediaPlayer.release();
        mMediaPlayer = null;
        App.getProxy(this).unregisterCacheListener(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMusicReceiver);
        super.onDestroy();
    }

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
        Log.d(App.TAG, percentsAvailable + "");
        sendCacheProgressBroadcast(percentsAvailable);
    }

    private void play(final int index) {
//        if (index >= mMusicDatas.size()) return;
//        if (mCurrentMusicIndex == index && mIsMusicPause) {
//            mMediaPlayer.start();
//        } else {
//            mMediaPlayer.stop();
//            mMediaPlayer = null;
//
//            mMediaPlayer = MediaPlayer.create(getApplicationContext(), mMusicDatas.get(index)
//                    .getMusicRes());
//
//            mMediaPlayer.start();
//            mMediaPlayer.setOnCompletionListener(this);
//            mCurrentMusicIndex = index;
//            mIsMusicPause = false;
//
//            int duration = mMediaPlayer.getDuration();
//            sendMusicDurationBroadCast(duration);
//        }
        if (mIsMusicPause) {
            mMediaPlayer.start();
            Log.d(App.TAG,"from out");
            App.isPause = false;
        } else {
            try {

                HttpProxyCacheServer proxy = App.getProxy(this);
                proxy.registerCacheListener(this, "http://172.25.107.112:8080/app/Easily.mp3");
                String proxyURL = proxy.getProxyUrl("http://172.25.107.112:8080/app/Easily.mp3");
                mMediaPlayer.setDataSource(proxyURL);
                mMediaPlayer.prepareAsync();
                Log.d(App.TAG, "im prepared?");
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mMediaPlayer.start();
                        Log.d(App.TAG, "im prepared.");
                        mMediaPlayer.setOnCompletionListener(MusicService.this);
                        mIsMusicPause = false;
                        App.isPause = false;

                        int duration = mMediaPlayer.getDuration();
                        sendMusicDurationBroadCast(duration);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sendMusicStatusBroadCast(ACTION_STATUS_MUSIC_PLAY);
    }

    private void pause() {
        mMediaPlayer.pause();
        mIsMusicPause = true;
        App.isPause = true;
        sendMusicStatusBroadCast(ACTION_STATUS_MUSIC_PAUSE);
        Log.d(App.TAG,"pause");
    }

    private void stop() {
        mMediaPlayer.stop();
    }

    private void next() {
        if (mCurrentMusicIndex + 1 < mMusicDatas.size()) {
            play(mCurrentMusicIndex + 1);
        } else {
            stop();
        }
    }

    private void last() {
        if (mCurrentMusicIndex != 0) {
            play(mCurrentMusicIndex - 1);
        }
    }

    private void seekTo(Intent intent) {
        if (mMediaPlayer.isPlaying()) {
            int position = intent.getIntExtra(PARAM_MUSIC_SEEK_TO, 0);
            mMediaPlayer.seekTo(position);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        sendMusicCompleteBroadCast();
    }

    class MusicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_OPT_MUSIC_PLAY)) {
                play(mCurrentMusicIndex);
            } else if (action.equals(ACTION_OPT_MUSIC_PAUSE)) {
                pause();
            } else if (action.equals(ACTION_OPT_MUSIC_LAST)) {
                last();
            } else if (action.equals(ACTION_OPT_MUSIC_NEXT)) {
                next();
            } else if (action.equals(ACTION_OPT_MUSIC_SEEK_TO)) {
                seekTo(intent);
            }
        }
    }

    private void sendMusicCompleteBroadCast() {
        Intent intent = new Intent(ACTION_STATUS_MUSIC_COMPLETE);
        intent.putExtra(PARAM_MUSIC_IS_OVER, (mCurrentMusicIndex == mMusicDatas.size() - 1));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendCacheProgressBroadcast(int cacheProgress) {
        Intent intent = new Intent(ACTION_CACHE_PROGRESS);
        intent.putExtra(CACHE_PROGRESS, cacheProgress);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    private void sendMusicDurationBroadCast(int duration) {
        Intent intent = new Intent(ACTION_STATUS_MUSIC_DURATION);
        intent.putExtra(PARAM_MUSIC_DURATION, duration);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMusicStatusBroadCast(String action) {
        Intent intent = new Intent(action);
        if (action.equals(ACTION_STATUS_MUSIC_PLAY)) {
            intent.putExtra(PARAM_MUSIC_CURRENT_POSITION, mMediaPlayer.getCurrentPosition());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}