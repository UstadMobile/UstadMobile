package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.*
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField
import com.ustadmobile.lib.db.entities.PresenterFieldRow


//There will be multiple types of viewholder here: Text, DropDown, Date, Header
class PresenterFieldRowEditRecyclerViewAdapter() : ListAdapter<PresenterFieldRow, PresenterFieldRowEditRecyclerViewAdapter.PresenterFieldRowViewHolder>(DIFF_UTIL) {

    abstract class PresenterFieldRowViewHolder(view: View, open var presenterFieldRow: PresenterFieldRow? = null)
        : RecyclerView.ViewHolder(view)

    class TextFieldRowViewHolder(var binding: ItemPresenterFieldRowTextBinding) : PresenterFieldRowViewHolder(binding.root) {
        override var presenterFieldRow: PresenterFieldRow?
            get() = super.presenterFieldRow
            set(value) {
                super.presenterFieldRow = value
                binding.customFieldValue = presenterFieldRow?.customFieldValue
                binding.customField = presenterFieldRow?.customField
            }
    }

    class DateFieldRowViewHolder(var binding: ItemPresenterFieldDateBinding) : PresenterFieldRowViewHolder(binding.root) {
        override var presenterFieldRow: PresenterFieldRow?
            get() = super.presenterFieldRow
            set(value) {
                super.presenterFieldRow = value
                binding.customField = presenterFieldRow?.customField
                binding.customFieldValue = presenterFieldRow?.customFieldValue
            }
    }

    class HeaderFieldRowViewHolder(var binding: ItemPresenterFieldRowHeaderBinding) : PresenterFieldRowViewHolder(binding.root) {
        override var presenterFieldRow: PresenterFieldRow?
            get() = super.presenterFieldRow
            set(value) {
                super.presenterFieldRow = value
                binding.presenterField = presenterFieldRow?.presenterField
            }
    }

    class DropdownFieldRowViewHolder(var binding: ItemPresenterFieldRowDropDownBinding) : PresenterFieldRowViewHolder(binding.root) {
        override var presenterFieldRow: PresenterFieldRow?
            get() = super.presenterFieldRow
            set(value) {
                super.presenterFieldRow = value
                binding.customFieldValue = presenterFieldRow?.customFieldValue
                binding.customFieldValueOptions = presenterFieldRow?.customFieldOptions
                binding.customField = presenterFieldRow?.customField
            }
    }

    class PictureFieldRowViewHolder(var binding: ItemPresenterFieldRowPictureBinding) : PresenterFieldRowViewHolder(binding.root) {

        override var presenterFieldRow: PresenterFieldRow?
            get() = super.presenterFieldRow
            set(value) {
                super.presenterFieldRow = value
                binding.customFieldValue = value?.customFieldValue
            }
    }


    class UnsupportedFieldRowViewHolder(view: View): PresenterFieldRowViewHolder(view)


    override fun getItemViewType(position: Int): Int {
        val presenterFieldRow = getItem(position)
        return presenterFieldRow.customField?.customFieldType ?: presenterFieldRow?.presenterField?.fieldType ?: -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresenterFieldRowViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            CustomField.FIELD_TYPE_TEXT -> TextFieldRowViewHolder(
                    ItemPresenterFieldRowTextBinding.inflate(inflater, parent, false))
            PersonDetailPresenterField.TYPE_HEADER -> HeaderFieldRowViewHolder(
                    ItemPresenterFieldRowHeaderBinding.inflate(inflater, parent, false))
            CustomField.FIELD_TYPE_DATE_SPINNER -> DateFieldRowViewHolder(
                    ItemPresenterFieldDateBinding.inflate(inflater, parent, false))
            CustomField.FIELD_TYPE_DROPDOWN -> DropdownFieldRowViewHolder(
                    ItemPresenterFieldRowDropDownBinding.inflate(inflater, parent, false))
            CustomField.FIELD_TYPE_PICTURE -> PictureFieldRowViewHolder(
                    ItemPresenterFieldRowPictureBinding.inflate(inflater, parent, false))
            else -> UnsupportedFieldRowViewHolder(
                    inflater.inflate(R.layout.item_presenter_field_row_unsupported,parent, false))
        }
    }

    override fun onBindViewHolder(holder: PresenterFieldRowViewHolder, position: Int) {
        val presenterFieldRow = getItem(position)
        holder.presenterFieldRow = presenterFieldRow
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
    }

    companion object {
        val DIFF_UTIL = object: DiffUtil.ItemCallback<PresenterFieldRow>() {
            override fun areItemsTheSame(oldItem: PresenterFieldRow, newItem: PresenterFieldRow): Boolean {
                return oldItem.presenterField?.fieldUid == newItem.presenterField?.fieldUid
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: PresenterFieldRow, newItem: PresenterFieldRow): Boolean {
                //Because we are using two way data binding, we want to make sure that we save data to the same instance!
                return oldItem === newItem
            }
        }
    }

}