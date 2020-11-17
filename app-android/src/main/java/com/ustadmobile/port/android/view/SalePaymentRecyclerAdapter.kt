
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemSalepaymentBinding
import com.ustadmobile.core.controller.SalePaymentItemListener
import com.ustadmobile.lib.db.entities.SalePayment


class SalePaymentRecyclerAdapter(var itemListener: SalePaymentItemListener?)
    : ListAdapter<SalePayment,
        SalePaymentRecyclerAdapter.InventoryTransactionDetailHolder>(DIFF_CALLBACK) {

    class InventoryTransactionDetailHolder(val itemBinding: ItemSalepaymentBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryTransactionDetailHolder {
        val itemBinding = ItemSalepaymentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        itemBinding.listener = itemListener
        return InventoryTransactionDetailHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: InventoryTransactionDetailHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.salePayment = item
        //holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SalePayment> = object
            : DiffUtil.ItemCallback<SalePayment>() {
            override fun areItemsTheSame(oldItem: SalePayment,
                                         newItem: SalePayment): Boolean {
                return oldItem.salePaymentUid == newItem.salePaymentUid

            }

            override fun areContentsTheSame(oldItem: SalePayment,
                                            newItem: SalePayment): Boolean {
                return oldItem == newItem
            }
        }
    }

}