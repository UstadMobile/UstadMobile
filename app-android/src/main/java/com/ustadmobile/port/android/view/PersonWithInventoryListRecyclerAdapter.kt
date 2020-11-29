
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemPersonwithinventoryBinding
import com.ustadmobile.lib.db.entities.PersonWithInventoryItemAndStock


class PersonWithInventoryListRecyclerAdapter()
    : ListAdapter<PersonWithInventoryItemAndStock,
        PersonWithInventoryListRecyclerAdapter.InventoryTransactionDetailHolder>(DIFF_CALLBACK) {

    class InventoryTransactionDetailHolder(val itemBinding: ItemPersonwithinventoryBinding)
        : RecyclerView.ViewHolder(itemBinding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryTransactionDetailHolder {
        val itemBinding = ItemPersonwithinventoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        return InventoryTransactionDetailHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: InventoryTransactionDetailHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.person = item

    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithInventoryItemAndStock> = object
            : DiffUtil.ItemCallback<PersonWithInventoryItemAndStock>() {
            override fun areItemsTheSame(oldItemWithInventoryItem: PersonWithInventoryItemAndStock,
                                         newItemWithInventoryItem: PersonWithInventoryItemAndStock): Boolean {
                return oldItemWithInventoryItem.personUid == newItemWithInventoryItem.personUid

            }

            override fun areContentsTheSame(oldItemWithInventoryItem: PersonWithInventoryItemAndStock,
                                            newItemWithInventoryItem: PersonWithInventoryItemAndStock): Boolean {
                return oldItemWithInventoryItem === newItemWithInventoryItem

            }
        }
    }

}