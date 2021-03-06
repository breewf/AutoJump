package com.hy.assistclick;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

/**
 * @author hy
 * @date 2020/4/29
 * ClassDesc:辅助功能工具类.
 **/
public class AccessibilityUtils {

    public static boolean checkAccessibility(Context context, boolean goSetting) {
        if (context == null) {
            return false;
        }
        // 判断辅助功能是否开启
        if (!AccessibilityUtils.isAccessibilitySettingsOn(context)) {
            // 引导至辅助功能设置页面
            if (goSetting) {
                context.startActivity(
                        new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                );
                Toast.makeText(context, "请找到-->服务(无障碍)-->"
                        + context.getString(R.string.app_name)
                        + "-->打开开关", Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
    }

    public static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(context.getPackageName().toLowerCase());
            }
        }

        return false;
    }
}
