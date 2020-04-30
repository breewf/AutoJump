package com.hy.autojump;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * @author hy
 * @date 2020/4/29
 * ClassDesc:FloatWindowManager.
 **/
public class FloatWindowManager {

    private final Context mContext;
    private final WindowManager mWindowManager;
    private static final WindowManager.LayoutParams LAYOUT_PARAMS;

    private View mFloatingView;

    public FloatWindowManager(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    static {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.x = 0;
        params.y = 0;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.LEFT | Gravity.TOP;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = ((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
        } else {
            params.type = ((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        }

        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        LAYOUT_PARAMS = params;
    }

    public void addView() {
        try {
            if (mFloatingView == null) {
                mFloatingView = new FloatingView(mContext);
                mFloatingView.setLayoutParams(LAYOUT_PARAMS);

                mWindowManager.addView(mFloatingView, LAYOUT_PARAMS);
                Toast.makeText(mContext, "悬浮框已开启", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "addFloatView Exception", Toast.LENGTH_SHORT).show();
        }
    }

    public void removeView() {
        try {
            if (mFloatingView != null) {
                mWindowManager.removeView(mFloatingView);
                mFloatingView = null;
                Toast.makeText(mContext, "悬浮框已关闭", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "removeFloatView Exception", Toast.LENGTH_SHORT).show();
        }
    }
}
