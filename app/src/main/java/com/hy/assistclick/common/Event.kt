package com.hy.assistclick.common

import android.os.Bundle

/**
 * @author hy
 * @date 2023/1/11
 * desc:
 **/
class Event {

    var action: String?
    var bundle = Bundle()

    constructor(action: String?) {
        this.action = action
    }

    constructor(action: String?, bundle: Bundle) {
        this.action = action
        this.bundle = bundle
    }
}