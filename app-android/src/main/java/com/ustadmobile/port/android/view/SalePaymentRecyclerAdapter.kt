
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemSalepaymentBinding
import com.ustadmobile.core.controller.SalePaymentItemListener
import com.ustadmobile.lib.db.entities.SalePaymentWithSaleItems


class SalePaymentRecyclerAdapter(var itemListener: SalePaymentItemListener?)
    : ListAdapter<SalePaymentWithSaleItems,
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
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SalePaymentWithSaleItems> = object
            : DiffUtil.ItemCallback<SalePaymentWithSaleItems>() {
            override fun areItemsTheSame(oldItem: SalePaymentWithSaleItems,
                                         newItem: SalePaymentWithSaleItems): Boolean {
                return oldItem.payment.salePaymentUid == newItem.payment.salePaymentUid

            }

            override fun areContentsTheSame(oldItem: SalePaymentWithSaleItems,
                                            newItem: SalePaymentWithSaleItems): Boolean {
                return oldItem == newItem
            }
        }
    }

}