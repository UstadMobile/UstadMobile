
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemSaleitemBinding
import com.ustadmobile.core.controller.SaleItemListItemListener
import com.ustadmobile.lib.db.entities.SaleItemWithProduct


class SaleItemRecyclerAdapter(var itemListener: SaleItemListItemListener?)
    : ListAdapter<SaleItemWithProduct,
        SaleItemRecyclerAdapter.InventoryTransactionDetailHolder>(DIFF_CALLBACK) {

    class InventoryTransactionDetailHolder(val itemBinding: ItemSaleitemBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryTransactionDetailHolder {
        val itemBinding = ItemSaleitemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        itemBinding.listener = itemListener
        return InventoryTransactionDetailHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: InventoryTransactionDetailHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.saleItem = item
        //holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SaleItemWithProduct> = object
            : DiffUtil.ItemCallback<SaleItemWithProduct>() {
            override fun areItemsTheSame(oldItem: SaleItemWithProduct,
                                         newItem: SaleItemWithProduct): Boolean {
                return oldItem.saleItemUid == newItem.saleItemUid

            }

            override fun areContentsTheSame(oldItem: SaleItemWithProduct,
                                            newItem: SaleItemWithProduct): Boolean {
                return oldItem == newItem
            }
        }
    }

}