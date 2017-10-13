package com.example.administrator.exmusic_final.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.exmusic_final.widget.MusicAdapter;
import com.example.administrator.exmusic_final.R;
import com.example.administrator.exmusic_final.Services.LockService;
import com.example.administrator.exmusic_final.Utils.HttpUtil;
import com.example.administrator.exmusic_final.Utils.MusicUtil;
import com.example.administrator.exmusic_final.db.Music;

import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private ListView music_list;
    private List<Music> musics = new ArrayList<Music>();
    private List<String> nameList = new ArrayList<String>();
    private List<String> artistList = new ArrayList<String>();
    private MusicAdapter musicAdapter;
    private ArrayAdapter<String> adapter;
    private Button loginBt;
    private ImageView function_music;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        function_music = (ImageView)findViewById(R.id.function_music);
        function_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,FunctionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
//                overridePendingTransition(0, 0);
            }
        });

        Intent lockIntent = new Intent(MainActivity.this, LockService.class);
        startService(lockIntent);
        Log.d("test", "before lock");
        music_list = (ListView) findViewById(R.id.musicList);

//        Test.saveMusic();
//        musics = DataSupport.findAll(Music.class);

        musicAdapter = new MusicAdapter(MainActivity.this, R.layout.music_item, musics);
        music_list.setAdapter(musicAdapter);
        music_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, PlayMusicActivity.class);
                intent.putExtra("musicId", position);
                startActivity(intent);
            }
        });
//=========================================================================================
//        sendRequestTest();
        queryMusics();
    }

    private void sendRequestTest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
//                    URL url = new URL("http://172.25.107.112:8080/app/musics.json");
                    URL url = new URL("http://192.168.1.103:8080/test/musics.json");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    Log.d("testtest", response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void queryMusics() {
        musics.clear();
//        musics = DataSupport.findAll(Music.class);
        if (musics.size() > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    musicAdapter.notifyDataSetChanged();
                    music_list.setSelection(0);

                }
            });
            Log.e("music", musics.get(2).getMusicURL());
        } else {
            //更新缓存音乐列表，保证下次断网打开apk数据与本次退出保持一致
            DataSupport.deleteAll(Music.class);
            String address = "http://172.25.107.112:8080/ExMusic/TestMusic?msg=list";
//            String address = "http://192.168.1.103:8080/test/musics.json";
            queryFromServer(address);
            Log.e("music", "queryMusic failed");
        }

    }


    private void queryFromServer(String address) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(MainActivity.this, "Load failing.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Log.d("position",responseText);
                boolean result = false;
                result = MusicUtil.handleMusicRequest(responseText, musics);
                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            musicAdapter.notifyDataSetChanged();
                            music_list.setSelection(0);
                            closeProgressDialog();
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("loading...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
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
        }
        return super.onKeyDown(keyCode, event);
    }
}
