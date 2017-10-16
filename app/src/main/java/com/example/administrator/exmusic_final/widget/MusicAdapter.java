package com.example.administrator.exmusic_final.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.exmusic_final.R;
import com.example.administrator.exmusic_final.Utils.FileUtils;
import com.example.administrator.exmusic_final.Utils.HttpUtil;
import com.example.administrator.exmusic_final.db.Music;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/9/21.
 */

public class MusicAdapter extends ArrayAdapter<Music> {
    private int resourceId;

    public MusicAdapter(Context context, int id, List<Music> musics) {
        super(context, id, musics);
        resourceId = id;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Music music = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        TextView nameText = (TextView) view.findViewById(R.id.name_text);
        TextView artistText = (TextView) view.findViewById(R.id.artist_text);
        ImageView downView = (ImageView) view.findViewById(R.id.download_view);
        nameText.setText(music.getName());
        artistText.setText(music.getArtist());
        downView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String queryId = music.getQueryId();
                Toast.makeText(getContext(), music.getName(), Toast.LENGTH_SHORT).show();
                HttpUtil.downloadMp3(queryId, music.getMusicURL());
                HttpUtil.downloadImage(queryId, music.getImageURL());
                HttpUtil.downloadLrc(queryId, music.getLrcURL());
            }
        });
        Log.d("musicadapter", music.getName() + music.getArtist());
        return view;
    }
}
