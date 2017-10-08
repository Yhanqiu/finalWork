package com.example.administrator.exmusic_final;

import android.app.Application;
import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;

import org.litepal.LitePalApplication;

/**
 * Created by Administrator on 2017/10/8.
 */

public class App extends LitePalApplication {
    public static String TAG = "testtest";
    public static boolean isPause = true;

    private HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy(Context context) {
        App app = (App) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);
    }
}
