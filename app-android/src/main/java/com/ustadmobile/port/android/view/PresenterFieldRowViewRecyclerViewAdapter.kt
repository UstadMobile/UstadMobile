package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.*
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField
import com.ustadmobile.lib.db.entities.PresenterFieldRow

class PresenterFieldRowViewRecyclerViewAdapter: AbstractPresenterFieldRowRecyclerViewAdapter(DIFF_UTIL) {

    class TextRowViewHolder(var binding: ItemPresenterFieldRowViewTextBinding): PresenterFieldRowViewHolder(binding.root) {
        override var presenterFieldRow: PresenterFieldRow?
            get() = super.presenterFieldRow
            set(value) {
                super.presenterFieldRow = value
                binding.customFieldValue = presenterFieldRow?.customFieldValue
                binding.customField = presenterFieldRow?.customField
            }
    }

    class DropDownRowViewHolder(var binding: ItemPresenterFieldRowViewDropdownBinding) : PresenterFieldRowViewHolder(binding.root) {
        override var presenterFieldRow: PresenterFieldRow?
            get() = super.presenterFieldRow
            set(value) {
                super.presenterFieldRow = value
                binding.customFieldValue = value?.customFieldValue
                binding.customFieldOptions = value?.customFieldOptions
                binding.customField = value?.customField
            }
    }

    class DateRowViewHolder(var binding: ItemPresenterFieldRowViewDateBinding) : PresenterFieldRowViewHolder(binding.root) {
        override var presenterFieldRow: PresenterFieldRow?
            get() = super.presenterFieldRow
            set(value) {
                super.presenterFieldRow = value
                binding.customField = value?.customField
                binding.customFieldValue = value?.customFieldValue
            }
    }


    class ImageRowViewHolder(var binding: ItemPresenterFieldRowViewImageBinding) : PresenterFieldRowViewHolder(binding.root) {
        override var presenterFieldRow: PresenterFieldRow?
            get() = super.presenterFieldRow
            set(value) {
                super.presenterFieldRow = value
                binding.customFieldValue = value?.customFieldValue
            }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresenterFieldRowViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            CustomField.FIELD_TYPE_TEXT -> TextRowViewHolder(
                    ItemPresenterFieldRowViewTextBinding.inflate(inflater, parent, false))
            PersonDetailPresenterField.TYPE_HEADER -> HeaderFieldRowViewHolder(
                    ItemPresenterFieldRowHeaderBinding.inflate(inflater, parent, false))
            CustomField.FIELD_TYPE_DROPDOWN -> DropDownRowViewHolder(
                    ItemPresenterFieldRowViewDropdownBinding.inflate(inflater, parent, false))
            CustomField.FIELD_TYPE_DATE_SPINNER -> DateRowViewHolder(
                    ItemPresenterFieldRowViewDateBinding.inflate(inflater, parent, false))
            CustomField.FIELD_TYPE_PICTURE -> ImageRowViewHolder(
                    ItemPresenterFieldRowViewImageBinding.inflate(inflater, parent, false))
            else -> UnsupportedFieldRowViewHolder(
                    inflater.inflate(R.layout.item_presenter_field_row_unsupported, parent, false))
        }
    }

    override fun onBindViewHolder(holder: PresenterFieldRowViewHolder, position: Int) {
        val presenterFieldRow = getItem(position)
        holder.presenterFieldRow = presenterFieldRow
    }

    companion object {

        val DIFF_UTIL = object: DiffUtil.ItemCallback<PresenterFieldRow>() {
            override fun areItemsTheSame(oldItem: PresenterFieldRow, newItem: PresenterFieldRow): Boolean {
                return oldItem.presenterField?.fieldUid == newItem.presenterField?.fieldUid
            }

            override fun areContentsTheSame(oldItem: PresenterFieldRow, newItem: PresenterFieldRow): Boolean {
                return oldItem == newItem
            }
        }

    }

}