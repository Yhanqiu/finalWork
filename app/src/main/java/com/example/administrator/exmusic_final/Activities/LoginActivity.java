package com.example.administrator.exmusic_final.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import com.example.administrator.exmusic_final.R;
import com.example.administrator.exmusic_final.Utils.FileUtils;
import com.example.administrator.exmusic_final.Utils.LoginUtil;
import com.example.administrator.exmusic_final.Utils.MyAuthCallback;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/10/10.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "loginActivity";

    // 界面控件
    private EditText idEdit;
    private EditText pwEdit;
    private Button loginButton;
    private Button registerButton;
    private TextView forgetText;
    private TextView sensorResult;
    private TextView usePwText;
    private ConstraintLayout fingerLayout;
    private ConstraintLayout pwLayout;

    private FingerprintManagerCompat fm = null;
    private MyAuthCallback myAuthCallback = null;
    private CancellationSignal cancellationSignal = null;

    private Handler handler = null;
    public static final int MSG_AUTH_SUCCESS = 100;
    public static final int MSG_AUTH_FAILED = 101;
    public static final int MSG_AUTH_ERROR = 102;
    public static final int MSG_AUTH_HELP = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
    }

    @Override
    protected void onStart() {
        Log.d("onstart","fad");
        super.onStart();

        if (checkLayout()) {
            pwLayout.setVisibility(View.INVISIBLE);
            fingerLayout.setVisibility(View.VISIBLE);
            sensorResult.setText("Touch sensor");
            sensorResult.setTextColor(getColor(R.color.grey));
            initFinger();
        } else {
            pwLayout.setVisibility(View.VISIBLE);
            fingerLayout.setVisibility(View.INVISIBLE);
        }
    }

    private boolean checkLayout() {
        SharedPreferences layout = getSharedPreferences("currentStatus", MODE_APPEND);
        boolean ifFinger = layout.getBoolean("confirmedFinger", false);
        Log.d("fingerfinger",layout.getBoolean("confirmedFinger", false)+"");
        return ifFinger;
    }

    private void initFinger() {
        fm = FingerprintManagerCompat.from(this);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Log.d(TAG, "msg: " + msg.what + " ,arg1: " + msg.arg1);
                switch (msg.what) {
                    case MSG_AUTH_SUCCESS:
                        setResultInfo(R.string.fingerprint_success);
                        SharedPreferences layout = getSharedPreferences("currentStatus", MODE_APPEND);
                        SharedPreferences.Editor layoutEditor = layout.edit();
                        layoutEditor.putBoolean("confirmedFinger", true);
                        layoutEditor.apply();
                        Log.d("fingerfinger",layout.getBoolean("confirmedFinger", false)+"");

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        break;
                    case MSG_AUTH_FAILED:
                        setResultInfo(R.string.fingerprint_not_recognized);
                        break;
                    case MSG_AUTH_ERROR:
                        handleErrorCode(msg.arg1);
                        break;
                    case MSG_AUTH_HELP:
                        handleHelpCode(msg.arg1);
                        break;
                }
            }
        };
        checkFingerEnable();
        if (cancellationSignal == null) {
            cancellationSignal = new CancellationSignal();
        }
        fm.authenticate(null, 0,
                cancellationSignal, myAuthCallback, null);
    }

    private void checkFingerEnable() {

        if (!fm.isHardwareDetected()) {
            // no fingerprint sensor is detected, show dialog to tell user.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.no_sensor_dialog_title);
            builder.setMessage(R.string.no_sensor_dialog_message);
            builder.setIcon(android.R.drawable.stat_sys_warning);
            builder.setCancelable(false);
            builder.setNegativeButton(R.string.cancel_btn_dialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            // show this dialog.
            builder.create().show();
        } else if (!fm.hasEnrolledFingerprints()) {
            // no fingerprint image has been enrolled.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.no_fingerprint_enrolled_dialog_title);
            builder.setMessage(R.string.no_fingerprint_enrolled_dialog_message);
            builder.setIcon(android.R.drawable.stat_sys_warning);
            builder.setCancelable(false);
            builder.setNegativeButton(R.string.cancel_btn_dialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            // show this dialog
            builder.create().show();
        } else {
            try {
                myAuthCallback = new MyAuthCallback(handler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    //界面初始化
    private void initViews() {
        idEdit = (EditText) findViewById(R.id.idEdit);
        pwEdit = (EditText) findViewById(R.id.PwEdit);
        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);
        forgetText = (TextView) findViewById(R.id.forgetText);
        sensorResult = (TextView) findViewById(R.id.sersor_result);
        usePwText = (TextView)findViewById(R.id.use_pw_text) ;
        usePwText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        pwLayout = (ConstraintLayout) findViewById(R.id.pw_login_layout);
        fingerLayout = (ConstraintLayout) findViewById(R.id.finger_login_layout);

        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        forgetText.setOnClickListener(this);
        usePwText.setOnClickListener(this);
    }

    //登录验证
    private void clickLogin() {
        final String account = idEdit.getText().toString();
        String password = pwEdit.getText().toString();
//        checkInput(account, password);
//        sendRequest("login",account,password);
        LoginUtil.sendLoginRequest("login", account, password, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("fromServer", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                response.body().close();
                Log.i("fromserver", "post: " + string);
                if (string.equals("true")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences layout = getSharedPreferences("currentStatus", MODE_APPEND);
                            SharedPreferences.Editor layoutEditor = layout.edit();
                            layoutEditor.putString("currentUser", account);
                            layoutEditor.apply();

                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setTitle("Tip");
                            builder.setMessage("Use fingerprint to enter.");
                            builder.setCancelable(true);
                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            });
                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    pwLayout.setVisibility(View.INVISIBLE);
                                    fingerLayout.setVisibility(View.VISIBLE);
                                    initFinger();
                                }
                            });
                            // show this dialog
                            builder.create().show();
                        }
                    });
                }else {
                    Toast.makeText(LoginActivity.this,"Wrong id or wrong password.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //验证算法
    private boolean checkInput(String account, String password) {
        SharedPreferences out = getSharedPreferences("us", MODE_APPEND);
        SharedPreferences salts = getSharedPreferences("salts", MODE_APPEND);
        String getPw = out.getString(account, "error");
        String saltStr = salts.getString(account, "");
//===========================文件读写存储===================================================
//        FileInputStream in = null;
//        BufferedReader reader=null;
//        StringBuilder content = new StringBuilder();
//        String line = "";
//        try{
//            in = openFileInput("users.txt");
//            reader=new BufferedReader(new InputStreamReader(in));
//            while ((line = reader.readLine())!=null){
//                Toast.makeText(this,line,Toast.LENGTH_SHORT).show();
////                Log.v("user",line);
////                content.append(line);
//            }
//        }catch (IOException e){
//            e.printStackTrace();
//        }finally {
//            if(reader!=null){
//                try{
//                    reader.close();
//                }catch (IOException e){
//                    e.printStackTrace();
//                }
//            }
//        }
//============================sharedpreferences键值对存储=====================================
        String calPw = sha(password + saltStr);
        if (getPw.equals("error")) {
            Toast.makeText(LoginActivity.this, "用户名不存在", Toast.LENGTH_SHORT).show();
        } else if (getPw.equals(calPw)) {
            Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "click" + getPw + "===" + calPw + "===" + saltStr);
        } else {
            Toast.makeText(LoginActivity.this, "登陆失败", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "click" + getPw + "===" + calPw + "===" + saltStr);
        }
        return true;
    }

    //跳转注册界面
    private void clickRegister() {
        Log.d(TAG, "clickRegister");
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    //跳转注册界面
    private void clickForget() {
        Log.d(TAG, "clickForget");
        Intent intent = new Intent(LoginActivity.this, ForgetPw.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginButton:
                clickLogin();
                break;
            case R.id.registerButton:
                clickRegister();
                break;
            case R.id.forgetText:
                clickForget();
                break;
            case R.id.use_pw_text:
                SharedPreferences layout = getSharedPreferences("currentStatus", MODE_APPEND);
                SharedPreferences.Editor layoutEditor = layout.edit();
                layoutEditor.putBoolean("confirmedFinger", false);
                layoutEditor.apply();
                Log.d("fingerfinger",layout.getBoolean("confirmedFinger", false)+"");
                pwLayout.setVisibility(View.VISIBLE);
                fingerLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void handleHelpCode(int code) {
        switch (code) {
            case FingerprintManager.FINGERPRINT_ACQUIRED_GOOD:
                setResultInfo(R.string.AcquiredGood_warning);
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_IMAGER_DIRTY:
                setResultInfo(R.string.AcquiredImageDirty_warning);
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_INSUFFICIENT:
                setResultInfo(R.string.AcquiredInsufficient_warning);
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_PARTIAL:
                setResultInfo(R.string.AcquiredPartial_warning);
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_TOO_FAST:
                setResultInfo(R.string.AcquiredTooFast_warning);
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_TOO_SLOW:
                setResultInfo(R.string.AcquiredToSlow_warning);
                break;
        }
    }

    private void handleErrorCode(int code) {
        switch (code) {
            case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
                setResultInfo(R.string.ErrorCanceled_warning);
                break;
            case FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE:
                setResultInfo(R.string.ErrorHwUnavailable_warning);
                break;
            case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
                setResultInfo(R.string.ErrorLockout_warning);
                break;
            case FingerprintManager.FINGERPRINT_ERROR_NO_SPACE:
                setResultInfo(R.string.ErrorNoSpace_warning);
                break;
            case FingerprintManager.FINGERPRINT_ERROR_TIMEOUT:
                setResultInfo(R.string.ErrorTimeout_warning);
                break;
            case FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
                setResultInfo(R.string.ErrorUnableToProcess_warning);
                break;
        }
    }

    private void setResultInfo(int stringId) {
        if (sensorResult != null) {
            if (stringId == R.string.fingerprint_success) {
                sensorResult.setTextColor(getColor(R.color.green));
            } else {
                sensorResult.setTextColor(getColor(R.color.red));
            }
            sensorResult.setText(stringId);
        }
    }
}
