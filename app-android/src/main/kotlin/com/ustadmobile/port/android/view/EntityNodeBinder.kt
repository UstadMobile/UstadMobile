package com.ustadmobile.port.android.view

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CommonEntityHandlerPresenter

import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewBinder

class EntityNodeBinder (
        var mPresenter: CommonEntityHandlerPresenter<*>)
    : TreeViewBinder<EntityNodeBinder.TreeHolder>() {

    override fun getLayoutId(): Int {
        return R.layout.item_select_multiple_tree_dialog
    }

    override fun provideViewHolder(view: View): TreeHolder {
        return TreeHolder(view)
    }

    override fun bindView(viewHolder: TreeHolder, i: Int, treeNode: TreeNode<*>?) {
        val locationNode = treeNode!!.getContent() as EntityLayoutType
        viewHolder.tvName.text = locationNode.name
        viewHolder.locationUid = locationNode.uid
    }

    inner class TreeHolder(rootView: View) : TreeViewBinder.ViewHolder(rootView) {

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