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
import com.ustadmobile.core.controller.GroupListPresenter
import com.ustadmobile.lib.db.entities.GroupWithMemberCount

class GroupListRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<GroupWithMemberCount>,
        internal var mPresenter: GroupListPresenter,
        internal var theActivity: Activity,
        internal var theContext: Context)
    : PagedListAdapter<GroupWithMemberCount, GroupListRecyclerAdapter.GroupListViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupListViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false)
        return GroupListViewHolder(list)

    }

    override fun onBindViewHolder(holder: GroupListViewHolder, position: Int) {

        val entity = getItem(position)
        val title = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_title)
        val desc = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_desc)
        val menu = holder.itemView.findViewById<AppCompatImageView>(R.id
                .item_title_with_desc_and_dots_dots)

        holder.itemView.setOnClickListener { v ->
            assert(entity != null)
            mPresenter.handleEditGroup(entity!!.groupUid)
        }

        assert(entity != null)
        if (entity == null) {
            return
        }
        title.text = entity.groupName
        var membersString = theActivity.getText(R.string.members).toString()
        val count = entity.memberCount
        if (count == 1) {
            membersString = theActivity.getText(R.string.member).toString()

        }
        val descString = "$count $membersString"
        desc.text = descString

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener{ v: View ->
            //creating a popup menu
            val popup = PopupMenu(theActivity, v)
            popup.setOnMenuItemClickListener { item ->
                val i = item.itemId
                if (i == R.id.edit) {
                    mPresenter.handleEditGroup(entity.groupUid)
                    true
                } else if (i == R.id.delete) {
                    mPresenter.handleDeleteGroup(entity.groupUid)
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

    inner class GroupListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
