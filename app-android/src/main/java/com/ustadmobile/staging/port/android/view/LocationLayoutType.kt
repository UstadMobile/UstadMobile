package com.ustadmobile.staging.port.android.view

import com.toughra.ustadmobile.R

import tellh.com.recyclertreeview_lib.LayoutItemType

class LocationLayoutType : LayoutItemType {
    var name: String
    var uid: Long = 0
    var selected: Boolean = false
    var leaf: Boolean = false

    constructor(name: String) {
        this.name = name
    }

    constructor(name: String, uid: Long?, selected: Boolean, leaf: Boolean) {
        this.name = name
        this.uid = uid!!
        this.selected = selected
        this.leaf = leaf
    }

    override fun getLayoutId(): Int {
        return R.layout.item_select_multiple_tree_dialog
    }
}
