package com.example.administrator.exmusic_final.Activities;

import android.content.Intent;
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
import com.example.administrator.exmusic_final.Utils.LoginUtil;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "RegisterActivity";

    private EditText newIdEdit;
    private EditText newPwEdit;
    private Button RegSucButton;

    private String idContent;
    private String pwContent;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_reg);
            initViews();
        }
//初始化界面
    private void initViews(){
        newIdEdit = (EditText)findViewById(R.id.newIdEdit);
        newPwEdit = (EditText)findViewById(R.id.newPwEdit);
        RegSucButton = (Button)findViewById(R.id.RegSucButton);

        RegSucButton.setOnClickListener(this);
    }

    //密码格式确认
    private void checkAndSave(String id, String pw) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        String encryptedPw = "";
        String saltStr = "";
        int salt = 0;
        //盐和加密后的密码密文分开存储
        SharedPreferences users = getSharedPreferences("us",MODE_APPEND);
        SharedPreferences salts = getSharedPreferences("salts",MODE_APPEND);

        if (id.length()<3||id.length()>15){
            Toast.makeText(this,"用户名长度在3到15之间",Toast.LENGTH_SHORT).show();
        }else{
            if (pw.length()<6||pw.length()>20){
                Toast.makeText(this,"密码长度在6到20之间",Toast.LENGTH_SHORT).show();
            }else {
//                if (users.contains(id)){
//                    Toast.makeText(this,"用户名已被注册",Toast.LENGTH_SHORT).show();
//                }else{
//===========================文件读写存储===================================================
//                try{
//                    out = openFileOutput("users.txt", Context.MODE_APPEND);
//                    writer = new BufferedWriter(new OutputStreamWriter(out));
//                    Toast.makeText(this,id+","+pw,Toast.LENGTH_LONG).show();
//                    writer.write(id+","+pw);
//                    writer.close();
//                }catch (IOException e) {
//                    e.printStackTrace();
//                }finally {
//                    try{
//                        if(writer!=null) writer.close();
//                    }catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//============================sharedpreferences键值对存储=====================================
//                    SharedPreferences.Editor usEditor = users.edit();
//                    SharedPreferences.Editor saltsEditor = salts.edit();
//                    //随机数生成盐
//                    salt = new Random().nextInt(100000);
//                    saltStr = sha(""+salt);
//                    encryptedPw = sha(pw+saltStr);
//                    saltsEditor.putString(id,saltStr);
//                    usEditor.putString(id,encryptedPw);
//                    Toast.makeText(RegisterActivity.this,encryptedPw,Toast.LENGTH_SHORT).show();
//                    saltsEditor.apply();
//                    usEditor.apply();

//                    salt = new Random().nextInt(100000);
//                    saltStr = LoginUtil.sha(""+salt);
//                    encryptedPw = LoginUtil.sha(pw+saltStr);
                    LoginUtil.sendLoginRequest("register", id, pw, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String string = response.body().string();
                            Log.i("fromserver", "post: " + string);
                            if (string.equals("true")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    }
                                });
                            }else {
                                Toast.makeText(RegisterActivity.this,"用户名已存在",Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
//                }


            }
        }
    }
//密码加密存储
//    private String encryptPassword(int salt, String originPw){
//        String pwSave = sha(originPw+salt);
//        return pwSave;
//    }
    @Override
    public void onClick(View v) {

        Log.d(TAG,"clicknew");
        System.out.println("click");
        switch (v.getId()){
            case R.id.RegSucButton:
                idContent = newIdEdit.getText().toString();
                pwContent = newPwEdit.getText().toString();
                checkAndSave(idContent,pwContent);
                break;
            default:
                break;
        }
    }
}

