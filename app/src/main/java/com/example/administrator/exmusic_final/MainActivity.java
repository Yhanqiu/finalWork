package com.example.administrator.exmusic_final;

import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.administrator.exmusic_final.db.Music;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView music_list;
    private List<Music> musics = new ArrayList<Music>();
    private List<String> nameList = new ArrayList<String>();
    private List<String> artistList = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        music_list = (ListView) findViewById(R.id.musicList);

        Test.saveMusic();
        musics = DataSupport.findAll(Music.class);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nameList);
        MusicAdapter musicAdapter = new MusicAdapter(MainActivity.this,R.layout.music_item,musics);
        music_list.setAdapter(musicAdapter);
//        if (musics.size() > 0) {
//            for (Music m : musics) {
//                nameList.add(m.getName());
//                artistList.add(m.getArtist());
//                adapter.notifyDataSetChanged();
//                music_list.setSelection(0);
//            }
//        }

//        boolean a = getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER);
//        boolean b = getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
//        Log.d("testtest",a+",,"+b);
    }
}
