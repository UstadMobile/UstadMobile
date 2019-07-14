package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.appcompat.widget.AppCompatImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleItemDetailPresenter
import com.ustadmobile.lib.db.entities.SaleItemReminder

class SaleItemReminderRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<SaleItemReminder>,
        internal var mPresenter: SaleItemDetailPresenter,
        internal var theActivity: Activity?,
        internal var theContext: Context) : PagedListAdapter<SaleItemReminder, SaleItemReminderRecyclerAdapter.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_reminder, parent, false)
        return ViewHolder(list)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val entity = getItem(position)

        val title = holder.itemView.findViewById<TextView>(R.id.item_reminder_days_tv)
        val deleteIV = holder.itemView.findViewById<AppCompatImageView>(R.id.item_reminder_dots_iv)

        var dayBeforeString = ""
        if (theActivity != null) {
            if (entity!!.saleItemReminderDays > 2) {
                dayBeforeString = theActivity!!.getString(R.string.days_before)
            } else {
                dayBeforeString = theActivity!!.getString(R.string.day_before)
            }
        }
        title.text = entity!!.saleItemReminderDays.toString() + " " + dayBeforeString

        deleteIV.setOnClickListener { mPresenter.handleDeleteReminder(entity.saleItemReminderUid) }

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
