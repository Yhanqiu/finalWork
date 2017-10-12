package com.example.administrator.exmusic_final.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.administrator.exmusic_final.R;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class ForgetPw extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ForgetrActivity";

    private EditText chIdEdit;
    private EditText chPwEdit;
    private Button confirmButton;

    private String idContent;
    private String pwContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pw);
        initViews();
    }

    //初始化界面
    private void initViews() {
        chIdEdit = (EditText) findViewById(R.id.chIdEdit);
        chPwEdit = (EditText) findViewById(R.id.chPwEdit);
        confirmButton = (Button) findViewById(R.id.confirmButton);

        confirmButton.setOnClickListener(this);
    }

    //sha-265单向散列函数
    public static String sha(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("sha-256");
            byte[] bytes = md5.digest((string).getBytes());
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


    //密码格式确认
    private void checkAndSave(String id, String pw) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        String encryptedPw = "";
        String saltStr = "";
        int salt = 0;
        //盐和加密后的密码密文分开存储
        SharedPreferences users = getSharedPreferences("us", MODE_APPEND);
        SharedPreferences salts = getSharedPreferences("salts", MODE_APPEND);

        if (!users.contains(id)) {
            Toast.makeText(this, "用户名不存在", Toast.LENGTH_SHORT).show();
        } else {
            if (pw.length() < 6 || pw.length() > 20) {
                Toast.makeText(this, "密码长度在6到20之间", Toast.LENGTH_SHORT).show();
            } else {
//============================sharedpreferences键值对存储=====================================
                SharedPreferences.Editor usEditor = users.edit();
                SharedPreferences.Editor saltsEditor = salts.edit();
                //随机数生成盐
                salt = new Random().nextInt(100000);
                saltStr = sha("" + salt);
                encryptedPw = sha(pw + saltStr);
                saltsEditor.putString(id, saltStr);
                usEditor.putString(id, encryptedPw);
                Toast.makeText(ForgetPw.this, encryptedPw, Toast.LENGTH_SHORT).show();
                saltsEditor.apply();
                usEditor.apply();

            }
        }
    }


    @Override
    public void onClick(View v) {
        Log.d(TAG, "clicknew");
        System.out.println("click");
        switch (v.getId()) {
            case R.id.confirmButton:
                Toast.makeText(this, "click", Toast.LENGTH_SHORT).show();

                idContent = chIdEdit.getText().toString();
                pwContent = chPwEdit.getText().toString();
                checkAndSave(idContent, pwContent);
                break;
            default:
                break;
        }
    }
}
