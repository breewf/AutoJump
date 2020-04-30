package com.hy.autojump;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hy.autojump.event.Actions;
import com.hy.autojump.event.ActivityChangedEvent;
import com.hy.autojump.event.Event;

import de.greenrobot.event.EventBus;

/**
 * @author hy
 * @date 2020/4/29
 * ClassDesc:悬浮提示View.
 **/
public class FloatingView extends LinearLayout {

    public static final String TAG = "FloatingView";

    private final Context mContext;
    private final WindowManager mWindowManager;
    private TextView mTvPackageName;
    private TextView mTvClassName;

    public FloatingView(Context context) {
        super(context);
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initView();
    }

    private void initView() {
        inflate(mContext, R.layout.layout_floating, this);
        mTvPackageName = findViewById(R.id.tv_package_name);
        mTvClassName = findViewById(R.id.tv_class_name);

        ImageView ivClose = findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContext == null) {
                    return;
                }
                EventBus.getDefault().post(new Event(Actions.ACTIONS_CLOSE_FLOAT));
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(ActivityChangedEvent event) {
        Log.d(TAG, "event:" + event.getPackageName() + ": " + event.getClassName());
        String packageName = event.getPackageName(),
                className = event.getClassName();

        mTvPackageName.setText(packageName);
        mTvClassName.setText(
                className.startsWith(packageName) ?
                        className.substring(packageName.length()) :
                        className
        );
    }

    Point preP, curP;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preP = new Point((int) event.getRawX(), (int) event.getRawY());
                break;

            case MotionEvent.ACTION_MOVE:
                curP = new Point((int) event.getRawX(), (int) event.getRawY());
                int dx = curP.x - preP.x,
                        dy = curP.y - preP.y;

                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.getLayoutParams();
                layoutParams.x += dx;
                layoutParams.y += dy;
                mWindowManager.updateViewLayout(this, layoutParams);

                preP = curP;
                break;
            default:
                break;
        }

        return false;
    }
}
