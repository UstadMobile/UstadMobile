package com.ustadmobile.port.android.view

import com.toughra.ustadmobile.R
import tellh.com.recyclertreeview_lib.LayoutItemType



class EntityLayoutType(val name: String, val uid: Long, val selected: Boolean, var leaf: Boolean) : LayoutItemType {

    override fun getLayoutId(): Int {
        return R.layout.item_select_multiple_tree_dialog
    }

}