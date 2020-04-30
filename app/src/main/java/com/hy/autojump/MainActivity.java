package com.hy.autojump;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hy.autojump.event.Actions;
import com.hy.autojump.event.Event;

import androidx.appcompat.app.AppCompatActivity;
import de.greenrobot.event.EventBus;

/**
 * @author hy
 * @date 2020/4/29
 * ClassDesc:MainActivity.
 **/
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;

    public FloatWindowManager mTrackerWindowManager;

    private TextView mStatusTv;

    private boolean mFloatViewAdd;

    private boolean mAccessibilityStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        initTrackerWindowManager();

        mStatusTv = findViewById(R.id.tv_status);
        setStatus();

        mStatusTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAccessibility();
            }
        });

        findViewById(R.id.btn_open_float).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFloatViewAdd) {
                    return;
                }
                if (checkCanDrawOverlays()) {
                    addFloatView();
                }
            }
        });
    }

    private void initTrackerWindowManager() {
        if (mTrackerWindowManager == null) {
            mTrackerWindowManager = new FloatWindowManager(this);
        }
    }

    private void setStatus() {
        if (mStatusTv == null) {
            return;
        }
        if (AccessibilityUtils.checkAccessibility(MainActivity.this)) {
            mStatusTv.setText("服务已开启");
            mStatusTv.setEnabled(false);
        } else {
            mStatusTv.setText("服务未开启");
            mStatusTv.setEnabled(true);
        }
    }

    /**
     * 悬浮窗权限
     */
    private boolean checkCanDrawOverlays() {
        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(
                    new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())),
                    REQUEST_CODE
            );
            Toast.makeText(this, "请先授予悬浮窗权限", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * 开启辅助功能
     */
    private void startAccessibility() {
        if (mAccessibilityStart) {
            return;
        }
        if (AccessibilityUtils.checkAccessibility(MainActivity.this)) {
            startService(new Intent(MainActivity.this, TrackerService.class)
                    .putExtra(TrackerService.COMMAND, TrackerService.COMMAND_OPEN)
            );
            mAccessibilityStart = true;
        }
    }

    /**
     * 添加悬浮框
     */
    private void addFloatView() {
        if (mTrackerWindowManager != null) {
            mTrackerWindowManager.addView();
            mFloatViewAdd = true;
        }
    }

    /**
     * 移除悬浮框
     */
    private void removeFloatView() {
        if (mTrackerWindowManager != null) {
            mTrackerWindowManager.removeView();
            mFloatViewAdd = false;
        }
    }

    public void onEventMainThread(Event event) {
        if (Actions.ACTIONS_CLOSE_FLOAT.equals(event.getAction())) {
            removeFloatView();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            super.onBackPressed();
        }
        //  super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            checkCanDrawOverlays();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}