package com.example.administrator.exmusic_final.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.example.administrator.exmusic_final.Activities.LockActivity;

public class LockService extends Service {
    BroadcastReceiver receiver;
    String musicName;
    String musicArtist;
    boolean ifPause;

    public LockService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("test","lockservice");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(Intent.ACTION_SCREEN_OFF)) {
                    Intent lockscreen = new Intent(LockService.this, LockActivity.class);
                    lockscreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    lockscreen.putExtra("musicName",musicName);
                    lockscreen.putExtra("musicArtist",musicArtist);
                    lockscreen.putExtra("ifPause",ifPause);
                    startActivity(lockscreen);
                } else if(action.equals(MusicService.ACTION_STATUS_MUSIC_PLAY)) {
                    musicName = intent.getStringExtra("musicName");
                    musicArtist = intent.getStringExtra("musicArtist");
                    ifPause = false;
                } else {
                    ifPause = true;
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(MusicService.ACTION_STATUS_MUSIC_PLAY);
        filter.addAction(MusicService.ACTION_STATUS_MUSIC_PAUSE);
        registerReceiver(receiver,filter);

    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
