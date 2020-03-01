package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView

import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleDetailPresenter
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.lib.db.entities.SalePayment

class SalePaymentRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<SalePayment>,
        internal var mPresenter: SaleDetailPresenter,
        internal var theActivity: Activity,
        internal var theContext: Context) : PagedListAdapter<SalePayment, SalePaymentRecyclerAdapter.SaleDetailViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleDetailViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_key_value, parent, false)
        return SaleDetailViewHolder(list)

    }

    override fun onBindViewHolder(holder: SaleDetailViewHolder, position: Int) {

        val entity = getItem(position)

        val prettyDate = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                entity!!.salePaymentPaidDate)
        val keyTV = holder.itemView.findViewById<TextView>(R.id.item_reminder_days_tv)
        val valueTV = holder.itemView.findViewById<TextView>(R.id.item_key_value_value)
        val cl = holder.itemView.findViewById<ConstraintLayout>(R.id.item_key_value_cl)

        cl.setOnClickListener{mPresenter.handleEditPayment(entity.salePaymentUid)}

        val amountText = entity.salePaymentPaidAmount.toString() + " " +
                entity.salePaymentCurrency
        keyTV.text = prettyDate
        valueTV.text = amountText

        val dots = holder.itemView.findViewById<AppCompatImageView>(R.id.item_reminder_dots_iv)


        //Options to Edit/Delete every schedule in the list
        dots.setOnClickListener { v: View ->
            //creating a popup menu
            val popup = PopupMenu(theActivity, v)

            popup.setOnMenuItemClickListener { item ->
                val i = item.itemId
                if (i == R.id.edit) {
                    mPresenter.handleEditPayment(entity.salePaymentUid)
                    true
                } else if (i == R.id.delete) {
                    mPresenter.handleDeletePayment(entity.salePaymentUid)
                    true
                } else {
                    false
                }

            }
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_edit_delete)

            popup.menu.findItem(R.id.edit).isVisible = true

            //displaying the popup
            popup.show()
        }
    }

    inner class SaleDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
