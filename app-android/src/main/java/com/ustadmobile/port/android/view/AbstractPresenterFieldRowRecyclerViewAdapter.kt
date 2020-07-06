package com.ustadmobile.port.android.view

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemPresenterFieldRowHeaderBinding
import com.ustadmobile.lib.db.entities.PresenterFieldRow

abstract class AbstractPresenterFieldRowRecyclerViewAdapter(diffUtil : DiffUtil.ItemCallback<PresenterFieldRow>)
    : ListAdapter<PresenterFieldRow, AbstractPresenterFieldRowRecyclerViewAdapter.PresenterFieldRowViewHolder>(diffUtil) {

    abstract class PresenterFieldRowViewHolder(view: View, open var presenterFieldRow: PresenterFieldRow? = null)
        : RecyclerView.ViewHolder(view)

    class UnsupportedFieldRowViewHolder(view: View): PresenterFieldRowViewHolder(view)

    class HeaderFieldRowViewHolder(var binding: ItemPresenterFieldRowHeaderBinding) : PresenterFieldRowViewHolder(binding.root) {
        override var presenterFieldRow: PresenterFieldRow?
            get() = super.presenterFieldRow
            set(value) {
                super.presenterFieldRow = value
                binding.presenterField = presenterFieldRow?.presenterField
            }
    }


    override fun getItemViewType(position: Int): Int {
        val presenterFieldRow = getItem(position)
        return presenterFieldRow.customField?.customFieldType ?: presenterFieldRow?.presenterField?.fieldType ?: -1
    }

}