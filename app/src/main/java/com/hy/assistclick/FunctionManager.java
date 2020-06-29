package com.hy.assistclick;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.hy.assistclick.common.Global;


/**
 * @author hy
 * @date 2020/6/29
 * Desc:FunctionManager
 */
public class FunctionManager {

    public static final String TAG = "TrackerService";

    public static final String WE_CHAT_PACKAGE = "com.tencent.mm";
    //public static final String WE_CHAT_PACKAGE = "com.huxiu";
    //public static final String WE_CHAT_ARTICLE_CLASS = "com.huxiu.ui.activity.ArticleDetailActivity";
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

    private boolean mAutoWeChatArticleStart;

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
                sendWeChatArticleGesture();
                startWeChatArticleGestureSwipeUp();
            }
        }
    };

    /**
     * 公众号文章辅助阅读
     */
    public void functionAutoWeChatArticle() {
        if (!Global.AUTO_WE_CHAT_ARTICLE) {
            return;
        }
        if (!mPackageName.equals(WE_CHAT_PACKAGE)) {
            return;
        }
        if (!mClassName.equals(WE_CHAT_ARTICLE_CLASS)) {
            if (mAutoWeChatArticleStart) {
                removeWeChatArticleGesture();
                mAutoWeChatArticleStart = false;
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "公众号文章辅助阅读--结束滑动监听-->>");
                }
            }
            return;
        }

        if (mAutoWeChatArticleStart) {
            return;
        }

        if (mAccessibilityService == null) {
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "公众号文章辅助阅读--开始滑动监听-->>");
        }

        sendWeChatArticleGesture();
        mAutoWeChatArticleStart = true;
    }

    private void sendWeChatArticleGesture() {
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(0, 6000);
        }
    }

    private void removeWeChatArticleGesture() {
        if (mHandler != null) {
            mHandler.removeMessages(0);
        }
    }

    /**
     * 向上滑动屏幕
     */
    private void startWeChatArticleGestureSwipeUp() {
        if (mAccessibilityService == null) {
            return;
        }
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path path = new Path();
        int x = mScreenWidth / 2;
        int y = mScreenHeight / 2;
        path.moveTo(x, y);
        path.lineTo(x, 200);
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
