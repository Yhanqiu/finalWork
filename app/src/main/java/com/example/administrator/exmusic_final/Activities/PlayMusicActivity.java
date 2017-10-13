package com.example.administrator.exmusic_final.Activities;

/**
 * Created by Administrator on 2017/9/29.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.administrator.exmusic_final.App;
import com.example.administrator.exmusic_final.ModelsTest.LrcDataBuilder;
import com.example.administrator.exmusic_final.ModelsTest.MusicData;
import com.example.administrator.exmusic_final.R;
import com.example.administrator.exmusic_final.Services.MusicService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.example.administrator.exmusic_final.Utils.DisplayUtil;
import com.example.administrator.exmusic_final.Utils.FastBlurUtil;
import com.example.administrator.exmusic_final.db.Music;
import com.example.administrator.exmusic_final.widget.BackgourndAnimationRelativeLayout;
import com.example.administrator.exmusic_final.widget.BackgroundAdapter;
import com.example.administrator.exmusic_final.widget.ILrcView;
import com.example.administrator.exmusic_final.widget.LrcRow;
import com.example.administrator.exmusic_final.widget.LrcView;

import org.litepal.crud.DataSupport;

public class PlayMusicActivity extends AppCompatActivity implements View
        .OnClickListener {
    private int duration;
    private int musicId = -1;
    private boolean ifLocal;
    private boolean isPaused = true;
    private ArrayList<Music> musicList = new ArrayList<Music>();

    private LrcView mLrcView;
    private Toolbar mToolbar;
    private SeekBar mSeekBar;
    private ImageView mIvPlayOrPause, mIvNext, mIvLast;
    private TextView mTvMusicDuration, mTvTotalMusicDuration, musicName, musicArtist;
    private BackgourndAnimationRelativeLayout mRootLayout;
    public static final int MUSIC_MESSAGE = 0;

    public static final String PARAM_MUSIC_LIST = "PARAM_MUSIC_LIST";

    private Handler mMusicHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mSeekBar.getProgress() >= duration) {
                next();
            } else {
                mSeekBar.setProgress(mSeekBar.getProgress() + 1000);
                mLrcView.seekLrcToTime(mSeekBar.getProgress() + 1000);
                mTvMusicDuration.setText(duration2Time(mSeekBar.getProgress()));
                startUpdateSeekBarProgress();

            }
        }
    };

    private MusicReceiver mMusicReceiver = new MusicReceiver();
    private List<MusicData> mMusicDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_music);
        musicList = (ArrayList<Music>) getIntent().getSerializableExtra("localMusics");
        musicId = getIntent().getIntExtra("musicId", 0);
        if (musicList != null) {
            ifLocal = true;
            Intent intent = new Intent(this, MusicService.class);
            intent.putExtra("localMusics", musicList);
            intent.putExtra("musicId", musicId);
            startService(intent);
        } else {
            musicList = (ArrayList<Music>) DataSupport.findAll(Music.class);
            ifLocal = false;
            Intent intent = new Intent(this, MusicService.class);
            intent.putExtra(PARAM_MUSIC_LIST, (Serializable) mMusicDatas);
            intent.putExtra("musicId", musicId);

            startService(intent);
        }
        initMusicDatas(musicId);
        initView();
        initMusicReceiver();
        makeStatusBarTransparent();

//        List<LrcRow> lrcRows = new LrcDataBuilder().BuiltFromAssets(this, "test2.lrc");
//
//        mLrcView.setLrcData(lrcRows);
//        // 开始播放歌曲并同步展示歌词
//        Timer mTimer = new Timer();
//        TimerTask mTask =
    }

    private void initMusicReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.REFRESH_ID);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PLAY);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PAUSE);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_DURATION);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_COMPLETE);
        /*注册广播*/
        registerReceiver(mMusicReceiver, intentFilter);
    }

    private void initView() {
        mLrcView = (LrcView) findViewById(R.id.lrcview);
//        mDisc.setVisibility(View.INVISIBLE);
        mIvNext = (ImageView) findViewById(R.id.ivNext);
        mIvLast = (ImageView) findViewById(R.id.ivLast);
        mIvPlayOrPause = (ImageView) findViewById(R.id.ivPlayOrPause);
        mTvMusicDuration = (TextView) findViewById(R.id.tvCurrentTime);
        mTvTotalMusicDuration = (TextView) findViewById(R.id.tvTotalTime);
        musicName = (TextView) findViewById(R.id.musicName);
        musicArtist = (TextView) findViewById(R.id.musicArtist);
        mSeekBar = (SeekBar) findViewById(R.id.musicSeekBar);
        mRootLayout = (BackgourndAnimationRelativeLayout) findViewById(R.id.rootLayout);

        mToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(mToolbar);

        mIvLast.setOnClickListener(this);
        mIvNext.setOnClickListener(this);
        mIvPlayOrPause.setOnClickListener(this);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTvMusicDuration.setText(duration2Time(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopUpdateSeekBarProgree();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() >= duration) {
                    next();
                } else {
                    seekTo(seekBar.getProgress());
                    mLrcView.seekLrcToTime(seekBar.getProgress());
                    startUpdateSeekBarProgress();
                }
            }
        });


        List<LrcRow> lrcRows = new LrcDataBuilder().BuiltFromAssets(this, "小幸运.lrc");
        mLrcView.setLrcData(lrcRows);
        mLrcView.setLrcViewSeekListener(new ILrcView.LrcViewSeekListener() {
            @Override
            public void onSeek(LrcRow currentlrcrow, long Currenselectrowtime) {
                seekTo((int) Currenselectrowtime);
                mLrcView.seekLrcToTime(Currenselectrowtime);
            }
        });

        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
    }

    private void stopUpdateSeekBarProgree() {
        mMusicHandler.removeMessages(MUSIC_MESSAGE);
    }

    /*设置透明状态栏*/
    private void makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    //更改为操作数据库
    private void initMusicDatas(int musicId) {
        MusicData musicData1 = new MusicData(R.raw.music1, R.raw.ic_music1, "寻", "三亩地");
        MusicData musicData2 = new MusicData(R.raw.music2, R.raw.ic_music2, "Nightingale", "YANI");
        MusicData musicData3 = new MusicData(R.raw.music3, R.raw.ic_music3, "Cornfield Chase", "Hans Zimmer");

        mMusicDatas.add(musicData1);
        mMusicDatas.add(musicData2);
        mMusicDatas.add(musicData3);


    }

    //传入参数变成文件系统url
    private void try2UpdateMusicPicBackground(final int musicPicRes) {
        if (mRootLayout.isNeed2UpdateBackground(musicPicRes)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    final Drawable foregroundDrawable = getForegroundDrawable(musicPicRes);
                    final Drawable foregroundDrawable = BackgroundAdapter.getForegroundDrawable(PlayMusicActivity.this, musicPicRes);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRootLayout.setForeground(foregroundDrawable);
                            mRootLayout.beginAnimation();
                        }
                    });
                }
            }).start();
        }
    }

    public void musicChanged() {
        musicName.setText(musicList.get(musicId).getName());
        musicArtist.setText(musicList.get(musicId).getArtist());
    }

    public void MusicInfoChanged(String musicName, String musicAuthor) {
        getSupportActionBar().setTitle(musicName);
        getSupportActionBar().setSubtitle(musicAuthor);
    }


    public void MusicPicChanged(int musicPicRes) {
        try2UpdateMusicPicBackground(musicPicRes);
    }

//    public void MusicChanged(DiscView.MusicChangedStatus musicChangedStatus) {
//        switch (musicChangedStatus) {
//            case PLAY: {
//                play();
//                Log.d(App.TAG, musicChangedStatus + "///");
//                break;
//            }
//            case PAUSE: {
//                pause();
//                break;
//            }
//            case NEXT: {
//                next();
//                break;
//            }
//            case LAST: {
//                last();
//                break;
//            }
//            case STOP: {
//                stop();
//                break;
//            }
//        }
//    }

    @Override
    public void onClick(View v) {
        if (v == mIvPlayOrPause) {
            playOrPause();
        } else if (v == mIvNext) {
            next();
        } else if (v == mIvLast) {
            last();
        }
    }

    private void playOrPause() {
        if (isPaused) {
            optMusic(MusicService.ACTION_OPT_MUSIC_PLAY);
            startUpdateSeekBarProgress();
        } else {
            optMusic(MusicService.ACTION_OPT_MUSIC_PAUSE);
            stopUpdateSeekBarProgree();
        }
    }

    private void stop() {
        stopUpdateSeekBarProgree();
        mIvPlayOrPause.setImageResource(R.drawable.ic_play);
        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
        mSeekBar.setProgress(0);
    }

    private void next() {
//        mRootLayout.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                musicId = (musicId + 1) % musicList.size();
//                optMusic(MusicService.ACTION_OPT_MUSIC_NEXT);
//            }
//        }, DURATION_NEEDLE_ANIAMTOR);
        mRootLayout.post(new Runnable() {
            @Override
            public void run() {
                musicId = (musicId + 1) % musicList.size();
                optMusic(MusicService.ACTION_OPT_MUSIC_NEXT);
            }
        });
        stopUpdateSeekBarProgree();
        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
    }

    private void last() {
//        mRootLayout.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                musicId = (musicId - 1) % musicList.size();
//                optMusic(MusicService.ACTION_OPT_MUSIC_LAST);
//            }
//        }, DURATION_NEEDLE_ANIAMTOR);
        mRootLayout.post(new Runnable() {
            @Override
            public void run() {
                musicId = (musicId + musicList.size() - 1) % musicList.size();
                optMusic(MusicService.ACTION_OPT_MUSIC_LAST);
            }
        });
        stopUpdateSeekBarProgree();
        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
    }

//    private void complete(boolean isOver) {
//        if (isOver) {
//            mDisc.stop();
//        } else {
//            mDisc.next();
//        }
//    }

    private void optMusic(final String action) {
        Intent intent = new Intent(action);
        intent.putExtra("musicId", musicId);
        Log.d("sillyIndex", musicId + "");
        sendBroadcast(intent);
    }

    private void seekTo(int position) {
        Intent intent = new Intent(MusicService.ACTION_OPT_MUSIC_SEEK_TO);
        intent.putExtra(MusicService.PARAM_MUSIC_SEEK_TO, position);
        sendBroadcast(intent);
    }

    private void startUpdateSeekBarProgress() {
        /*避免重复发送Message*/
        stopUpdateSeekBarProgree();
        mMusicHandler.sendEmptyMessageDelayed(0, 1000);
    }

    /*根据时长格式化称时间文本*/
    private String duration2Time(int duration) {
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        return (min < 10 ? "0" + min : min + "") + ":" + (sec < 10 ? "0" + sec : sec + "");
    }

    private void updateMusicDurationInfo(int totalDuration) {
        mSeekBar.setProgress(0);
        mSeekBar.setMax(totalDuration);
        mTvTotalMusicDuration.setText(duration2Time(totalDuration));
        mTvMusicDuration.setText(duration2Time(0));
        startUpdateSeekBarProgress();
    }

    private void updateSecondaryProgress(int cacheProgress) {
        Log.d(App.TAG, "secpro" + duration * cacheProgress / 100);
        mSeekBar.setSecondaryProgress(duration * cacheProgress / 100);
    }

    class MusicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MusicService.REFRESH_ID)) {
                musicId = intent.getIntExtra("musicId", musicId);
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_PLAY)) {
                mIvPlayOrPause.setImageResource(R.drawable.ic_pause);
                musicName.setText(intent.getStringExtra("musicName"));
                musicArtist.setText(intent.getStringExtra("musicArtist"));
                isPaused = false;
                int currentPosition = intent.getIntExtra(MusicService.PARAM_MUSIC_CURRENT_POSITION, 0);
                Log.d(App.TAG, currentPosition + "???");
                mSeekBar.setProgress(currentPosition);
//                if (!mDisc.isPlaying()) {
//                    mDisc.playOrPause();
//                }
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_PAUSE)) {
                isPaused = true;
                mIvPlayOrPause.setImageResource(R.drawable.ic_play);
                stopUpdateSeekBarProgree();
                Log.d(App.TAG, "here");
//                if (mDisc.isPlaying()) {
//                    mDisc.playOrPause();
//                }
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_DURATION)) {
                Log.d("herehere", "adfhkdhfal");
                duration = intent.getIntExtra(MusicService.PARAM_MUSIC_DURATION, 0);
                updateMusicDurationInfo(duration);
                updateSecondaryProgress(100);
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_COMPLETE)) {
                boolean isOver = intent.getBooleanExtra(MusicService.PARAM_MUSIC_IS_OVER, true);
//                complete(isOver);
            } else if (action.equals(MusicService.ACTION_CACHE_PROGRESS)) {
                int secondaryProgress = intent.getIntExtra(MusicService.CACHE_PROGRESS, 0);
                updateSecondaryProgress(secondaryProgress);
            }
        }
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mMusicReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
