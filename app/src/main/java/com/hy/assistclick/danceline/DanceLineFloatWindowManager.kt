package com.hy.assistclick.danceline

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.blankj.utilcode.util.ScreenUtils

/**
 * @author hy
 * @date 2023/1/11
 * desc:
 **/
class DanceLineFloatWindowManager(private val context: Context, private val closeListener: DanceLineFloatCloseListener) {

    private val windowManager: WindowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var logFloatingView: View? = null

    init {
        // mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    fun addView() {
        try {
            if (logFloatingView == null) {
                logFloatingView = DanceLineFloatingView(context, closeListener)
                logFloatingView?.layoutParams = LAYOUT_PARAMS
                windowManager.addView(logFloatingView, LAYOUT_PARAMS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeView() {
        try {
            if (logFloatingView != null) {
                windowManager.removeView(logFloatingView)
                logFloatingView = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private var LAYOUT_PARAMS: WindowManager.LayoutParams? = null

        init {
            val params = WindowManager.LayoutParams()
            params.x = 0
            params.y = 0
            // params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.width = ScreenUtils.getScreenWidth() / 5 * 4
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.gravity = Gravity.START or Gravity.TOP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            params.format = PixelFormat.RGBA_8888
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            // params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            LAYOUT_PARAMS = params
        }
    }
}