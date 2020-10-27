
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemProductListBinding
import com.ustadmobile.core.controller.ProductListItemListener
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ustadmobile.port.android.view.ext.setSelectedIfInList


class ProductListRecyclerAdapter(var itemListener: ProductListItemListener?)
    : SelectablePagedListAdapter<ProductWithInventoryCount,
        ProductListRecyclerAdapter.ProductListViewHolder>(DIFF_CALLBACK) {

    class ProductListViewHolder(val itemBinding: ItemProductListBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListViewHolder {
        val itemBinding = ItemProductListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        return ProductListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.product = item
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ProductWithInventoryCount> = object
            : DiffUtil.ItemCallback<ProductWithInventoryCount>() {
            override fun areItemsTheSame(oldItem: ProductWithInventoryCount,
                                         newItem: ProductWithInventoryCount): Boolean {
                return oldItem.productUid == newItem.productUid

            }

            override fun areContentsTheSame(oldItem: ProductWithInventoryCount,
                                            newItem: ProductWithInventoryCount): Boolean {
                return oldItem == newItem
            }
        }
    }

}