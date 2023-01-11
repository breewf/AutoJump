package com.hy.assistclick.danceline

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.blankj.utilcode.util.ObjectUtils
import com.hy.assistclick.R
import com.hy.assistclick.BuildConfig
import com.hy.assistclick.common.Actions
import com.hy.assistclick.common.Arguments
import com.hy.assistclick.common.Global
import com.hy.assistclick.common.PreferManager
import com.hy.assistclick.event.Event
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * @author hy
 * @date 2023/1/10
 * desc: DanceLineActivity
 */
open class DanceLineActivity : AppCompatActivity() {

    private var tvClick: TextView? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: DanceLineListAdapter? = null

    private var danceLineFloatWindowManager: DanceLineFloatWindowManager? = null

    private var dataMap: MutableMap<Int, String> = HashMap()
    private var danceLine: DanceLine? = null
    private var clickCount: Int = 0
    private var clickCurrentTimeMillis: Long = 0
    private var clickCurrentTimeMillisTemp: Long = 0
    private var runClickCount: Int = 0
    private var clickDataListRecord: MutableList<Long>? = null
    private var clickDataListRun: MutableList<Long>? = null

    companion object {
        const val REQUEST_CODE_DANCE_LINE = 1001
        const val DANCE_CLICK = 2233
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dance_line)

        val mActionBar = supportActionBar
        mActionBar?.title = getString(R.string.dance_line)

        tvClick = findViewById(R.id.tv_click)
        recyclerView = findViewById(R.id.rv_dance_line)

        initData()

        val dataList = buildDanceLineData()
        adapter = DanceLineListAdapter(dataList)
        recyclerView?.adapter = adapter
        tvClick?.setOnClickListener(View.OnClickListener {
            val x = Random().nextInt(200)
            val y = Random().nextInt(600)
            if (BuildConfig.DEBUG) {
                Log.i("AutoClick", "x:$x y:$y")
            }
            tvClick?.text = "点击屏幕 x:$x y:$y"
        })
    }

    private fun initData() {
        dataMap[1001] = "春天"
        dataMap[1002] = "钢琴"
        dataMap[1003] = "海滩"
        dataMap[1004] = "时光"
    }

    private fun buildDanceLineData(): MutableList<DanceLine> {
        var dataList = PreferManager.getDanceLineList()
        if (dataList == null) {
            dataList = ArrayList()
        }

        val iterator: Iterator<Int> = dataMap.keys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            var hasLevelData = false
            run forEachData@{
                dataList.forEach {
                    if (key == it.id) {
                        hasLevelData = true
                        return@forEachData
                    }
                }
            }
            if (!hasLevelData) {
                // 不存在，则添加
                val line = DanceLine()
                line.id = key
                line.name = dataMap[key]
                dataList.add(line)
            }
        }

        // 保存数据
        PreferManager.setDanceLineList(dataList)
        return dataList
    }

    open fun getDanceLineData(): DanceLine? {
        return danceLine
    }

    /**
     * 重置
     */
    open fun recordResetData() {
        clickCount = 0
        clickCurrentTimeMillis = 0
        clickCurrentTimeMillisTemp = 0
    }

    /**
     * 点击记录数据，点击时触发
     */
    open fun recordDanceLineData() {
        if (danceLine == null) {
            return
        }

        clickDataListRecord = danceLine!!.clickDataList
        if (ObjectUtils.isEmpty(clickDataListRecord)) {
            clickDataListRecord = ArrayList()
        }
        if (clickDataListRecord!!.size == 0) {
            clickDataListRecord!!.add(0, 0)
        }
        // 第一个必为0
        clickDataListRecord!![0] = 0

        clickCurrentTimeMillis = System.currentTimeMillis()
        if (clickCount == 0) {
            clickCurrentTimeMillisTemp = clickCurrentTimeMillis
        }
        val spaceTime = clickCurrentTimeMillis - clickCurrentTimeMillisTemp

        if (clickCount > 0) {
            if (clickDataListRecord!!.size > clickCount) {
                val localSpaceTimeData = clickDataListRecord!![clickCount]
                if (BuildConfig.DEBUG) {
                    Log.i("AutoClick", "已存在点击数据: dataListSize:" + clickDataListRecord!!.size
                            + " spaceTime:" + spaceTime + " localSpaceTimeData:" + localSpaceTimeData)
                }
                val bundle = Bundle()
                bundle.putLong(Arguments.ARG_LONG, spaceTime)
                bundle.putInt(Arguments.ARG_INT, clickDataListRecord!!.size)
                bundle.putString(Arguments.ARG_STRING, "此处已存在数据~")
                EventBus.getDefault().post(Event(Actions.ACTIONS_DANCE_LINE_RECORD_SHOW, bundle))
            } else {
                clickDataListRecord!!.add(spaceTime)
                if (BuildConfig.DEBUG) {
                    Log.i("AutoClick", "录制-存储点击数据--add:$clickCount dataListSize:" + clickDataListRecord!!.size
                            + " spaceTime:" + spaceTime)
                }
                val bundle = Bundle()
                bundle.putLong(Arguments.ARG_LONG, spaceTime)
                bundle.putInt(Arguments.ARG_INT, clickDataListRecord!!.size)
                bundle.putString(Arguments.ARG_STRING, "此处无数据，保存数据~")
                EventBus.getDefault().post(Event(Actions.ACTIONS_DANCE_LINE_RECORD_SHOW, bundle))
            }

            // 赋值，重要
            danceLine!!.clickDataList = clickDataListRecord
        }

        clickCount++
        clickCurrentTimeMillisTemp = clickCurrentTimeMillis
    }

    /**
     * 保存记录的数据
     */
    open fun saveRecordData() {
        if (danceLine == null || ObjectUtils.isEmpty(clickDataListRecord)) {
            return
        }
        danceLine!!.clickDataList = clickDataListRecord

        val dataList: MutableList<DanceLine> = PreferManager.getDanceLineList() ?: return
        for (i in 0..dataList.size) {
            if (dataList[i].id == danceLine!!.id) {
                dataList[i] = danceLine as DanceLine
                break
            }
        }
        PreferManager.setDanceLineList(dataList)
        Toast.makeText(this, "数据已保存~", Toast.LENGTH_LONG).show()
    }

    /**
     * 运行
     */
    open fun run() {
        if (danceLine == null) {
            return
        }
        clickDataListRun = danceLine!!.clickDataList
        if (ObjectUtils.isEmpty(clickDataListRun)) {
            Toast.makeText(this, "没有数据哦~", Toast.LENGTH_LONG).show()
            return
        }
        if (clickDataListRun!!.size <= 1) {
            Toast.makeText(this, "数据为空~", Toast.LENGTH_LONG).show()
            return
        }
        // 执行一次点击，开始运行
        val bundle = Bundle()
        bundle.putLong(Arguments.ARG_LONG, clickDataListRun!![1])
        bundle.putInt(Arguments.ARG_INT, clickDataListRun!!.size)
        bundle.putInt(Arguments.ARG_INT2, 1)
        EventBus.getDefault().post(Event(Actions.ACTIONS_DANCE_LINE_CLICK, bundle))
        runClickCount = 1

        // 从第二个数据开始，第一个数据为0
        delayClick(clickDataListRun!![1])
    }

    open fun delayClick(delayTime: Long) {
        handler.sendEmptyMessageDelayed(DANCE_CLICK, delayTime)
    }

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                DANCE_CLICK -> {
                    if (ObjectUtils.isNotEmpty(clickDataListRun)) {
                        runClickCount++
                        // 执行点击
                        val bundle = Bundle()
                        if (clickDataListRun!!.size > runClickCount) {
                            bundle.putLong(Arguments.ARG_LONG, clickDataListRun!![runClickCount])
                        }
                        bundle.putInt(Arguments.ARG_INT, clickDataListRun!!.size)
                        bundle.putInt(Arguments.ARG_INT2, runClickCount)
                        EventBus.getDefault().post(Event(Actions.ACTIONS_DANCE_LINE_CLICK, bundle))

                        if (clickDataListRun!!.size > runClickCount) {
                            delayClick(clickDataListRun!![runClickCount])
                        }
                    }
                }
            }
        }
    }

    /**
     * 打开悬浮框
     */
    open fun addDanceLineFloatView(danceLine: DanceLine?) {
        if (!checkCanDrawOverlays(true)) {
            return
        }
        this.danceLine = danceLine
        if (danceLineFloatWindowManager == null) {
            danceLineFloatWindowManager = DanceLineFloatWindowManager(this, object : DanceLineFloatCloseListener {
                override fun onClose() {
                    removeDanceLineFloatView()
                }
            })
        }
        danceLineFloatWindowManager!!.addView()
        Global.DANCE_LINE = true
    }

    /**
     * 关闭悬浮框
     */
    open fun removeDanceLineFloatView() {
        danceLineFloatWindowManager?.removeView()
        Global.DANCE_LINE = false
        Global.DANCE_LINE_RECORD = false

        handler.removeMessages(DANCE_CLICK)
    }

    /**
     * 悬浮窗权限
     */
    private fun checkCanDrawOverlays(goSetting: Boolean): Boolean {
        if (Build.VERSION.SDK_INT < 23) {
            return false
        }
        if (!Settings.canDrawOverlays(this)) {
            if (goSetting) {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
                    REQUEST_CODE_DANCE_LINE
                )
                Toast.makeText(this, "请先授予悬浮窗权限", Toast.LENGTH_LONG).show()
            }
            return false
        }
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_DANCE_LINE) {
            if (checkCanDrawOverlays(false)) {
                addDanceLineFloatView(danceLine)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}