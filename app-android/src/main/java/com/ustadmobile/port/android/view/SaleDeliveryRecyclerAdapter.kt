
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemSaledeliveryBinding
import com.ustadmobile.core.controller.SaleDeliveryItemListener
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SaleDeliveryAndItems


class SaleDeliveryRecyclerAdapter(var itemListener: SaleDeliveryItemListener?)
    : ListAdapter<SaleDeliveryAndItems,
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
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SaleDeliveryAndItems> = object
            : DiffUtil.ItemCallback<SaleDeliveryAndItems>() {
            override fun areItemsTheSame(oldItem: SaleDeliveryAndItems,
                                         newItem: SaleDeliveryAndItems): Boolean {
                return oldItem?.saleDeliveryUid == newItem?.saleDeliveryUid

            }

            override fun areContentsTheSame(oldItem: SaleDeliveryAndItems,
                                            newItem: SaleDeliveryAndItems): Boolean {
                return oldItem == newItem
            }
        }
    }

}