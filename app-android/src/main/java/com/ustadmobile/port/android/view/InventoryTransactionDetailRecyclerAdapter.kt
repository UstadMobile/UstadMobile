
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemInventorytransactiondetailBinding
import com.ustadmobile.core.controller.InventoryTransactionDetailListener
import com.ustadmobile.lib.db.entities.InventoryTransactionDetail
import com.ustadmobile.port.android.view.binding.MODE_START_OF_DAY
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class InventoryTransactionDetailRecyclerAdapter(var itemListener: InventoryTransactionDetailListener?)
    : SelectablePagedListAdapter<InventoryTransactionDetail,
        InventoryTransactionDetailRecyclerAdapter.InventoryTransactionDetailHolder>(DIFF_CALLBACK) {

    class InventoryTransactionDetailHolder(val itemBinding: ItemInventorytransactiondetailBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryTransactionDetailHolder {
        val itemBinding = ItemInventorytransactiondetailBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        itemBinding.dateTimeMode = MODE_START_OF_DAY
        itemBinding.timeZoneId = "Asia/Kabul"
        return InventoryTransactionDetailHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: InventoryTransactionDetailHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.entry = item
        holder.itemBinding.dateTimeMode = MODE_START_OF_DAY
        holder.itemBinding.timeZoneId = "Asia/Kabul"
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<InventoryTransactionDetail> = object
            : DiffUtil.ItemCallback<InventoryTransactionDetail>() {
            override fun areItemsTheSame(oldItem: InventoryTransactionDetail,
                                         newItem: InventoryTransactionDetail): Boolean {
                return oldItem == newItem

            }

            override fun areContentsTheSame(oldItem: InventoryTransactionDetail,
                                            newItem: InventoryTransactionDetail): Boolean {
                return oldItem == newItem
            }
        }
    }

}