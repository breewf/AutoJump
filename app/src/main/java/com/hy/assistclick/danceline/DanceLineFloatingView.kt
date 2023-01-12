package com.hy.assistclick.danceline

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.hy.assistclick.AssistService
import com.hy.assistclick.R
import com.hy.assistclick.common.Actions
import com.hy.assistclick.common.Arguments
import com.hy.assistclick.common.Global
import com.hy.assistclick.event.Event
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * @author hy
 * @date 2023/1/11
 * desc:
 **/
@SuppressLint("ViewConstructor")
class DanceLineFloatingView(private val mContext: Context, private val closeListener: DanceLineFloatCloseListener?) : FrameLayout(mContext) {

    private val mWindowManager: WindowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var mPreP: Point? = null
    private var mCurP: Point? = null

    private var tvTitle: TextView? = null
    private var tvInfo: TextView? = null
    private var tvStart: TextView? = null
    private var contentLayout: View? = null

    companion object {
        const val TAG = "DanceLineFloatingView"
    }

    init {
        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        inflate(mContext, R.layout.layout_float_dance_line, this)

        val ivClose = findViewById<ImageView>(R.id.iv_close)
        ivClose.setOnClickListener { closeListener?.onClose() }

        tvTitle = findViewById(R.id.tv_dance_title)
        tvInfo = findViewById(R.id.tv_dance_info)
        tvStart = findViewById(R.id.tv_start)
        contentLayout = findViewById(R.id.content_layout)

        val danceLine = (mContext as DanceLineActivity).getDanceLineData()

        if (Global.DANCE_LINE_RECORD) {
            tvTitle?.text = "录制模式，点击此区域开始录制数据"
            tvStart?.text = "保存录制数据"
            tvInfo?.visibility = View.VISIBLE
            tvInfo?.text = "录制点击时间：0"
            mContext.recordResetData()
        } else {
            tvTitle?.text = "运行关卡：" + danceLine?.name
            tvInfo?.visibility = View.VISIBLE
            tvInfo?.text = "下次运行时间：0"
            tvStart?.text = "开始运行"
        }

        contentLayout?.setOnClickListener {
            if (!Global.DANCE_LINE_RECORD) {
                return@setOnClickListener
            }
            if (!mContext.isRecordToRun()) {
                // 模拟点击一下屏幕
                if (AssistService.getInstance() != null) {
                    AssistService.getInstance().dispatchClick(400, 700, object : AccessibilityService.GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            super.onCompleted(gestureDescription)
                            // 录制数据
                            mContext.recordDanceLineData(System.currentTimeMillis())
                        }
                    })
                }
            }
        }

        tvStart?.setOnClickListener {
            if (Global.DANCE_LINE_RECORD) {
                // 保存
                mContext.saveRecordData()
            } else {
                // 运行
                mContext.run()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe
    fun onEvent(event: Event?) {
        if (event == null) {
            return
        }
        if (Actions.ACTIONS_DANCE_LINE_CLICK == event.action) {
            val nextTime = event.bundle.get(Arguments.ARG_LONG)
            val size = event.bundle.get(Arguments.ARG_INT)
            val count = event.bundle.get(Arguments.ARG_INT2)
            tvInfo?.text = "下次运行时间：$nextTime ms\n点击数据总量：$size  已运行数量：$count"
            if (AssistService.getInstance() != null) {
                AssistService.getInstance().dispatchClick(400, 700)
            }
        }

        if (Actions.ACTIONS_DANCE_LINE_RECORD_SHOW == event.action) {
            val showString = event.bundle.get(Arguments.ARG_STRING) as String
            val spaceTime = event.bundle.get(Arguments.ARG_LONG)
            tvInfo?.text = "$showString\n此次点击距离上次的时间：$spaceTime"
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> mPreP = Point(event.rawX.toInt(), event.rawY.toInt())
            MotionEvent.ACTION_MOVE -> {
                mCurP = Point(event.rawX.toInt(), event.rawY.toInt())
                val dx = mCurP!!.x - mPreP!!.x
                val dy = mCurP!!.y - mPreP!!.y
                val layoutParams = this.layoutParams as WindowManager.LayoutParams
                layoutParams.x += dx
                layoutParams.y += dy
                mWindowManager.updateViewLayout(this, layoutParams)
                mPreP = mCurP
            }
            else -> {}
        }
        return false
    }
}