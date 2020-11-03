
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemProductImageBinding
import com.ustadmobile.core.controller.ProductImageListener
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


//TODO: Change to Product picture when that is ready
class ProductImageRecyclerAdapter(var itemListener: ProductImageListener?)
    : SelectablePagedListAdapter<Product,
        ProductImageRecyclerAdapter.ProductListViewHolder>(DIFF_CALLBACK) {

    class ProductListViewHolder(val itemBinding: ItemProductImageBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListViewHolder {
        val itemBinding = ItemProductImageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
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
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Product> = object
            : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(oldItem: Product,
                                         newItem: Product): Boolean {
                return oldItem.productUid == newItem.productUid

            }

            override fun areContentsTheSame(oldItem: Product,
                                            newItem: Product): Boolean {
                return oldItem == newItem
            }
        }
    }

}