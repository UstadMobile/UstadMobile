
package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemSaleitemwithproducerinventoryselectionBinding
import com.ustadmobile.lib.db.entities.ProductDeliveryWithProductAndTransactions


class ProductDeliveryWithProducersSelectionRecyclerAdapter()
    : ListAdapter<ProductDeliveryWithProductAndTransactions,
        ProductDeliveryWithProducersSelectionRecyclerAdapter.InventoryTransactionDetailHolder>(DIFF_CALLBACK) {

    class InventoryTransactionDetailHolder(val itemBinding: ItemSaleitemwithproducerinventoryselectionBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryTransactionDetailHolder {
        val itemBinding = ItemSaleitemwithproducerinventoryselectionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        return InventoryTransactionDetailHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: InventoryTransactionDetailHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.productDeets = item
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ProductDeliveryWithProductAndTransactions> = object
            : DiffUtil.ItemCallback<ProductDeliveryWithProductAndTransactions>() {
            override fun areItemsTheSame(oldItem: ProductDeliveryWithProductAndTransactions,
                                         newItem: ProductDeliveryWithProductAndTransactions): Boolean {
                return oldItem.productUid == newItem.productUid

            }

            override fun areContentsTheSame(oldItem: ProductDeliveryWithProductAndTransactions,
                                            newItem: ProductDeliveryWithProductAndTransactions): Boolean {
                return oldItem === newItem

            }
        }
    }

}