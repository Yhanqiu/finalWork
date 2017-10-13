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
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.administrator.exmusic_final.App;
import com.example.administrator.exmusic_final.R;
import com.example.administrator.exmusic_final.Services.MusicService;
import com.example.administrator.exmusic_final.db.Music;
import com.example.administrator.exmusic_final.widget.BackgroundAdapter;

import org.w3c.dom.Text;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;


public class LockActivity extends SwipeBackActivity implements View.OnClickListener, SensorEventListener {
    int[] play_pause = {R.drawable.ic_pause, R.drawable.ic_play};
    private SensorManager sensorManager;

    private ImageButton pauseBt;
    private ImageButton prevBt;
    private ImageButton nextBt;
    private TextView stepNum;
    private TextView lrcText;
    private TextView musicName;
    private TextView musicArtist;
    private ConstraintLayout lock_layout;

    private BroadcastReceiver playPauseReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        initViews(getIntent());
        initReceiver();

    }

    private void initReceiver() {
        playPauseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(MusicService.ACTION_STATUS_MUSIC_PLAY)) {
                    pauseBt.setImageResource(R.drawable.ic_pause);
                    musicName.setText(intent.getStringExtra("musicName"));
                    musicArtist.setText(intent.getStringExtra("musicArtist"));
                } else {
                    pauseBt.setImageResource(R.drawable.ic_play);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PAUSE);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PLAY);
        registerReceiver(playPauseReceiver, intentFilter);

    }

    private void initViews(Intent intent) {

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
        lrcText = (TextView) findViewById(R.id.lrcText);
        musicName = (TextView) findViewById(R.id.musicName);
        musicArtist = (TextView) findViewById(R.id.musicArtist);
        lock_layout = (ConstraintLayout) findViewById(R.id.lock_layout);

//        musicNameText = intent.getStringExtra();
//        musicArtistText = intent.getStringExtra();
        musicName.setText(intent.getStringExtra("musicName"));
        musicArtist.setText(intent.getStringExtra("musicArtist"));
        if (intent.getBooleanExtra("ifPause",false)) {
            pauseBt.setImageResource(R.drawable.ic_play);
        } else {
            pauseBt.setImageResource(R.drawable.ic_pause);
        }

//        lockscreen.putExtra("musicName",musicName);
//        lockscreen.putExtra("musicArtist",musicArtist);
//        lockscreen.putExtra("isPause",ifPause);

        pauseBt.setOnClickListener(this);
        prevBt.setOnClickListener(this);
        nextBt.setOnClickListener(this);
        refreshBackground();
    }


    private void refreshBackground() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Drawable foregroundDrawable = BackgroundAdapter.getForegroundDrawable(LockActivity.this, R.drawable.test_back);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lock_layout.setBackground(foregroundDrawable);
                    }
                });
            }
        }).start();

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
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(playPauseReceiver);
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
    }

    private void optMusic(final String action) {
        sendBroadcast(new Intent(action));
    }

    public void setPlayPauseBt(int i) {
        if (App.isPause) {
            pauseBt.setImageResource(play_pause[(1 + i) % 2]);
        } else {
            pauseBt.setImageResource(play_pause[i]);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.LPlayBt:
                if (App.isPause) {
                    optMusic(MusicService.ACTION_OPT_MUSIC_PLAY);
                } else {
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
