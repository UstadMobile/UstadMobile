
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemPersonwithinventorycountBinding
import com.ustadmobile.core.controller.PersonWithInventoryCountListener
import com.ustadmobile.lib.db.entities.PersonWithInventoryCount
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class PersonWithInventoryCountRecyclerAdapter(var itemListener: PersonWithInventoryCountListener?)
    : SelectablePagedListAdapter<PersonWithInventoryCount,
        PersonWithInventoryCountRecyclerAdapter.ProductListViewHolder>(DIFF_CALLBACK) {

    class ProductListViewHolder(val itemBinding: ItemPersonwithinventorycountBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListViewHolder {
        val itemBinding = ItemPersonwithinventorycountBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        return ProductListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.person = item
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithInventoryCount> = object
            : DiffUtil.ItemCallback<PersonWithInventoryCount>() {
            override fun areItemsTheSame(oldItem: PersonWithInventoryCount,
                                         newItem: PersonWithInventoryCount): Boolean {
                return oldItem.personUid == newItem.personUid

            }

            override fun areContentsTheSame(oldItem: PersonWithInventoryCount,
                                            newItem: PersonWithInventoryCount): Boolean {
                return oldItem.fullName() == newItem.fullName() &&
                        oldItem == newItem
            }
        }
    }

}