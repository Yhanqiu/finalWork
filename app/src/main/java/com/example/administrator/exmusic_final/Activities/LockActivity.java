package com.example.administrator.exmusic_final.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.administrator.exmusic_final.App;
import com.example.administrator.exmusic_final.R;
import com.example.administrator.exmusic_final.Services.MusicService;
import com.example.administrator.exmusic_final.Utils.FileUtils;
import com.example.administrator.exmusic_final.Utils.HttpUtil;
import com.example.administrator.exmusic_final.db.Music;
import com.example.administrator.exmusic_final.widget.BackgroundAdapter;

import org.litepal.crud.DataSupport;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import me.wcy.lrcview.LrcView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class LockActivity extends SwipeBackActivity implements View.OnClickListener, SensorEventListener {
    private SensorManager sensorManager;
    private me.wcy.lrcview.LrcView nLrcView;
    private ArrayList<Music> musicList;
    private int currentPosition;
    private boolean ifPause;

    private ImageButton pauseBt;
    private ImageButton prevBt;
    private ImageButton nextBt;
    private TextView stepNum;
    //    private TextView lrcText;
    private TextView musicName;
    private TextView musicArtist;
    private int musicId;
    private ConstraintLayout lock_layout;

    private BroadcastReceiver playPauseReceiver;

    private Handler mMusicHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            currentPosition = currentPosition + 1000;
            nLrcView.updateTime(currentPosition);
            Log.d("locklock", currentPosition + "");
            startUpdateSeekBarProgress();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        initData(getIntent());
        initViews();
    }

    @Override
    protected void onStart() {
        initReceiver();
        super.onStart();
    }

    private void initData(Intent intent) {
        currentPosition = intent.getIntExtra("position", 0);
        musicList = (ArrayList<Music>) intent.getSerializableExtra("localMusics");
        ifPause = intent.getBooleanExtra("ifPause", false);
        musicId = intent.getIntExtra("musicId", 0);
    }

    private void initReceiver() {
        playPauseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(MusicService.ACTION_STATUS_MUSIC_PLAY)) {
                    ifPause = false;
                    currentPosition = intent.getIntExtra(MusicService.PARAM_MUSIC_CURRENT_POSITION, 0);
                    pauseBt.setImageResource(R.drawable.ic_pause);
                    musicId = intent.getIntExtra("musicId", 0);
                    musicName.setText(musicList.get(musicId).getName());
                    musicArtist.setText(musicList.get(musicId).getArtist());
                    startUpdateSeekBarProgress();
                } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_PAUSE)) {
                    ifPause = true;
                    pauseBt.setImageResource(R.drawable.ic_play);
                    stopUpdateSeekBarProgree();
                } else {
                    musicId = intent.getIntExtra("musicId", musicId);
                    File lrcFile = new File(musicList.get(musicId).getLrcURL());
                    File imageFile = new File(musicList.get(musicId).getImageURL());
                    if (lrcFile.exists()) {
                        nLrcView.loadLrc(lrcFile);
                    }
                    if (imageFile.exists()) {
                        refreshBackground();
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PAUSE);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PLAY);
        intentFilter.addAction(MusicService.REFRESH_ID);
        registerReceiver(playPauseReceiver, intentFilter);

    }

    private void startUpdateSeekBarProgress() {
        /*避免重复发送Message*/
        stopUpdateSeekBarProgree();
        mMusicHandler.sendEmptyMessageDelayed(0, 1000);
    }

    private void stopUpdateSeekBarProgree() {
        mMusicHandler.removeMessages(0);
    }

    private void initViews() {

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        pauseBt = (ImageButton) findViewById(R.id.LPlayBt);
        prevBt = (ImageButton) findViewById(R.id.LPrevBt);
        nextBt = (ImageButton) findViewById(R.id.LNextBt);
        stepNum = (TextView) findViewById(R.id.stepNum);
//        lrcText = (TextView) findViewById(R.id.lrcText);
        musicName = (TextView) findViewById(R.id.musicName);
        musicArtist = (TextView) findViewById(R.id.musicArtist);
        lock_layout = (ConstraintLayout) findViewById(R.id.lock_layout);

//        musicNameText = intent.getStringExtra();
//        musicArtistText = intent.getStringExtra();
        nLrcView = (LrcView) findViewById(R.id.lock_lyric_view);
        File lrcFile = new File(musicList.get(musicId).getLrcURL());
        updateLrc();
        if (ifPause) {
            pauseBt.setImageResource(R.drawable.ic_play);
        } else {
            pauseBt.setImageResource(R.drawable.ic_pause);
            startUpdateSeekBarProgress();
        }
//

//        lockscreen.putExtra("musicName",musicName);
//        lockscreen.putExtra("musicArtist",musicArtist);
//        lockscreen.putExtra("isPause",ifPause);

        pauseBt.setOnClickListener(this);
        prevBt.setOnClickListener(this);
        nextBt.setOnClickListener(this);
        refreshBackground();
    }


    private void updateLrc() {
        final File lrcFile = new File(musicList.get(musicId).getLrcURL());
        if (lrcFile.exists() && lrcFile.length() > 1540) {
            nLrcView.loadLrc(lrcFile);
        } else {
            String queryId = musicList.get(musicId).getQueryId();
            if (queryId != null) {
                String url = "http:/172.25.107.112:8080/ExMusic/TestMusic?msg=lrc&id=" + queryId;
                HttpUtil.sendOkHttpRequest(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response != null) {
                            InputStream inputStream = response.body().byteStream();
                            if (inputStream != null) {
                                int result = FileUtils.CreateFileFromStream(musicList.get(musicId).getLrcURL(), inputStream);
                                Log.d("result", result + "");
                                if (result == 1 && lrcFile.length() > 1540) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            nLrcView.loadLrc(new File(musicList.get(musicId).getLrcURL()));
                                        }
                                    });
                                }
                            }
                            inputStream.close();
                        }
                    }
                });
            }
        }
    }

    private void refreshBackground() {
        final File imageFile = new File(musicList.get(musicId).getImageURL());
        if (imageFile.exists() && imageFile.length() > 2048) {
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    final Drawable foregroundDrawable = getForegroundDrawable(musicPicRes);
                    final Drawable foregroundDrawable = BackgroundAdapter.getForegroundDrawableFromFile(LockActivity.this, musicList.get(musicId).getImageURL());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lock_layout.setBackground(foregroundDrawable);
                        }
                    });
                }
            }).start();
        } else {
            String queryId = musicList.get(musicId).getQueryId();
            if (queryId != null) {
                String url = "http:/172.25.107.112:8080/ExMusic/TestMusic?msg=image&id=" + queryId;
                HttpUtil.sendOkHttpRequest(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response != null) {
                            InputStream inputStream = response.body().byteStream();
                            if (inputStream != null) {
                                int result = FileUtils.CreateFileFromStream(musicList.get(musicId).getImageURL(), inputStream);
                                Log.d("result", result + "");
                                if (result == 1 && imageFile.length() > 2048) {
                                    final Drawable foregroundDrawable = BackgroundAdapter.getForegroundDrawableFromFile(LockActivity.this, musicList.get(musicId).getImageURL());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            lock_layout.setBackground(foregroundDrawable);
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            lock_layout.setBackground(getDrawable(R.drawable.ic_blackground));
                                        }
                                    });

                                }
                            }
                            inputStream.close();
                        }
                    }
                });
            }

        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                final Drawable foregroundDrawable;
//                if (new File(musicList.get(musicId).getImageURL()).exists()) {
//                    foregroundDrawable = BackgroundAdapter.getForegroundDrawableFromFile(LockActivity.this, musicList.get(musicId).getImageURL());
//                } else {
//                    foregroundDrawable = BackgroundAdapter.getForegroundDrawable(LockActivity.this, R.drawable.ic_blackground);
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        lock_layout.setBackground(foregroundDrawable);
//                    }
//                });
//            }
//        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {

        unregisterReceiver(playPauseReceiver);
        sensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
    }

    private void optMusic(final String action) {
        sendBroadcast(new Intent(action));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.LPlayBt:
                if (ifPause) {
                    ifPause = false;
                    optMusic(MusicService.ACTION_OPT_MUSIC_PLAY);
                } else {
                    ifPause = true;
                    optMusic(MusicService.ACTION_OPT_MUSIC_PAUSE);
                }
                break;
            case R.id.LPrevBt:
                optMusic(MusicService.ACTION_OPT_MUSIC_LAST);
                break;
            case R.id.LNextBt:
                optMusic(MusicService.ACTION_OPT_MUSIC_NEXT);
                break;
            default:
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int steps = 0;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_STEP_COUNTER:
                steps = (int) event.values[0];
                stepNum.setText(steps + "");

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
