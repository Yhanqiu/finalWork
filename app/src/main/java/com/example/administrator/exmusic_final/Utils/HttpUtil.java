package com.example.administrator.exmusic_final.Utils;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/9/27.
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }


    public static void downloadMp3(String queryId, final String MusicURL) {

        String url = "http:/172.25.107.112:8080/ExMusic/TestMusic?msg=music&id=" + queryId;

        sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null) {
                    InputStream inputStream = response.body().byteStream();
                    if(inputStream!=null){
                        FileUtils.CreateFileFromStream(MusicURL, inputStream);
                    }
                    inputStream.close();
                }
            }
        });

    }

    public static void downloadImage(String queryId, final String imageURL) {

        String url = "http:/172.25.107.112:8080/ExMusic/TestMusic?msg=image&id=" + queryId;

        sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null) {
                    InputStream inputStream = response.body().byteStream();
                    if (inputStream != null) {
                        FileUtils.CreateFileFromStream(imageURL, inputStream);
                    }
                    inputStream.close();
                }
            }
        });

    }

    public static void downloadLrc(String queryId, final String lrcURL) {

        String url = "http:/172.25.107.112:8080/ExMusic/TestMusic?msg=lrc&id=" + queryId;

        sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null) {
                    InputStream inputStream = response.body().byteStream();
                    if (inputStream != null) {
                        FileUtils.CreateFileFromStream(lrcURL, inputStream);

                    }
                    inputStream.close();
                }
            }
        });

    }
}
