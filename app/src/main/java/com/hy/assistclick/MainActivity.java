package com.hy.assistclick;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hy.assistclick.common.Global;
import com.hy.assistclick.common.PreferManager;
import com.hy.assistclick.event.Actions;
import com.hy.assistclick.event.Event;

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
    private Switch mSwitch3;
    private Switch mSwitch4;
    private Switch mSwitch5;
    private Switch mSwitchOpenFloat;

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
        mSwitch3 = findViewById(R.id.switch3);
        mSwitch4 = findViewById(R.id.switch4);
        mSwitch5 = findViewById(R.id.switch5);
        mSwitchOpenFloat = findViewById(R.id.btn_open_float);

        initSwitchStatus();

        initClickListener();
    }

    private void initSwitchStatus() {
        mSwitch2.setChecked(Global.AUTO_JUMP = PreferManager.getAutoJumpConfig());
        mSwitch3.setChecked(Global.AUTO_GET_POWER = PreferManager.getAutoGetPowerConfig());
        mSwitch4.setChecked(Global.APP_TASK_HIDE = PreferManager.getAppTaskHideConfig());
        mSwitch5.setChecked(Global.AUTO_TIK_TOK_JUMP_AD = PreferManager.getAutoTikTokAdConfig());
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
                if (PreferManager.getAutoJumpConfig()) {
                    PreferManager.setAutoJumpConfig(false);
                    Global.AUTO_JUMP = false;
                } else {
                    PreferManager.setAutoJumpConfig(true);
                    Global.AUTO_JUMP = true;
                }
            }
        });

        mSwitch3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PreferManager.getAutoGetPowerConfig()) {
                    PreferManager.setAutoGetPowerConfig(false);
                    Global.AUTO_GET_POWER = false;
                } else {
                    PreferManager.setAutoGetPowerConfig(true);
                    Global.AUTO_GET_POWER = true;
                }
            }
        });

        mSwitch4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PreferManager.getAppTaskHideConfig()) {
                    PreferManager.setAppTaskHideConfig(false);
                    Global.APP_TASK_HIDE = false;
                } else {
                    PreferManager.setAppTaskHideConfig(true);
                    Global.APP_TASK_HIDE = true;
                }
            }
        });

        mSwitch5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PreferManager.getAutoTikTokAdConfig()) {
                    PreferManager.setAutoTikTokAdConfig(false);
                    Global.AUTO_TIK_TOK_JUMP_AD = false;
                } else {
                    PreferManager.setAutoTikTokAdConfig(true);
                    Global.AUTO_TIK_TOK_JUMP_AD = true;
                }
            }
        });

        mSwitchOpenFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Global.SEE_ACTIVITY) {
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
            startService(new Intent(MainActivity.this, AssistService.class)
                    .putExtra(AssistService.COMMAND, AssistService.COMMAND_OPEN)
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
            Global.SEE_ACTIVITY = true;
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
            Global.SEE_ACTIVITY = false;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_help) {
            startActivity(new Intent(this, HelpActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
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