package com.ustadmobile.port.android.view

import com.toughra.ustadmobile.R

import tellh.com.recyclertreeview_lib.LayoutItemType

/**
 * This Layout Class represents every entry part of the Multi select recycler view.
 * This is the data that will get fed into the recycler view and hence would need to be
 * updated with at least the name and uid. Selected and leaf are navigation properties.
 */
class EntityLayoutType(var name: String, var uid: Long?, var selected: Boolean, var leaf: Boolean)
    : LayoutItemType {

    val layoutId: Int
        get() = R.layout.item_select_multiple_tree_dialog
}
