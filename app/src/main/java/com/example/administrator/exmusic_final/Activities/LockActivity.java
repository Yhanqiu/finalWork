package com.example.administrator.exmusic_final.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
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

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import static com.example.administrator.exmusic_final.widget.DiscView.DURATION_NEEDLE_ANIAMTOR;

public class LockActivity extends SwipeBackActivity implements View.OnClickListener,SensorEventListener {
    int[] play_pause = {R.drawable.ic_pause, R.drawable.ic_play};
    private SensorManager sensorManager;

    private ImageButton pauseBt;
    private ImageButton prevBt;
    private ImageButton nextBt;
    private TextView stepNum;
    private TextView lrcText;
    private LinearLayout lock_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


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
        stepNum = (TextView)findViewById(R.id.stepNum);
        lrcText = (TextView)findViewById(R.id.lrcText);
        lock_layout = (LinearLayout)findViewById(R.id.lock_layout);

        setPlayPauseBt(0);
        pauseBt.setOnClickListener(this);
        prevBt.setOnClickListener(this);
        nextBt.setOnClickListener(this);


        new Thread(new Runnable() {
            @Override
            public void run() {
                final Drawable foregroundDrawable = BackgroundAdapter.getForegroundDrawable(LockActivity.this,R.drawable.test_back);
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
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
    }

    private void optMusic(final String action) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(action));
    }

    public void setPlayPauseBt(int i) {
        if(App.isPause) {
            pauseBt.setImageResource(play_pause[(1+i)%2]);
        }else {
            pauseBt.setImageResource(play_pause[i]);
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.LPlayBt:
                setPlayPauseBt(1);
                if(App.isPause) {
                    optMusic(MusicService.ACTION_OPT_MUSIC_PLAY);
                }else {
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
        int steps=0;
        switch (event.sensor.getType()){
            case Sensor.TYPE_STEP_COUNTER:
                steps = (int) event.values[0];
                stepNum.setText(steps+"");

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
