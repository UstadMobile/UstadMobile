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
import com.ustadmobile.core.controller.HolidayCalendarListPresenter
import com.ustadmobile.lib.db.entities.UMCalendarWithNumEntries

class HolidayCalendarListRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<UMCalendarWithNumEntries>,
        internal var mPresenter: HolidayCalendarListPresenter, internal var theActivity: Activity,
        internal var theContext: Context)
    : PagedListAdapter<UMCalendarWithNumEntries, HolidayCalendarListRecyclerAdapter.HolidayCalendarListViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayCalendarListViewHolder {

        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false)
        return HolidayCalendarListViewHolder(list)

    }

    override fun onBindViewHolder(holder: HolidayCalendarListViewHolder, position: Int) {

        val entity = getItem(position)

        val title = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_title)
        val desc = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_desc)
        val menu = holder.itemView.findViewById<AppCompatImageView>(R.id.item_title_with_desc_and_dots_dots)

        assert(entity != null)
        val entityTitle = entity!!.umCalendarName
        title.text = entityTitle
        val numEntries = entity.numEntries
        var entriesString = theActivity.getText(R.string.entries).toString()
        if (numEntries == 1) {
            entriesString = theActivity.getText(R.string.entry).toString()
        }
        val numEntitiesString = "$numEntries $entriesString"
        desc.text = numEntitiesString

        holder.itemView.setOnClickListener { v -> mPresenter.handleEditCalendar(entity.umCalendarUid) }

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener { v: View ->
            //creating a popup menu
            val popup = PopupMenu(theActivity.applicationContext, v)

            popup.setOnMenuItemClickListener { item ->
                val i = item.itemId
                if (i == R.id.edit) {
                    mPresenter.handleEditCalendar(entity.umCalendarUid)
                    true
                } else if (i == R.id.delete) {
                    mPresenter.handleDeleteCalendar(entity.umCalendarUid)
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

    inner class HolidayCalendarListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
