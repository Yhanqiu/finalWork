package com.example.administrator.exmusic_final.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.administrator.exmusic_final.R;
import com.example.administrator.exmusic_final.db.Music;
import com.example.administrator.exmusic_final.widget.LocalMusicAdapter;
import com.example.administrator.exmusic_final.widget.MusicAdapter;

import java.util.ArrayList;
import java.util.List;

public class FunctionActivity extends AppCompatActivity {
    ImageView online_music;
    ListView functionList;

    LocalMusicAdapter localMusicAdapter;
    ArrayList<Music> localMusics = new ArrayList<Music>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function);
        getLocalMusics();
        initViews();
    }

    private void initViews() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        online_music = (ImageView) findViewById(R.id.online_music);
        online_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FunctionActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        functionList = (ListView)findViewById(R.id.functionList);

        localMusicAdapter = new LocalMusicAdapter(FunctionActivity.this, R.layout.music_item, localMusics);
        functionList.setAdapter(localMusicAdapter);
        functionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(FunctionActivity.this, PlayMusicActivity.class);
                intent.putExtra("localMusics", localMusics);
                intent.putExtra("musicId",position);
                startActivity(intent);
            }
        });
    }

    private void getLocalMusics() {

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, MediaStore.Audio.AudioColumns.IS_MUSIC);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Music song = new Music();
                song.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
                song.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                song.setMusicURL(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                Log.d("functionfunction",song.toString());
                if (cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)) > 1000 * 800) {
                    // 注释部分是切割标题，分离出歌曲名和歌手 （本地媒体库读取的歌曲信息不规范）
                    if (song.getName().contains("-")) {
                        String[] str = song.getName().split("-");
                        song.setArtist(str[0]);
                        song.setName(str[1]);
                    }
                    localMusics.add(song);
                }
            }
            // 释放资源
            cursor.close();
        }
    }

    @Override
    protected void onPause() {
        overridePendingTransition(0, 0);
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
