package com.hy.assistclick.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;


/**
 * @author hy
 * @date 2020/5/1
 * ClassDesc:SharedPreferences.
 **/
public class PreferManager {

    /**
     * 屏幕宽度
     */
    public static final String SCREEN_WIDTH = "screen_width";
    /**
     * 屏幕高度
     */
    public static final String SCREEN_HEIGHT = "screen_height";
    /**
     * 状态栏宽度
     */
    public static final String STATUS_BAR_HEIGHT = "status_bar_height";

    @SuppressLint("StaticFieldLeak")
    private static SharedPreferences sp;
    private static SharedPreferences.Editor edit;

    @SuppressLint("CommitPrefEdits")
    public static void init(Context context) {
        if (sp == null) {
            sp = context.getSharedPreferences("AssistClick", Context.MODE_PRIVATE);
            edit = sp.edit();
        }
    }

    public static void setString(String key, String value) {
        edit.putString(key, value);
        edit.apply();
    }

    public static String getString(String key) {
        return sp.getString(key, "");
    }

    public static String getString(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public static void setLong(String key, long value) {
        edit.putLong(key, value);
        edit.apply();
    }

    public static long getLong(String key, long defaultValue) {
        return sp.getLong(key, defaultValue);
    }

    public static void setBoolean(String key, boolean value) {
        edit.putBoolean(key, value);
        edit.apply();
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    public static void setInt(String key, int value) {
        edit.putInt(key, value);
        edit.apply();
    }

    public static int getInt(String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    public static void setFloat(String key, float value) {
        edit.putFloat(key, value);
        edit.apply();
    }

    public static float getFloat(String key, float defaultValue) {
        return sp.getFloat(key, defaultValue);
    }

    /**
     * ================================================
     * 自动跳过开关
     * ================================================
     */
    public static final String AUTO_JUMP_CONFIG = "AUTO_JUMP_CONFIG";

    public static boolean getAutoJumpConfig() {
        return sp.getBoolean(AUTO_JUMP_CONFIG, false);
    }

    public static void setAutoJumpConfig(boolean isOpen) {
        edit.putBoolean(AUTO_JUMP_CONFIG, isOpen).apply();
    }

    /**
     * ================================================
     * 多任务隐藏开关
     * ================================================
     */
    public static final String APP_TASK_HIDE_CONFIG = "APP_TASK_HIDE_CONFIG";

    public static boolean getAppTaskHideConfig() {
        return sp.getBoolean(APP_TASK_HIDE_CONFIG, false);
    }

    public static void setAppTaskHideConfig(boolean isOpen) {
        edit.putBoolean(APP_TASK_HIDE_CONFIG, isOpen).apply();
    }

    /**
     * ================================================
     * 查看当前activity开关
     * ================================================
     */
    public static final String SEE_ACTIVITY_CONFIG = "SEE_ACTIVITY_CONFIG";

    public static boolean getSeeActivityConfig() {
        return sp.getBoolean(SEE_ACTIVITY_CONFIG, false);
    }

    public static void setSeeActivityConfig(boolean isOpen) {
        edit.putBoolean(SEE_ACTIVITY_CONFIG, isOpen).apply();
    }

    /**
     * ================================================
     * 抖音自动划走广告开关
     * ================================================
     */
    public static final String AUTO_TIK_TOK_JUMP_AD_CONFIG = "AUTO_TIK_TOK_JUMP_AD_CONFIG";

    public static boolean getAutoTikTokAdConfig() {
        return sp.getBoolean(AUTO_TIK_TOK_JUMP_AD_CONFIG, false);
    }

    public static void setAutoTikTokAdConfig(boolean isOpen) {
        edit.putBoolean(AUTO_TIK_TOK_JUMP_AD_CONFIG, isOpen).apply();
    }

    /**
     * ================================================
     * 自动登录电脑端微信开关
     * ================================================
     */
    public static final String AUTO_WE_CHAT_LOGIN_CONFIG = "AUTO_WE_CHAT_LOGIN_CONFIG";

    public static boolean getAutoWeChatLoginConfig() {
        return sp.getBoolean(AUTO_WE_CHAT_LOGIN_CONFIG, false);
    }

    public static void setAutoWeChatLoginConfig(boolean isOpen) {
        edit.putBoolean(AUTO_WE_CHAT_LOGIN_CONFIG, isOpen).apply();
    }

    /**
     * ================================================
     * 淘宝开关
     * ================================================
     */
    public static final String AUTO_TAO_BAO_CONFIG = "AUTO_TAO_BAO_CONFIG";

    public static boolean getAutoTaoBaoConfig() {
        return sp.getBoolean(AUTO_TAO_BAO_CONFIG, false);
    }

    public static void setAutoTaoBaoConfig(boolean isOpen) {
        edit.putBoolean(AUTO_TAO_BAO_CONFIG, isOpen).apply();
    }

}
