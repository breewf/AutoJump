package com.hy.assistclick.danceline

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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

    private var dataMap: MutableMap<Int, String> = LinkedHashMap()
    private var danceLine: DanceLine? = null
    private var recordClickCount: Int = 0
    private var clickCurrentTimeMillis: Long = 0
    private var clickCurrentTimeMillisTemp: Long = 0
    private var runClickCount: Int = 0
    private var clickDataRecord: MutableList<Long>? = null
    private var clickDataRecordTemp: MutableList<Long> = ArrayList()
    private var clickDataRun: MutableList<Long>? = null

    /**
     * 是否是录制转运行
     */
    private var recordToRun: Boolean = false

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
        dataMap[1002] = "时光"
        dataMap[1003] = "钢琴"
        dataMap[1004] = "沙滩"
        dataMap[1005] = "丑小鸭"
        dataMap[1006] = "寒冬"
        dataMap[1007] = "海洋"
        dataMap[1008] = "嘻哈演变"
        dataMap[1009] = "风暴"
        dataMap[1010] = "稀树草原"
        dataMap[1011] = "西部世界"
        dataMap[1012] = "情人节"
        dataMap[1013] = "天空之梦"
        dataMap[1014] = "万圣节"
        dataMap[1015] = "万圣节魔方"
        dataMap[1016] = "出埃及记"
        dataMap[1017] = "The Alone"
        dataMap[1018] = "田野"
        dataMap[1019] = "沙漠"
        dataMap[1020] = "青石巷"
        dataMap[1021] = "末日"
        dataMap[1022] = "山脉"
        dataMap[1023] = "复活节"
        dataMap[1024] = "迷宫"
        dataMap[1025] = "地球"
        dataMap[1026] = "地球 color"
        dataMap[1027] = "风暴"
        dataMap[1028] = "圣诞前夕"
        dataMap[1029] = "圣诞节派对"
        dataMap[1030] = "秋天"
        dataMap[1031] = "The Faded"
        dataMap[1032] = "田野 reggne"
        dataMap[1033] = "金牛座"
        dataMap[1034] = "游乐园"
        dataMap[1035] = "心动"
        dataMap[1036] = "寒冬 house"
        dataMap[1037] = "篮球"
        dataMap[1038] = "足球"
        dataMap[1039] = "战殇"
        dataMap[1040] = "大教堂 rock"
        dataMap[1041] = "大教堂"
        dataMap[1042] = "水晶"
        dataMap[1043] = "印度情缘"
        dataMap[1044] = "All About Us"
        dataMap[1045] = "水手传说"
        dataMap[1046] = "混沌"
        dataMap[1047] = "刺客传奇"
        dataMap[1048] = "春节"
        dataMap[1049] = "中国园林"
        dataMap[1050] = "赛车"
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
        recordClickCount = 0
        clickCurrentTimeMillis = 0
        clickCurrentTimeMillisTemp = 0

        clickDataRecordTemp.clear()
    }

    open fun isRecordToRun(): Boolean {
        return recordToRun
    }

    /**
     * 点击记录数据，点击时触发
     */
    open fun recordDanceLineData(currentTimeMillis: Long) {
        if (danceLine == null) {
            return
        }

        clickDataRecord = danceLine!!.clickDataList
        if (ObjectUtils.isEmpty(clickDataRecord)) {
            clickDataRecord = ArrayList()
        }
        if (clickDataRecord!!.size == 0) {
            clickDataRecord!!.add(0, 0)
        }
        // 第一个必为0
        clickDataRecord!![0] = 0

        // 首次点击生效
        if (!recordToRun && recordClickCount == 0) {
            clickDataRecordTemp.addAll(clickDataRecord!!)
            if (clickDataRecordTemp.size > 1) {
                // 转为自动运行
                recordToRun = true
                run()
                Toast.makeText(this, "录制开始自动运行~", Toast.LENGTH_LONG).show()
                return
            }
        }

        clickCurrentTimeMillis = currentTimeMillis
        if (recordClickCount == 0) {
            clickCurrentTimeMillisTemp = clickCurrentTimeMillis
        }
        val spaceTime = clickCurrentTimeMillis - clickCurrentTimeMillisTemp

        if (recordClickCount > 0) {
            if (clickDataRecord!!.size > recordClickCount) {
                val localSpaceTimeData = clickDataRecord!![recordClickCount]
                if (BuildConfig.DEBUG) {
                    Log.i("AutoClick", "已存在点击数据: dataListSize:" + clickDataRecord!!.size
                            + " spaceTime:" + spaceTime + " localSpaceTimeData:" + localSpaceTimeData)
                }
                val bundle = Bundle()
                bundle.putLong(Arguments.ARG_LONG, spaceTime)
                bundle.putString(Arguments.ARG_STRING, "此处已存在数据~" + "数据量：" + clickDataRecord!!.size)
                EventBus.getDefault().post(Event(Actions.ACTIONS_DANCE_LINE_RECORD_SHOW, bundle))
            } else {
                clickDataRecord!!.add(spaceTime)
                if (BuildConfig.DEBUG) {
                    Log.i("AutoClick", "录制-存储点击数据--add:$recordClickCount dataListSize:" + clickDataRecord!!.size
                            + " spaceTime:" + spaceTime)
                }
                val bundle = Bundle()
                bundle.putLong(Arguments.ARG_LONG, spaceTime)
                bundle.putString(Arguments.ARG_STRING, "此处无数据，保存数据~" + "数据量：" + clickDataRecord!!.size)
                EventBus.getDefault().post(Event(Actions.ACTIONS_DANCE_LINE_RECORD_SHOW, bundle))
            }

            // 赋值，重要
            danceLine!!.clickDataList = clickDataRecord
        }

        recordClickCount++
        clickCurrentTimeMillisTemp = clickCurrentTimeMillis
    }

    /**
     * 保存记录的数据
     */
    open fun saveRecordData() {
        if (danceLine == null || ObjectUtils.isEmpty(clickDataRecord)) {
            return
        }
        danceLine!!.clickDataList = clickDataRecord

        val dataList: MutableList<DanceLine> = PreferManager.getDanceLineList() ?: return
        for (i in 0..dataList.size) {
            if (dataList[i].id == danceLine!!.id) {
                dataList[i] = danceLine as DanceLine
                adapter?.notifyItemChanged(i)
                break
            }
        }
        PreferManager.setDanceLineList(dataList)
        Toast.makeText(this, "关卡数据已保存~", Toast.LENGTH_LONG).show()
    }

    /**
     * 运行
     */
    open fun run() {
        if (danceLine == null) {
            return
        }
        clickDataRun = danceLine!!.clickDataList
        if (ObjectUtils.isEmpty(clickDataRun)) {
            Toast.makeText(this, "没有数据哦~", Toast.LENGTH_LONG).show()
            return
        }
        if (clickDataRun!!.size <= 1) {
            Toast.makeText(this, "数据为空~", Toast.LENGTH_LONG).show()
            return
        }
        // 执行一次点击，开始运行
        val bundle = Bundle()
        bundle.putLong(Arguments.ARG_LONG, clickDataRun!![1])
        bundle.putInt(Arguments.ARG_INT, clickDataRun!!.size)
        bundle.putInt(Arguments.ARG_INT2, 1)
        EventBus.getDefault().post(Event(Actions.ACTIONS_DANCE_LINE_CLICK, bundle))
        runClickCount = 1
        setRecordToRun()

        // 从第二个数据开始，第一个数据为0
        delayClick(clickDataRun!![1])
    }

    open fun delayClick(delayTime: Long) {
        // 10ms 是执行点击的耗时，减掉相当于和录制时的时间对齐
        handler.sendEmptyMessageDelayed(DANCE_CLICK, delayTime - 10)
    }

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                DANCE_CLICK -> {
                    if (ObjectUtils.isNotEmpty(clickDataRun)) {
                        runClickCount++
                        setRecordToRun()
                        // 执行点击
                        val bundle = Bundle()
                        if (clickDataRun!!.size > runClickCount) {
                            bundle.putLong(Arguments.ARG_LONG, clickDataRun!![runClickCount])
                        }
                        bundle.putInt(Arguments.ARG_INT, clickDataRun!!.size)
                        bundle.putInt(Arguments.ARG_INT2, runClickCount)
                        EventBus.getDefault().post(Event(Actions.ACTIONS_DANCE_LINE_CLICK, bundle))

                        if (clickDataRun!!.size > runClickCount) {
                            delayClick(clickDataRun!![runClickCount])
                        }
                    }
                }
            }
        }
    }

    private fun setRecordToRun() {
        if (!recordToRun) {
            return
        }
        recordClickCount = runClickCount
        if (ObjectUtils.isNotEmpty(clickDataRecordTemp)) {
            clickDataRecordTemp.removeFirst()
        }
        if (clickDataRecordTemp.size == 0) {
            recordToRun = false

            // 重要，赋值最后一次自动运行点击的时间
            clickCurrentTimeMillisTemp = System.currentTimeMillis() + 10

            // 震动提醒
            vibrator()
        }
    }

    /**
     * clear
     */
    open fun clearDanceLine(danceLine: DanceLine?) {
        if (danceLine == null) {
            return
        }
        val list = danceLine.clickDataList
        if (ObjectUtils.isNotEmpty(list)) {
            list!!.clear()
        }
        val dataList: MutableList<DanceLine> = PreferManager.getDanceLineList() ?: return
        for (i in 0..dataList.size) {
            if (dataList[i].id == danceLine.id) {
                dataList[i] = danceLine
                adapter?.notifyItemChanged(i)
                break
            }
        }
        PreferManager.setDanceLineList(dataList)
        Toast.makeText(this, "已清除关卡数据~", Toast.LENGTH_LONG).show()
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

        // 回到桌面
        val intent = Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
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

    private fun vibrator() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
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