package com.hy.autojump;

import android.app.Application;

import com.hy.autojump.common.PreferManager;

/**
 * @author HY
 * @date 2020/5/1
 * Desc:
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PreferManager.init(this);
    }
}
