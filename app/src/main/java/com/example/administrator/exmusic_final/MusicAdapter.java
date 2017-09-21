package com.example.administrator.exmusic_final;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.administrator.exmusic_final.R;
import com.example.administrator.exmusic_final.db.Music;

import java.util.List;

/**
 * Created by Administrator on 2017/9/21.
 */

public class MusicAdapter extends ArrayAdapter<Music> {
    private int resourceId;

    public MusicAdapter (Context context, int id, List<Music> musics){
        super(context,id,musics);
        resourceId = id;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Music music = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,null);
        TextView nameText = (TextView)view.findViewById(R.id.name_text);
        TextView artistText = (TextView)view.findViewById(R.id.artist_text);
        nameText.setText(music.getName());
        artistText.setText(music.getArtist());
        return view;
    }
}
