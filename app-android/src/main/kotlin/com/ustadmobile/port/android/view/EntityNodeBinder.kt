package com.ustadmobile.port.android.view

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CommonEntityHandlerPresenter

import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewBinder

class EntityNodeBinder internal constructor(internal var mPresenter: CommonEntityHandlerPresenter)
    : TreeViewBinder<EntityNodeBinder.ViewHolder>() {

    val layoutId: Int
        get() = R.layout.item_select_multiple_tree_dialog

    fun provideViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    fun bindView(viewHolder: ViewHolder, i: Int, treeNode: TreeNode) {
        val locationNode = treeNode.getContent() as EntityLayoutType
        viewHolder.tvName.text = locationNode.name
        viewHolder.locationUid = locationNode.uid
    }

    inner class ViewHolder(rootView: View) : TreeViewBinder.ViewHolder(rootView) {

        val ivArrow: ImageView
        internal var tvName: TextView
        var checkBox: CheckBox
            internal set
        internal var locationUid: Long? = null

        init {
            this.tvName = rootView.findViewById(R.id.item_select_multiple_tree_dialog_name)
            this.ivArrow = rootView.findViewById(R.id.item_select_multiple_tree_dialog_arrow)
            this.checkBox = rootView.findViewById(R.id.item_select_multiple_tree_dialog_checkbox)

            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                this.checkBox.isChecked = isChecked
                mPresenter.entityChecked(tvName.text.toString(), locationUid, isChecked)

            }
        }
    }
}
