package com.example.administrator.exmusic_final.Utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.example.administrator.exmusic_final.db.Music;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Administrator on 2017/9/27.
 */

public class MusicUtil {


    public static void sendTestRequest(okhttp3.Callback callback){
        String url = "http://172.25.107.112:8080/ExMusic/TestMusic";
        FormBody formBody = new FormBody.Builder()
                .add("msg","music")
                .add("id","6").build();

        Request request = new Request.Builder()
                .post(formBody)
                .url(url)
                .addHeader("User-Agent", "Apple")
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public static boolean handleMusicRequest(String response, List<Music> musics) {

        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ExMusic/";
        int result = FileUtils.CreateFile(path + "/ExMusic/test.txt");
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allMusic = new JSONArray(response);
                for (int i = 0; i < allMusic.length(); i++) {
                    JSONObject musicObject = allMusic.getJSONObject(i);
                    Music music = new Music();
                    String musicName = musicObject.getString("name");
                    music.setName(musicName);
                    music.setArtist(musicObject.getString("artist"));
                    music.setQueryId(musicObject.getString("id"));
                    music.setMusicURL(path+"musics/"+musicName+".mp3");
                    music.setImageURL(path+"images/"+musicName+".jgp");
                    music.setLrcURL(path+"lyrics/"+musicName+".lrc");
//                    music.setMusicURL(musicObject.getString("musicURL"));
//                    music.setImageURL(musicObject.getString("imageURL"));
//                    music.setLrcURL(musicObject.getString("lrcURL"));
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
