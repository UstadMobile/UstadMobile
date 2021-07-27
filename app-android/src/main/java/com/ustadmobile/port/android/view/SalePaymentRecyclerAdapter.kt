
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemSalepaymentBinding
import com.ustadmobile.core.controller.SalePaymentItemListener
import com.ustadmobile.lib.db.entities.SalePaymentWithSaleItems
import com.ustadmobile.port.android.view.binding.MODE_START_OF_DAY


class SalePaymentRecyclerAdapter(var itemListener: SalePaymentItemListener?)
    : ListAdapter<SalePaymentWithSaleItems,
        SalePaymentRecyclerAdapter.InventoryTransactionDetailHolder>(DIFF_CALLBACK) {

    class InventoryTransactionDetailHolder(val itemBinding: ItemSalepaymentBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryTransactionDetailHolder {
        val itemBinding = ItemSalepaymentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        itemBinding.listener = itemListener
        itemBinding.dateTimeMode = MODE_START_OF_DAY
        itemBinding.timeZoneId = "Asia/Kabul"
        return InventoryTransactionDetailHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: InventoryTransactionDetailHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.salePayment = item
        holder.itemBinding.dateTimeMode = MODE_START_OF_DAY
        holder.itemBinding.timeZoneId = "Asia/Kabul"
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
                return oldItem.salePaymentUid == newItem.salePaymentUid

            }

            override fun areContentsTheSame(oldItem: SalePaymentWithSaleItems,
                                            newItem: SalePaymentWithSaleItems): Boolean {
                return oldItem == newItem
            }
        }
    }

}