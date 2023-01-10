package com.hy.assistclick;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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
    private TextView mOpenTipsTv;

    private Switch mSwitch1;
    private Switch mSwitch2;
    private Switch mSwitch4;
    private Switch mSwitch5;
    private Switch mSwitch6;
    private Switch mSwitch7;
    private Switch mSwitchOpenFloat;

    private LinearLayout mLayoutDanceLine;

    private boolean mAccessibilityStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        initTrackerWindowManager();

        mServiceStatusTv = findViewById(R.id.tv_service_status);
        mOpenTipsTv = findViewById(R.id.tv_open_tips);
        mSwitch1 = findViewById(R.id.switch1);
        mSwitch2 = findViewById(R.id.switch2);
        mSwitch4 = findViewById(R.id.switch4);
        mSwitch5 = findViewById(R.id.switch5);
        mSwitch6 = findViewById(R.id.switch6);
        mSwitch7 = findViewById(R.id.switch7);
        mSwitchOpenFloat = findViewById(R.id.btn_open_float);
        mLayoutDanceLine = findViewById(R.id.layout_dance_line);

        initSwitchStatus();

        initClickListener();
    }

    private void initSwitchStatus() {
        mSwitch2.setChecked(Global.AUTO_JUMP = PreferManager.getAutoJumpConfig());
        mSwitch4.setChecked(Global.APP_TASK_HIDE = PreferManager.getAppTaskHideConfig());
        mSwitch5.setChecked(Global.AUTO_TIK_TOK_JUMP_AD = PreferManager.getAutoTikTokAdConfig());
        mSwitch6.setChecked(Global.AUTO_WE_CHAT_LOGIN = PreferManager.getAutoWeChatLoginConfig());
        mSwitch7.setChecked(Global.AUTO_TAO_BAO = PreferManager.getAutoTaoBaoConfig());
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

        mSwitch6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PreferManager.getAutoWeChatLoginConfig()) {
                    PreferManager.setAutoWeChatLoginConfig(false);
                    Global.AUTO_WE_CHAT_LOGIN = false;
                } else {
                    PreferManager.setAutoWeChatLoginConfig(true);
                    Global.AUTO_WE_CHAT_LOGIN = true;
                }
            }
        });

        mSwitch7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PreferManager.getAutoTaoBaoConfig()) {
                    PreferManager.setAutoTaoBaoConfig(false);
                    Global.AUTO_TAO_BAO = false;
                } else {
                    PreferManager.setAutoTaoBaoConfig(true);
                    Global.AUTO_TAO_BAO = true;
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

        mLayoutDanceLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DanceLineActivity.class));
            }
        });

        mOpenTipsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "嘻嘻，不指定哪天才能更新呢~", Toast.LENGTH_LONG).show();
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