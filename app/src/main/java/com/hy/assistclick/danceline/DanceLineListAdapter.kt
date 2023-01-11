package com.hy.assistclick.danceline

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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

        var danceLine: DanceLine? = null

        init {
            tvName = view.findViewById(R.id.tv_name)
            btnRecord = view.findViewById(R.id.btn_record)
            btnRun = view.findViewById(R.id.btn_run)

            btnRecord!!.setOnClickListener {
                Global.DANCE_LINE_RECORD = true
                (activity as DanceLineActivity).addDanceLineFloatView(danceLine)
            }

            btnRun!!.setOnClickListener {
                (activity as DanceLineActivity).addDanceLineFloatView(danceLine)
            }
        }

        override fun bind(item: DanceLine?) {
            if (item == null) {
                return
            }
            danceLine = item
            val hasData = !ObjectUtils.isEmpty(item.clickDataList)
            val str = if (hasData) "  [存在录制数据~]" else "  [无数据~]"
            tvName?.text = item.name + str
        }
    }
}