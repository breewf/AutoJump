package com.hy.autojump;

import android.accessibilityservice.AccessibilityService;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.hy.autojump.common.Global;
import com.hy.autojump.event.ActivityChangedEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * @author hy
 * @date 2020/4/29
 * ClassDesc:检测服务.
 **/
public class TrackerService extends AccessibilityService {

    public static final String TAG = "TrackerService";

    public static final String COMMAND = "COMMAND";
    public static final String COMMAND_OPEN = "COMMAND_OPEN";
    public static final String COMMAND_CLOSE = "COMMAND_CLOSE";

    public static final int IGNORE_TIME = 5000;

    public static final String JUMP = "跳过";

    public static final String WEI_XIN__PACKAGE = "com.tencent.mm";
    public static final String QQ__PACKAGE = "com.tencent.mobileqq";

    public static final String A_LI_PAY_PACKAGE = "com.eg.android.AlipayGphone";
    public static final String A_LI_PAY_ANT_FOREST_CLASS = "com.alipay.mobile.nebulax.integration.mpaas.activity.NebulaActivity";
    public static final String ANT_FOREST_TITLE = "蚂蚁森林";

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
     * 是否是蚂蚁森林页面
     */
    private boolean mAntForest;

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
        addWhiteList();

//        AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
//        accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
//        accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
//        accessibilityServiceInfo.notificationTimeout = 1000;
//        setServiceInfo(accessibilityServiceInfo);
    }

    private void addWhiteList() {
        if (mPackageWhiteList == null) {
            return;
        }
        if (!mPackageWhiteList.contains(getPackageName())) {
            mPackageWhiteList.add(getPackageName());
        }
        if (!mPackageWhiteList.contains(WEI_XIN__PACKAGE)) {
            mPackageWhiteList.add(WEI_XIN__PACKAGE);
        }
        if (!mPackageWhiteList.contains(QQ__PACKAGE)) {
            mPackageWhiteList.add(QQ__PACKAGE);
        }
        if (!mPackageWhiteList.contains(A_LI_PAY_PACKAGE)) {
            mPackageWhiteList.add(A_LI_PAY_PACKAGE);
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
            mAntForest = false;
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

        // checkEvent(event);

        functionAutoJump();

        // functionAutoGetAntPower();

        if (!mPackageNameTemp.equals(mPackageName)) {
            mPackageNameTemp = mPackageName;
        }
    }

    /**
     * 功能--蚂蚁森林，自动收取能量
     */
    private void functionAutoGetAntPower() {
        if (!Global.AUTO_GET_POWER) {
            return;
        }
        if (!mPackageName.equals(A_LI_PAY_PACKAGE)) {
            // 不是支付宝
            return;
        }
        if (!mClassName.contains(A_LI_PAY_ANT_FOREST_CLASS)) {
            // 不是蚂蚁森林所在页面
            mAntForest = false;
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "蚂蚁森林-->>发现蚂蚁森林页面!!!!");
        }

        // 开始检测
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        checkAccessibilityNodeInfoForGetPower(nodeInfo);
    }

    /**
     * 蚂蚁森林收能量
     */
    public void checkAccessibilityNodeInfoForGetPower(AccessibilityNodeInfo nodeInfo) {
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
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "NodeInfo: " + i + " "
                        + "className:" + className + " : "
                        + childNodeInfo.getContentDescription() + " : "
                        + textContent);
            }

            if (!TextUtils.isEmpty(textContent)
                    && textContent.contains(ANT_FOREST_TITLE)) {
                mAntForest = true;
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "蚂蚁森林-->>发现蚂蚁森林!!!!");
                }
            }

            if (mAntForest && !TextUtils.isEmpty(textContent)
                    && !textContent.contains("收取")
                    && !textContent.contains("获得")
                    && (textContent.contains("g") || textContent.contains("绿色能量"))) {
                // 点击收取能量
                childNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "蚂蚁森林-->>收取一个能量-->>" + textContent);
                }
            }

            checkAccessibilityNodeInfoForGetPower(childNodeInfo);
        }
    }

    /**
     * 功能--自动跳过
     */
    private void functionAutoJump() {
        if (!Global.AUTO_JUMP) {
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
                    new Handler().postDelayed(new Runnable() {
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
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "检测APP-->>白名单APP:" + mPackageName);
                }
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "检测APP-->>系统APP:" + mPackageName);
            }
        }
    }

    /**
     * 自动跳过检测
     */
    public void checkAccessibilityNodeInfoForJump(AccessibilityNodeInfo nodeInfo) {
        if (mAutoJump && !mOpenNewApp) {
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
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "NodeInfo: " + i + " "
                        + "className:" + className + " : "
                        + childNodeInfo.getContentDescription() + " : "
                        + textContent);
            }

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

//                Rect outBounds = new Rect();
//                childNodeInfo.getBoundsInScreen(outBounds);
//                simulationClick(outBounds.centerX(), outBounds.centerY());
//                execCmd(outBounds.centerX(), outBounds.centerY());

                break;
            } else {
                checkAccessibilityNodeInfoForJump(childNodeInfo);
            }
        }
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
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                eventText = "TYPE_TOUCH_EXPLORATION_GESTURE_END";
                break;
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                eventText = "TYPE_ANNOUNCEMENT";
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                eventText = "TYPE_TOUCH_EXPLORATION_GESTURE_START";
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
                break;
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
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
            boolean isSysApp = (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
            boolean isSysUpd = (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1;
            isSystemApp = isSysApp || isSysUpd;
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
    private void simulationClick(int x, int y) {
        String[] order = {"input", "tap", " ", x + "", y + ""};
        try {
            new ProcessBuilder(order).start();
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "检测APP-->>模拟点击!!!!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "检测APP-->>模拟点击--IOException!!!!");
            }
        }
    }

    /**
     * 执行ADB命令：input tap 125 340
     */
    private void execCmd(int x, int y) {
//        String[] order = {"input", "tap", " ", x + "", y + ""};
        String order = "input" + " tap" + " " + x + " " + y;
        try {
            OutputStream os = Runtime.getRuntime().exec("su").getOutputStream();
            os.write(order.getBytes());
            os.flush();
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "检测APP-->>执行ADB命令!!!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "检测APP-->>执行ADB命令--Exception!!!!");
            }
        }
    }

    /**
     * 模拟点击
     * 需要系统签名
     *
     * @param x x
     * @param y y
     */
    private void simulationClick2(int x, int y) {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0));
            inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 全局事件模拟（返回键：AccessibilityService.GLOBAL_ACTION_BACK）
     */
    public boolean clickBack() {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
