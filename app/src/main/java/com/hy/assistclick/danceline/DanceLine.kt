package com.hy.assistclick.danceline

import com.hy.assistclick.base.BaseMode

/**
 * @author hy
 * @date 2023/1/10
 * desc:
 **/
class DanceLine : BaseMode() {

    var id: Int = -1
    var name: String? = null

    /**
     * 点击数据源
     * 存储时间，单位毫秒
     */
    var clickDataList: MutableList<Long>? = null
}