
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemSaleListBinding
import com.ustadmobile.core.controller.SaleListItemListener
import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleListDetail
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ustadmobile.port.android.view.binding.MODE_START_OF_DAY
import com.ustadmobile.port.android.view.ext.setSelectedIfInList


class SaleListRecyclerAdapter(var itemListener: SaleListItemListener?): SelectablePagedListAdapter<SaleListDetail, SaleListRecyclerAdapter.SaleListViewHolder>(DIFF_CALLBACK) {

    class SaleListViewHolder(val itemBinding: ItemSaleListBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleListViewHolder {
        val itemBinding = ItemSaleListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        itemBinding.dateTimeMode = MODE_START_OF_DAY
        itemBinding.timeZoneId = "Asia/Kabul"
        return SaleListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: SaleListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.sale = item
        holder.itemBinding.dateTimeMode = MODE_START_OF_DAY
        holder.itemBinding.timeZoneId = "Asia/Kabul"
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SaleListDetail> = object
            : DiffUtil.ItemCallback<SaleListDetail>() {
            override fun areItemsTheSame(oldItem: SaleListDetail,
                                         newItem: SaleListDetail): Boolean {
                return oldItem.saleUid == newItem.saleUid
            }

            override fun areContentsTheSame(oldItem: SaleListDetail,
                                            newItem: SaleListDetail): Boolean {
                return oldItem == newItem
            }
        }
    }

}