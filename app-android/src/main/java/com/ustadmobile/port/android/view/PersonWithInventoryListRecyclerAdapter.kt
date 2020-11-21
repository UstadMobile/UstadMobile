
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemPersonwithinventoryBinding
import com.ustadmobile.core.controller.CategoryListListener
import com.ustadmobile.lib.db.entities.PersonWithInventory
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class PersonWithInventoryListRecyclerAdapter()
    : ListAdapter<PersonWithInventory,
        PersonWithInventoryListRecyclerAdapter.InventoryTransactionDetailHolder>(DIFF_CALLBACK) {

    class InventoryTransactionDetailHolder(val itemBinding: ItemPersonwithinventoryBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

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
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithInventory> = object
            : DiffUtil.ItemCallback<PersonWithInventory>() {
            override fun areItemsTheSame(oldItem: PersonWithInventory,
                                         newItem: PersonWithInventory): Boolean {
                return oldItem.personUid == newItem.personUid

            }

            override fun areContentsTheSame(oldItem: PersonWithInventory,
                                            newItem: PersonWithInventory): Boolean {
                return oldItem == newItem
            }
        }
    }

}