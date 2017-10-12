package com.example.administrator.exmusic_final.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2017/10/11.
 */

public class AccountUtil {
    public static String getcurrentAccount(Context context) {
        SharedPreferences layout = context.getSharedPreferences("users", Context.MODE_APPEND);
        return layout.getString("currentUser", null);
    }
}
