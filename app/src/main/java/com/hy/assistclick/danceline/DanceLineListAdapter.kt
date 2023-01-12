package com.hy.assistclick.danceline

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hy.assistclick.base.BaseViewHolder
import com.hy.assistclick.base.IViewHolder
import com.hy.assistclick.danceline.DanceLineListAdapter.DanceLineViewHolder
import com.hy.assistclick.R
import com.hy.assistclick.common.Global
import com.hy.assistclick.utils.ObjectUtils
import com.hy.assistclick.utils.Utils

/**
 * @author hy
 * @date 2022/10/27
 * desc: LogListAdapter
 **/
class DanceLineListAdapter internal constructor(list: MutableList<DanceLine>?) :
    RecyclerView.Adapter<DanceLineViewHolder>() {

    private var data: MutableList<DanceLine> = list ?: arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DanceLineViewHolder {
        return DanceLineViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_dance_line, parent, false))
    }

    override fun onBindViewHolder(holder: DanceLineViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class DanceLineViewHolder(view: View) : BaseViewHolder(view), IViewHolder<DanceLine?> {

        private val activity: Activity = Utils.getActivityFromView(view)

        private var tvName: TextView? = null
        private var btnRecord: TextView? = null
        private var btnRun: TextView? = null
        private var btnClear: TextView? = null

        var danceLine: DanceLine? = null

        init {
            tvName = view.findViewById(R.id.tv_name)
            btnRecord = view.findViewById(R.id.btn_record)
            btnRun = view.findViewById(R.id.btn_run)
            btnClear = view.findViewById(R.id.btn_clear)

            btnRecord!!.setOnClickListener {
                Global.DANCE_LINE_RECORD = true
                (activity as DanceLineActivity).addDanceLineFloatView(danceLine)
            }

            btnRun!!.setOnClickListener {
                (activity as DanceLineActivity).addDanceLineFloatView(danceLine)
            }

            btnClear!!.setOnClickListener {
                val builder = androidx.appcompat.app.AlertDialog.Builder(activity)
                builder.setMessage("确定要清除本关卡数据吗？")
                builder.setPositiveButton("确定") { _, _ ->
                    (activity as DanceLineActivity).clearDanceLine(danceLine)
                }
                builder.setNegativeButton("取消") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                builder.show()
            }
        }

        override fun bind(item: DanceLine?) {
            if (item == null) {
                return
            }
            danceLine = item
            val hasData = !ObjectUtils.isEmpty(item.clickDataList)
            val str = if (hasData) "  [存在数据" + " size=" + item.clickDataList!!.size + "]" else "  [无数据~]"
            tvName?.text = item.name + str

            if (hasData) {
                tvName?.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent))
            } else {
                tvName?.setTextColor(ContextCompat.getColor(activity, R.color.gray8))
            }
        }
    }
}