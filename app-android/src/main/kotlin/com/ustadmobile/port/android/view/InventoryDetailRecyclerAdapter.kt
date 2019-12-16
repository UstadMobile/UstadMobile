package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.InventoryDetailPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.lib.db.entities.InventoryTransactionDetail
import kotlinx.coroutines.Job

class InventoryDetailRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<InventoryTransactionDetail>,
        internal var mPresenter: InventoryDetailPresenter,
        internal var theActivity: Activity,
        internal var theContext: Context)
    : PagedListAdapter<InventoryTransactionDetail,
        InventoryDetailRecyclerAdapter.InventoryListViewHolder>(diffCallback) {

    private var impl = UstadMobileSystemImpl.instance

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryListViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_inventory_transaction, parent, false)
        return InventoryListViewHolder(list)

    }


    override fun onBindViewHolder(holder: InventoryListViewHolder, position: Int) {


        val cl = holder.itemView.findViewById<ConstraintLayout>(R.id.item_inventory_transaction_cl)
        val type = holder.itemView.findViewById<TextView>(R.id.item_inventory_transaction_mode)
        val desc = holder.itemView.findViewById<TextView>(R.id.item_inventory_transaction_desc)
        val date = holder.itemView.findViewById<TextView>(R.id.item_inventory_transaction_date)
        val icon = holder.itemView.findViewById<ImageView>(R.id.imageView11)

        val entity = getItem(position)

        var typeText = ""
        if(entity!!.saleUid == 0L){
            typeText = theActivity.getText(R.string.receive).toString()
            icon.visibility = View.VISIBLE
            icon.setImageResource(R.drawable.ic_arrow_forward_24dp)
        }else{
            typeText = theActivity.getText(R.string.sell).toString()
            icon.visibility = View.VISIBLE
            icon.setImageResource(R.drawable.ic_arrow_left_24dp)
        }

        val descText = entity.stockCount.toString() + " " + theActivity.getText(R.string.by) + " " +
                entity.weNames
        type.setText(typeText)
        desc.setText(descText)

        val prettyDate = UMCalendarUtil.getPrettyDateFromLong(entity!!.transactionDate, "")
        date.setText(prettyDate)

        cl.setOnClickListener({
            if(entity!!.saleUid != 0L){
                mPresenter.handleClickSaleTransaction(entity!!.saleUid)
            }else{
                mPresenter.handleClickInventoryTransaction(entity!!.transactionDate)
            }
        })

    }

    inner class InventoryListViewHolder(itemView: View, var imageLoadJob: Job? = null)
        : RecyclerView.ViewHolder(itemView)

    companion object {


    }


}
