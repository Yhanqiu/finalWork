package com.example.administrator.exmusic_final.Utils;

import android.text.TextUtils;

import com.example.administrator.exmusic_final.db.Music;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2017/9/27.
 */

public class MusicUtil {

    public static boolean handleMusicRequest(String response, List<Music> musics) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allMusic = new JSONArray(response);
                for (int i = 0; i < allMusic.length(); i++) {
                    JSONObject musicObject = allMusic.getJSONObject(i);
                    Music music = new Music();
                    music.setName(musicObject.getString("name"));
                    music.setArtist(musicObject.getString("artist"));
                    music.setMusicURL(musicObject.getString("musicURL"));
                    music.setImageURL(musicObject.getString("imageURL"));
                    music.setLrcURL(musicObject.getString("lrcURL"));
                    musics.add(music);
                    music.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
