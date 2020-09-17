package com.hy.assistclick;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


/**
 * @author hy
 * @date 2020/6/29
 * Desc:FunctionManager
 */
public class FunctionManager {

    public static final String TAG = "TrackerService";

    public static final String WE_CHAT_PACKAGE = "com.tencent.mm";
    public static final String WE_CHAT_ARTICLE_CLASS = "com.tencent.mm.plugin.brandservice.ui.timeline.preload.ui.TmplWebViewTooLMpUI";

    private AccessibilityService mAccessibilityService;

    /**
     * 包名
     */
    private String mPackageName = "";

    /**
     * 类名
     */
    private String mClassName = "";

    private int mScreenWidth;
    private int mScreenHeight;

    public FunctionManager(AccessibilityService accessibilityService) {
        mAccessibilityService = accessibilityService;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public void setClassName(String className) {
        mClassName = className;
    }

    public void setScreen(int screenWidth, int screenHeight) {
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {

            }
        }
    };

    /**
     * 向上滑动屏幕
     */
    private void startGestureSwipeUp() {
        if (mAccessibilityService == null) {
            return;
        }
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path path = new Path();
        float x = mScreenWidth / 2.0f;
        float y = mScreenHeight / 3.0f * 2;
        path.moveTo(x, y);
        path.lineTo(x, 300);
        GestureDescription gestureDescription = builder
                .addStroke(new GestureDescription.StrokeDescription(path,
                        100, 1000))
                .build();
        mAccessibilityService.dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "dispatchGesture===onCompleted");
                }
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "dispatchGesture===onCancelled");
                }
            }

        }, null);

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "startWeChatArticleGestureSwipeUp-->>");
        }
    }
}
