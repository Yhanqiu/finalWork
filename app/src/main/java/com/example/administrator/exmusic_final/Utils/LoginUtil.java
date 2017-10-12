package com.example.administrator.exmusic_final.Utils;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.example.administrator.exmusic_final.Activities.LoginActivity;
import com.example.administrator.exmusic_final.Activities.MainActivity;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/10/10.
 */

public class LoginUtil {
    public static void sendLoginRequest(String msg, String name, String pw,okhttp3.Callback callback){
        String url = "http://172.25.107.112:8080/ExMusic/TestLogin";
        FormBody formBody = new FormBody.Builder()
                .add("msg",msg)
                .add("name", name)
                .add("password",pw).build();

        Request request = new Request.Builder()
                .post(formBody)
                .url(url)
                .addHeader("User-Agent", "Apple")
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(callback);
    }


    public static String sha(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("sha-256");
            byte[] bytes = md5.digest((string ).getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
