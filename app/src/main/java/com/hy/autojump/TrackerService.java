package com.hy.autojump;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.hy.autojump.event.ActivityChangedEvent;

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

    public static final String JUMP = "跳过";

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

    private boolean mOpenNewApp;
    private boolean mOpenNewClass;
    private boolean mAutoJump;

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

//        AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
//        accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
//        accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
//        accessibilityServiceInfo.notificationTimeout = 1000;
//        setServiceInfo(accessibilityServiceInfo);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 获取包名
        if (!TextUtils.isEmpty(event.getPackageName())) {
            mPackageName = event.getPackageName().toString();
        }
        Log.d(TAG, "onAccessibilityEvent:getPackageName:" + mPackageName);

        mOpenNewApp = !mPackageNameTemp.equals(mPackageName);

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            if (!TextUtils.isEmpty(event.getClassName())) {
                mClassName = event.getClassName().toString();
            }
            if (!mOpenNewApp) {
                mOpenNewClass = !mClassNameTemp.equals(mClassName);
            }
            Log.d(TAG, "onAccessibilityEvent:getClassName:" + mClassName);

            if (!TextUtils.isEmpty(mPackageName) && !TextUtils.isEmpty(mClassName)) {
                EventBus.getDefault().post(new ActivityChangedEvent(
                        mPackageName, mClassName
                ));
            }
        }

        // checkEvent(event);

        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        checkAccessibilityNodeInfoRecycle(nodeInfo);

        if (!mPackageNameTemp.equals(mPackageName)) {
            mPackageNameTemp = mPackageName;
        }
        if (!mClassNameTemp.equals(mClassName)) {
            mClassNameTemp = mClassName;
        }

//        if (mOpenNewApp) {
//            // 新打开的app，5s后不再检测
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mAutoJump = true;
//                }
//            }, 5000);
//        }
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
     * checkAccessibilityNodeInfo
     */
    public void checkAccessibilityNodeInfoRecycle(AccessibilityNodeInfo nodeInfo) {
        if (mAutoJump && !mOpenNewApp) {
            Log.i(TAG, "notice:检测APP-->>该APP已检测且跳过，不再检测");
            return;
        } else {
            mAutoJump = false;
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
                Log.i(TAG, "notice:检测APP-->>该APP已跳过，打断循环检测");
                break;
            }
            AccessibilityNodeInfo childInfo = nodeInfo.getChild(i);
            if (childInfo == null) {
                continue;
            }
            String className = childInfo.getClassName().toString();
            String textContent = "";
            if (!TextUtils.isEmpty(childInfo.getText())) {
                textContent = childInfo.getText().toString();
            }
            Log.i(TAG, "NodeInfo: " + i + " "
                    + "className:" + className + " : "
                    + childInfo.getContentDescription() + " : "
                    + textContent);

            if (!TextUtils.isEmpty(textContent) && textContent.equals(JUMP)) {
                if (childInfo.isEnabled()) {
                    // 点击跳过
                    childInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    mAutoJump = true;
                    Toast.makeText(this, "自动跳过", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
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
                // 获得点击View的类型
                // 进行模拟点击
                if (node.isEnabled()) {
                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
        return false;
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
