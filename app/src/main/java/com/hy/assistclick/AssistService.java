package com.hy.assistclick;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.hy.assistclick.common.Global;
import com.hy.assistclick.event.ActivityChangedEvent;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author hy
 * @date 2020/4/29
 * ClassDesc:辅助功能服务.
 **/
public class AssistService extends AccessibilityService {

    public static final String TAG = "TrackerService";

    public static final String COMMAND = "COMMAND";
    public static final String COMMAND_OPEN = "COMMAND_OPEN";
    public static final String COMMAND_CLOSE = "COMMAND_CLOSE";

    public static final int IGNORE_TIME = 5000;

    public static final String JUMP = "跳过";
    public static final String LOGIN = "登录";

    public static final String WEI_XIN_PACKAGE = "com.tencent.mm";
    public static final String QQ_PACKAGE = "com.tencent.mobileqq";
    public static final String TAO_BAO = "com.taobao.taobao";

    public static final String TIK_TOK_PACKAGE = "com.ss.android.ugc.aweme";
    public static final String TIK_TOK_CLASS = "main.MainActivity";

    public static final String WEI_XIN_PC_LOGIN_CLASS = "com.tencent.mm.plugin.webwx.ui.ExtDeviceWXLoginUI";

    public static final String TAO_BAO_CLASS_MAIN = "com.taobao.tao.TBMainActivity";
    public static final String TAO_BAO_CLASS_PURCHASE = "com.taobao.android.purchase.TBPurchaseActivity";

    private int mScreenWidth;
    private int mScreenHeight;

    private FunctionManager mFunctionManager;

    private final static int TIMER_TASK_DELAY_NONE = 0;
    private final static int TIMER_TASK_DELAY_1000 = 1000;
    private final static int TIMER_TASK_1000 = 1000;

    private static AssistService mInstance;
    private ScheduledExecutorService mScheduledExecutorService;
    private TimerTask mTimerTask;

    /**
     * 包名白名单--过滤不检测
     */
    private List<String> mPackageWhiteList = new ArrayList<>();

    /**
     * 包名
     */
    private String mPackageName = "";
    private String mPackageNameTemp = "";

    /**
     * 类名
     */
    private String mClassName = "";
    private String mClassNameTemp = "";

    /**
     * 打开/切换到新的app
     */
    private boolean mOpenNewApp;

    /**
     * 是否已经自动跳过
     */
    private boolean mAutoJump;

    /**
     * 是否忽略抖音主页面划走广告
     */
    private boolean mIgnoreTikTokAd;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        String command = intent.getStringExtra(COMMAND);
        if (command != null) {
            if (command.equals(COMMAND_OPEN)) {
            } else if (command.equals(COMMAND_CLOSE)) {
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mInstance = this;
        mScreenWidth = getScreenWidth(this);
        mScreenHeight = getScreenHeight(this);
        addWhiteList();
    }

    public static AssistService getInstance() {
        return mInstance;
    }

    private void addWhiteList() {
        if (mPackageWhiteList == null) {
            return;
        }
        if (!mPackageWhiteList.contains(getPackageName())) {
            mPackageWhiteList.add(getPackageName());
        }
        if (!mPackageWhiteList.contains(WEI_XIN_PACKAGE)) {
            mPackageWhiteList.add(WEI_XIN_PACKAGE);
        }
        if (!mPackageWhiteList.contains(QQ_PACKAGE)) {
            mPackageWhiteList.add(QQ_PACKAGE);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 获取包名
        if (!TextUtils.isEmpty(event.getPackageName())) {
            mPackageName = event.getPackageName().toString();
        }

        mOpenNewApp = !mPackageNameTemp.equals(mPackageName);
        if (mOpenNewApp) {
            mAutoJump = false;
            mIgnoreTikTokAd = false;
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "检测APP-->>打开了新的App:" + mPackageName);
            }
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            if (!TextUtils.isEmpty(event.getClassName())) {
                mClassName = event.getClassName().toString();
            }

            if (Global.SEE_ACTIVITY) {
                if (!TextUtils.isEmpty(mPackageName) && !TextUtils.isEmpty(mClassName)) {
                    EventBus.getDefault().post(new ActivityChangedEvent(
                            mPackageName, mClassName
                    ));
                }
            }
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "PackageName-->>" + mPackageName);
            Log.i(TAG, "ClassName------>>" + mClassName);
        }

        // checkEvent(event);

        functionAutoJump();

        // functionTikTokAutoJumpAd();

        functionAutoLoginPcWeChat();

        // functionAutoTaoBao();

        if (!mPackageNameTemp.equals(mPackageName)) {
            mPackageNameTemp = mPackageName;
        }
    }

    /**
     * 功能--自动跳过
     */
    private void functionAutoJump() {
        if (!Global.AUTO_JUMP) {
            return;
        }
        if (mAutoJump) {
            return;
        }
        // 非系统app
        if (!isSystemApp(mPackageName)) {
            // 非白名单app
            if (!mPackageWhiteList.contains(mPackageName)) {
                // 开始检测
                AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                checkAccessibilityNodeInfoForJump(nodeInfo);

                if (mOpenNewApp) {
                    App.getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!mAutoJump) {
                                mAutoJump = true;
                                if (BuildConfig.DEBUG) {
                                    Log.i(TAG, "检测APP-->>超时，不再检测");
                                }
                            }
                        }
                    }, IGNORE_TIME);
                }
            } else {
                mAutoJump = true;
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "检测APP-->>白名单APP:" + mPackageName);
                }
            }
        } else {
            mAutoJump = true;
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "检测APP-->>系统APP:" + mPackageName);
            }
        }
    }

    /**
     * 自动跳过检测
     */
    public void checkAccessibilityNodeInfoForJump(AccessibilityNodeInfo nodeInfo) {
        if (mAutoJump) {
            return;
        }
        if (nodeInfo == null) {
            return;
        }
        if (nodeInfo.getChildCount() == 0) {
            return;
        }

        int size = nodeInfo.getChildCount();
        for (int i = 0; i < size; i++) {
            if (mAutoJump) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "检测APP-->>该APP已跳过，打断循环检测");
                }
                break;
            }
            AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
            if (childNodeInfo == null) {
                continue;
            }
            String className = "";
            if (!TextUtils.isEmpty(childNodeInfo.getClassName())) {
                className = childNodeInfo.getClassName().toString();
            }
            String textContent = "";
            if (!TextUtils.isEmpty(childNodeInfo.getText())) {
                textContent = childNodeInfo.getText().toString();
            }
            //if (BuildConfig.DEBUG) {
            //    Log.i(TAG, "NodeInfo: " + i + " "
            //            + "className:" + className + " : "
            //            + childNodeInfo.getContentDescription() + " : "
            //            + textContent);
            //}

            if (!TextUtils.isEmpty(textContent)
                    && textContent.contains(JUMP)) {
                // 点击跳过
                childNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                mAutoJump = true;
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "检测APP-->>自动跳过!!!!");
                }

                AccessibilityNodeInfo parentNodeInfo = childNodeInfo.getParent();
                if (parentNodeInfo != null) {
                    parentNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "检测APP-->>parent--自动跳过!!!!");
                    }
                }
                break;
            } else {
                checkAccessibilityNodeInfoForJump(childNodeInfo);
            }
        }
    }

    /**
     * 功能--抖音自动跳广告
     */
    private void functionTikTokAutoJumpAd() {
        if (!Global.AUTO_TIK_TOK_JUMP_AD) {
            return;
        }
        if (!mPackageName.equals(TIK_TOK_PACKAGE)) {
            // 不是抖音
            mIgnoreTikTokAd = true;
            return;
        }
        if (!mClassName.contains(TIK_TOK_CLASS)) {
            // 不是抖音主页面
            mIgnoreTikTokAd = true;
            return;
        }

        if (mIgnoreTikTokAd) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "TikTok-->>mIgnoreTikTokAd--return1111-->>");
            }
            return;
        }

        // 开始检测
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        checkAccessibilityNodeInfoForJumpTikTokAd(nodeInfo);
    }

    /**
     * 抖音跳过广告
     */
    public void checkAccessibilityNodeInfoForJumpTikTokAd(AccessibilityNodeInfo nodeInfo) {
        if (mIgnoreTikTokAd) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "TikTok-->>mIgnoreTikTokAd--return2222-->>");
            }
            return;
        }
        if (nodeInfo == null) {
            return;
        }
        if (nodeInfo.getChildCount() == 0) {
            return;
        }

        int size = nodeInfo.getChildCount();
        for (int i = 0; i < size; i++) {
            if (mIgnoreTikTokAd) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "TikTok-->>mIgnoreTikTokAd----break-->>");
                }
                break;
            }
            AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
            if (childNodeInfo == null) {
                continue;
            }
            String className = "";
            if (!TextUtils.isEmpty(childNodeInfo.getClassName())) {
                className = childNodeInfo.getClassName().toString();
            }
            String textContent = "";
            if (!TextUtils.isEmpty(childNodeInfo.getText())) {
                textContent = childNodeInfo.getText().toString();
            }
            //if (BuildConfig.DEBUG) {
            //    Log.i(TAG, "TikTok:NodeInfo: " + i + " "
            //            + "className:" + className + " : "
            //            + childNodeInfo.getContentDescription() + " : "
            //            + textContent);
            //}

            boolean check = !TextUtils.isEmpty(textContent)
                    && (textContent.contains("[t]") || textContent.contains("抖音小游戏"));
            boolean isVisibleToUser = childNodeInfo.isVisibleToUser();
            if (!mIgnoreTikTokAd && check && isVisibleToUser) {
                mIgnoreTikTokAd = true;
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "TikTok-->>发现广告视频-->>" + textContent);
                }
                // 划走
                gestureSwipeUp();
                break;
            } else {
                checkAccessibilityNodeInfoForJumpTikTokAd(childNodeInfo);
            }
        }
    }

    /**
     * 功能--自动登录电脑端微信
     */
    private void functionAutoLoginPcWeChat() {
        if (!Global.AUTO_WE_CHAT_LOGIN) {
            return;
        }
        if (!mPackageName.equals(WEI_XIN_PACKAGE)) {
            // 不是微信
            return;
        }
        if (!mClassName.equals(WEI_XIN_PC_LOGIN_CLASS)) {
            // 不是登录电脑端微信确认页面
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "检测自动登录电脑端微信-->>");
        }

        // 开始检测
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        checkAccessibilityNodeInfoForAutoLoginPcWeChat(nodeInfo);
    }

    /**
     * 自动登录电脑端微信
     */
    public void checkAccessibilityNodeInfoForAutoLoginPcWeChat(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        if (nodeInfo.getChildCount() == 0) {
            return;
        }

        int size = nodeInfo.getChildCount();
        for (int i = 0; i < size; i++) {
            AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
            if (childNodeInfo == null) {
                continue;
            }
            String className = "";
            if (!TextUtils.isEmpty(childNodeInfo.getClassName())) {
                className = childNodeInfo.getClassName().toString();
            }
            String textContent = "";
            if (!TextUtils.isEmpty(childNodeInfo.getText())) {
                textContent = childNodeInfo.getText().toString();
            }
            //if (BuildConfig.DEBUG) {
            //    Log.i(TAG, "TikTok:NodeInfo: " + i + " "
            //            + "className:" + className + " : "
            //            + childNodeInfo.getContentDescription() + " : "
            //            + textContent);
            //}

            boolean check = !TextUtils.isEmpty(textContent)
                    && textContent.equals(LOGIN);
            if (check) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "WeChat-->>登录电脑端微信-->>" + textContent);
                }
                // 点击登录
                childNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            } else {
                checkAccessibilityNodeInfoForAutoLoginPcWeChat(childNodeInfo);
            }
        }
    }

    /**
     * 功能--淘宝
     */
    private void functionAutoTaoBao() {
        if (!Global.AUTO_TAO_BAO) {
            return;
        }
        if (!mPackageName.equals(TAO_BAO)) {
            return;
        }
        boolean isTargetPage = mClassName.equals(TAO_BAO_CLASS_MAIN) ||
                mClassName.equals(TAO_BAO_CLASS_PURCHASE);
        if (!isTargetPage) {
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "淘宝页面-->>");
        }

        // 开始检测
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        checkAccessibilityNodeInfoForAutoTaoBao(nodeInfo);
    }

    /**
     * 淘宝
     */
    public void checkAccessibilityNodeInfoForAutoTaoBao(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        if (nodeInfo.getChildCount() == 0) {
            return;
        }

        int size = nodeInfo.getChildCount();
        for (int i = 0; i < size; i++) {
            AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
            if (childNodeInfo == null) {
                continue;
            }
            String className = "";
            if (!TextUtils.isEmpty(childNodeInfo.getClassName())) {
                className = childNodeInfo.getClassName().toString();
            }
            String textContent = "";
            if (!TextUtils.isEmpty(childNodeInfo.getText())) {
                textContent = childNodeInfo.getText().toString();
            }
            //if (BuildConfig.DEBUG) {
            //    Log.i(TAG, "TikTok:NodeInfo: " + i + " "
            //            + "className:" + className + " : "
            //            + childNodeInfo.getContentDescription() + " : "
            //            + textContent);
            //}

            boolean check = !TextUtils.isEmpty(textContent)
                    && textContent.equals("结算");
            boolean check2 = !TextUtils.isEmpty(textContent)
                    && textContent.equals("提交订单");
            if (check || check2) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "淘宝页面--点击---->>" + textContent);
                }
                //childNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                if (check) {
                    dispatchClick(942, 1927);
                }
                if (check2) {
                    dispatchClick(924, 2060);
                }
                break;
            } else {
                checkAccessibilityNodeInfoForAutoTaoBao(childNodeInfo);
            }
        }
    }

    /**
     * 向上滑动屏幕
     */
    private void gestureSwipeUp() {
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path path = new Path();
        int x = mScreenWidth / 2;
        int y = mScreenHeight / 2;
        path.moveTo(x, y);
        path.lineTo(x, 200);
        GestureDescription gestureDescription = builder
                .addStroke(new GestureDescription.StrokeDescription(path,
                        100, 100))
                .build();
        dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                mIgnoreTikTokAd = false;
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "dispatchGesture===onCompleted");
                }
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                mIgnoreTikTokAd = false;
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "dispatchGesture===onCancelled");
                }
            }

        }, null);

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "TikTok-->>划走广告-->>!!!!");
        }

        startTimerTask();
    }

    /**
     * event事件
     */
    private void checkEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        String eventText = "";
        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                eventText = "TYPE_VIEW_CLICKED";
                AccessibilityNodeInfo noteInfo = event.getSource();
                Log.i(TAG, noteInfo.toString());
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                eventText = "TYPE_VIEW_FOCUSED";
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                eventText = "TYPE_VIEW_LONG_CLICKED";
                break;
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                eventText = "TYPE_VIEW_SELECTED";
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                eventText = "TYPE_VIEW_TEXT_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                eventText = "TYPE_WINDOW_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                eventText = "TYPE_NOTIFICATION_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                eventText = "TYPE_ANNOUNCEMENT";
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                eventText = "TYPE_TOUCH_EXPLORATION_GESTURE_START";
                // 触摸浏览事件开始
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "触摸浏览事件开始-->>");
                }
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                eventText = "TYPE_TOUCH_EXPLORATION_GESTURE_END";
                // 触摸浏览事件结束
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "触摸浏览事件结束-->>");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                eventText = "TYPE_VIEW_HOVER_ENTER";
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                eventText = "TYPE_VIEW_HOVER_EXIT";
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                eventText = "TYPE_VIEW_SCROLLED";
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                eventText = "TYPE_VIEW_TEXT_SELECTION_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                eventText = "TYPE_WINDOW_CONTENT_CHANGED";
                break;
            case AccessibilityEvent.TYPE_ASSIST_READING_CONTEXT:
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                break;
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
                // 触摸屏幕事件结束
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "触摸屏幕事件结束-->>");
                }
                break;
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
                // 触摸屏幕事件开始
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "触摸屏幕事件开始-->>");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
                break;
            case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED:
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                break;
            default:
                break;
        }
    }

    /**
     * 判断是否是系统app
     */
    public boolean isSystemApp(String pkgName) {
        boolean isSystemApp = false;
        PackageInfo pi = null;
        try {
            PackageManager pm = getApplicationContext().getPackageManager();
            pi = pm.getPackageInfo(pkgName, 0);
        } catch (Throwable t) {
            Log.e(TAG, t.getMessage(), t);
        }
        if (pi != null) {
            isSystemApp = (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
        }
        return isSystemApp;
    }

    /**
     * 根据View的Id搜索符合条件的节点，精确搜索方式;
     * 这个只适用于自己写的界面，因为Id可能重复
     * 示例："com.tencent.mm:id/co0"
     */
    public List<AccessibilityNodeInfo> findNodesById(String viewId) {
        AccessibilityNodeInfo nodeInfo = getRootNodeInfo();
        if (nodeInfo != null) {
            return nodeInfo.findAccessibilityNodeInfosByViewId(viewId);
        }
        return null;
    }

    /**
     * 根据Text搜索所有符合条件的节点，模糊搜索方式
     */
    public List<AccessibilityNodeInfo> findNodesByText(String text) {
        AccessibilityNodeInfo nodeInfo = getRootNodeInfo();
        if (nodeInfo != null) {
            return nodeInfo.findAccessibilityNodeInfosByText(text);
        }
        return null;
    }

    /**
     * 获取NodeInfo
     */
    private AccessibilityNodeInfo getRootNodeInfo() {
        AccessibilityNodeInfo nodeInfo;
        nodeInfo = getRootInActiveWindow();
        return nodeInfo;
    }

    private boolean performClick(List<AccessibilityNodeInfo> nodeInfos) {
        if (nodeInfos != null && !nodeInfos.isEmpty()) {
            AccessibilityNodeInfo node;
            for (int i = 0; i < nodeInfos.size(); i++) {
                node = nodeInfos.get(i);
                if (node != null && node.isEnabled()) {
                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
        return false;
    }

    /**
     * 模拟点击
     *
     * @param x x
     * @param y y
     */
    public void dispatchClick(int x, int y) {
        dispatchClick(x, y, null);
    }

    /**
     * 模拟点击
     *
     * @param x x
     * @param y y
     */
    public void dispatchClick(int x, int y, GestureResultCallback callback) {
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x + 1, y);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        // builder.addStroke(new GestureDescription.StrokeDescription(path, 0, ViewConfiguration.getTapTimeout()));
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 10));
        dispatchGesture(builder.build(), callback, null);
    }

    /**
     * 全局事件模拟（返回键：AccessibilityService.GLOBAL_ACTION_BACK）
     */
    public boolean clickBack() {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(metric);
        }
        return metric.heightPixels;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(metric);
        }
        return metric.widthPixels;
    }

    /**
     * TimerTask
     */
    private void startTimerTask() {
        if (mScheduledExecutorService == null) {
            mScheduledExecutorService = new ScheduledThreadPoolExecutor(3,
                    new BasicThreadFactory.Builder().namingPattern("scheduled-pool-%d").daemon(true).build());
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    mIgnoreTikTokAd = false;
                    cancelTimeTask();
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "TikTok-->>mIgnoreTikTokAd==false-->>!!!!");
                    }
                }
            };
        }
        mScheduledExecutorService.schedule(mTimerTask, TIMER_TASK_1000, TimeUnit.MILLISECONDS);
    }

    private void cancelTimeTask() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdown();
            mScheduledExecutorService = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelTimeTask();
        Log.d(TAG, "onDestroy");
    }
}
