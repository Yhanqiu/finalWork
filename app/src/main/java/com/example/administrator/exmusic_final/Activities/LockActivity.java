package com.example.administrator.exmusic_final.Activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.administrator.exmusic_final.R;
import com.example.administrator.exmusic_final.widget.BackgroundAdapter;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class LockActivity extends SwipeBackActivity implements View.OnClickListener {
    int[] play_pause = {R.drawable.ic_pause, R.drawable.ic_play};
    int abab = 0;

    private ImageButton pauseBt;
    private ImageButton prevBt;
    private ImageButton nextBt;
    private LinearLayout lock_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        pauseBt = (ImageButton) findViewById(R.id.LPlayBt);
        prevBt = (ImageButton) findViewById(R.id.LPrevBt);
        nextBt = (ImageButton) findViewById(R.id.LNextBt);
        lock_layout = (LinearLayout)findViewById(R.id.lock_layout);

        pauseBt.setImageResource(play_pause[abab++ % 2]);
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
    public void onBackPressed() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.LPlayBt:
                pauseBt.setImageResource(play_pause[abab++ % 2]);
                break;
            case R.id.LPrevBt:
                break;
            case R.id.LNextBt:
                break;
            default:
        }
    }
}
