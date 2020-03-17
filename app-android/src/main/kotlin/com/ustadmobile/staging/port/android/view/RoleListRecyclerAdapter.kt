package com.ustadmobile.staging.port.android.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.RoleListPresenter
import com.ustadmobile.lib.db.entities.Role

class RoleListRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<Role>,
        internal var mPresenter: RoleListPresenter,
        internal var theActivity: Activity,
        internal var theContext: Context) : PagedListAdapter<Role, RoleListRecyclerAdapter.RoleListViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoleListViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false)
        return RoleListViewHolder(list)

    }

    override fun onBindViewHolder(holder: RoleListViewHolder, position: Int) {

        val entity = getItem(position)

        val title = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_title)
        val desc = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_desc)
        val menu = holder.itemView.findViewById<AppCompatImageView>(R.id
                .item_title_with_desc_and_dots_dots)

        holder.itemView.setOnClickListener({ v ->
            assert(entity != null)
            mPresenter.handleEditRole(entity!!.roleUid)
        })

        assert(entity != null)
        title.setText(entity!!.roleName)
        val rolePermissions = entity!!.rolePermissions
        val count = java.lang.Long.bitCount(rolePermissions)
        var permissionString = theActivity.getText(R.string.permissions).toString()
        if (count == 1) {
            permissionString = theActivity.getText(R.string.permission).toString()
        }
        val roleDesc = "$count $permissionString"
        desc.setText(roleDesc)

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener{ v: View ->
            //creating a popup menu
            val popup = PopupMenu(theActivity.applicationContext, v)

            popup.setOnMenuItemClickListener { item ->
                val i = item.itemId
                if (i == R.id.edit) {
                    mPresenter.handleEditRole(entity!!.roleUid)
                    true
                } else if (i == R.id.delete) {
                    mPresenter.handleRoleDelete(entity!!.roleUid)
                    true
                } else {
                    false
                }
            }
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_item_schedule)

            //displaying the popup
            popup.show()
        }

    }

    inner class RoleListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
