package com.example.administrator.exmusic_final.widget;

import android.content.Context;
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
import com.example.administrator.exmusic_final.Utils.HttpUtil;
import com.example.administrator.exmusic_final.db.Music;

import java.util.List;

/**
 * Created by Administrator on 2017/10/13.
 */

public class LocalMusicAdapter extends ArrayAdapter<Music> {
    private int resourceId;

    public LocalMusicAdapter(Context context, int id, List<Music> musics) {
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
        downView.setVisibility(View.INVISIBLE);
        nameText.setText(music.getName());
        artistText.setText(music.getArtist());
        return view;
    }
}
