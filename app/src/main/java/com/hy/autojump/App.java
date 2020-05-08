package com.hy.autojump;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.hy.autojump.common.PreferManager;

/**
 * @author HY
 * @date 2020/5/1
 * Desc:
 */
public class App extends Application {

    private static Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());

        PreferManager.init(this);
    }

    public static Handler getHandler() {
        return mHandler;
    }
}
