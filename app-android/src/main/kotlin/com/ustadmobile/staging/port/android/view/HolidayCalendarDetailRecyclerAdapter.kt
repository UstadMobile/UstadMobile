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
import com.ustadmobile.core.controller.HolidayCalendarDetailPresenter
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.lib.db.entities.DateRange

class HolidayCalendarDetailRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<DateRange>,
        internal var mPresenter: HolidayCalendarDetailPresenter,
        internal var theActivity: Activity,
        internal var theContext: Context)
    : PagedListAdapter<DateRange, HolidayCalendarDetailRecyclerAdapter.HolidayCalendarDetailViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayCalendarDetailViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false)
        return HolidayCalendarDetailViewHolder(list)

    }

    override fun onBindViewHolder(holder: HolidayCalendarDetailViewHolder, position: Int) {

        val entity = getItem(position)

        val title = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_title)
        val desc = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_desc)
        val menu = holder.itemView.findViewById<AppCompatImageView>(R.id.item_title_with_desc_and_dots_dots)

        assert(entity != null)
        var rangeString = UMCalendarUtil.getPrettySuperSimpleDateSimpleWithoutYearFromLong(entity!!.dateRangeFromDate)

        if (entity!!.dateRangeToDate > 0) {
            rangeString = rangeString + " - " +
                    UMCalendarUtil.getPrettySuperSimpleDateSimpleWithoutYearFromLong(entity!!.dateRangeToDate)
        }
        title.setText(rangeString)

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener{ v: View ->
            //creating a popup menu
            val popup = PopupMenu(theActivity.applicationContext, v)

            popup.setOnMenuItemClickListener { item ->
                val i = item.itemId
                if (i == R.id.edit) {
                    mPresenter.handleEditRange(entity!!.dateRangeUid)
                    true
                } else if (i == R.id.delete) {
                    mPresenter.handleDeleteRange(entity!!.dateRangeUid)
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


    inner class HolidayCalendarDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
