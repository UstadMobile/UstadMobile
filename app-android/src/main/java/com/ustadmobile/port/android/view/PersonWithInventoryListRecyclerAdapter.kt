
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemPersonwithinventoryBinding
import com.ustadmobile.lib.db.entities.PersonWithInventoryItemAndStock


class PersonWithInventoryListRecyclerAdapter(var deliveryMode: Boolean = false,
                                             var newDelivery: Boolean = false )
    : ListAdapter<PersonWithInventoryItemAndStock,
        PersonWithInventoryListRecyclerAdapter.InventoryTransactionDetailHolder>(DIFF_CALLBACK) {

    class InventoryTransactionDetailHolder(val itemBinding: ItemPersonwithinventoryBinding)
        : RecyclerView.ViewHolder(itemBinding.root), SeekBar.OnSeekBarChangeListener{

        private val editText = itemBinding.viewProducerWithInventorySelectionSelectedEdittext

        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            editText.setText(p1.toString())
        }

        override fun onStartTrackingTouch(p0: SeekBar?) { }

        override fun onStopTrackingTouch(p0: SeekBar?) { }

    }


    var saleDeliveryMode: Boolean? = deliveryMode
    var newSaleDelivery: Boolean? = newDelivery



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryTransactionDetailHolder {
        val itemBinding = ItemPersonwithinventoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        return InventoryTransactionDetailHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: InventoryTransactionDetailHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.person = item
        holder.itemBinding.saleDeliveryMode = saleDeliveryMode
        holder.itemBinding.newSaleDelivery = newSaleDelivery
        holder.itemBinding.viewProducerWithInventorySelectionSeekbar.setOnSeekBarChangeListener(holder)


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