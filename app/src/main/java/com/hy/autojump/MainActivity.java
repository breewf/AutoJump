package com.hy.autojump;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hy.autojump.event.Actions;
import com.hy.autojump.event.Event;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import de.greenrobot.event.EventBus;

/**
 * @author hy
 * @date 2020/4/29
 * ClassDesc:MainActivity.
 **/
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;

    public FloatWindowManager mTrackerWindowManager;

    private TextView mServiceStatusTv;

    private Switch mSwitch1;
    private Switch mSwitch2;
    private Switch mSwitchOpenFloat;

    private boolean mFloatViewAdd;

    private boolean mAccessibilityStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        initTrackerWindowManager();

        mServiceStatusTv = findViewById(R.id.tv_service_status);
        mSwitch1 = findViewById(R.id.switch1);
        mSwitch2 = findViewById(R.id.switch2);
        mSwitchOpenFloat = findViewById(R.id.btn_open_float);

        initSwitchStatus();

        initClickListener();
    }

    private void initSwitchStatus() {

    }

    private void initClickListener() {
        mSwitch1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAccessibility();
            }
        });

        mSwitch2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mSwitchOpenFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFloatViewAdd) {
                    removeFloatView();
                    return;
                }
                if (checkCanDrawOverlays(true)) {
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

    private void setServiceStatus() {
        if (mServiceStatusTv == null) {
            return;
        }
        if (AccessibilityUtils.checkAccessibility(MainActivity.this, false)) {
            mServiceStatusTv.setText("服务已开启");
            mServiceStatusTv.setTextColor(ContextCompat.getColor(this, R.color.color_green_400));
            mSwitch1.setChecked(true);
            mSwitch1.setClickable(false);
            mAccessibilityStart = true;
        } else {
            mServiceStatusTv.setText("服务未开启");
            mServiceStatusTv.setTextColor(ContextCompat.getColor(this, R.color.color_red_400));
            mSwitch1.setChecked(false);
            mSwitch1.setClickable(true);
            mAccessibilityStart = false;
        }
    }

    /**
     * 悬浮窗权限
     */
    private boolean checkCanDrawOverlays(boolean goSetting) {
        if (!Settings.canDrawOverlays(this)) {
            if (goSetting) {
                startActivityForResult(
                        new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())),
                        REQUEST_CODE
                );
                Toast.makeText(this, "请先授予悬浮窗权限", Toast.LENGTH_LONG).show();
            }
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
        if (AccessibilityUtils.checkAccessibility(MainActivity.this, true)) {
            startService(new Intent(MainActivity.this, TrackerService.class)
                    .putExtra(TrackerService.COMMAND, TrackerService.COMMAND_OPEN)
            );
        }
    }

    /**
     * 打开悬浮框
     */
    private void addFloatView() {
        if (mTrackerWindowManager != null) {
            mTrackerWindowManager.addView();
            if (mSwitchOpenFloat != null) {
                mSwitchOpenFloat.setChecked(true);
            }
            mFloatViewAdd = true;
        }
    }

    /**
     * 关闭悬浮框
     */
    private void removeFloatView() {
        if (mTrackerWindowManager != null) {
            mTrackerWindowManager.removeView();
            if (mSwitchOpenFloat != null) {
                mSwitchOpenFloat.setChecked(false);
            }
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
            if (checkCanDrawOverlays(false)) {
                addFloatView();
            } else {
                if (mSwitchOpenFloat != null) {
                    mSwitchOpenFloat.setChecked(false);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setServiceStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}