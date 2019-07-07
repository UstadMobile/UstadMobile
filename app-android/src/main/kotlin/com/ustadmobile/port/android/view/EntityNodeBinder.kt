package com.ustadmobile.port.android.view

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CommonEntityHandlerPresenter
import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewBinder


class EntityNodeBinder(var presenter: CommonEntityHandlerPresenter<*>) : TreeViewBinder<EntityNodeBinder.TreeHolder>() {

    override fun bindView(viewHolder: TreeHolder?, i: Int, treeNode: TreeNode<*>?) {
        val locationNode = treeNode!!.content as EntityLayoutType
        viewHolder!!.tvName.text = locationNode.name
        viewHolder.locationUid = locationNode.uid
    }

    override fun getLayoutId(): Int {
        return R.layout.item_select_multiple_tree_dialog
    }

    override fun provideViewHolder(view: View?): TreeHolder? {
        return TreeHolder(view!!)
    }

    inner class TreeHolder(rootView: View) : TreeViewBinder.ViewHolder(rootView) {

        val ivArrow: ImageView = rootView.findViewById(R.id.item_select_multiple_tree_dialog_arrow)
        var tvName: TextView = rootView.findViewById(R.id.item_select_multiple_tree_dialog_name)
        var checkBox: CheckBox = rootView.findViewById(R.id.item_select_multiple_tree_dialog_checkbox)
            internal set
        var locationUid: Long? = null

        init {
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                this.checkBox.isChecked = isChecked
                presenter.entityChecked(tvName.text.toString(), locationUid!!, isChecked)
            }
        }
    }
}