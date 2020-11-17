
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemSaledeliveryBinding
import com.ustadmobile.core.controller.SaleDeliveryItemListener
import com.ustadmobile.lib.db.entities.SaleDelivery


class SaleDeliveryRecyclerAdapter(var itemListener: SaleDeliveryItemListener?)
    : ListAdapter<SaleDelivery,
        SaleDeliveryRecyclerAdapter.InventoryTransactionDetailHolder>(DIFF_CALLBACK) {

    class InventoryTransactionDetailHolder(val itemBinding: ItemSaledeliveryBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryTransactionDetailHolder {
        val itemBinding = ItemSaledeliveryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        itemBinding.listener = itemListener
        return InventoryTransactionDetailHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: InventoryTransactionDetailHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.saleDelivery = item
        //holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SaleDelivery> = object
            : DiffUtil.ItemCallback<SaleDelivery>() {
            override fun areItemsTheSame(oldItem: SaleDelivery,
                                         newItem: SaleDelivery): Boolean {
                return oldItem.saleDeliveryUid == newItem.saleDeliveryUid

            }

            override fun areContentsTheSame(oldItem: SaleDelivery,
                                            newItem: SaleDelivery): Boolean {
                return oldItem == newItem
            }
        }
    }

}