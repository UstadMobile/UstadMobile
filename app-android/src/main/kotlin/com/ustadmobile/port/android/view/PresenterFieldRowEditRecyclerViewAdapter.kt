package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.*
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField
import com.ustadmobile.lib.db.entities.PresenterFieldRow
import java.io.File

interface PresenterFieldImageHelper {

    fun onClickTakePicture(presenterFieldRow: PresenterFieldRow)

    fun onClickSelectPictureFromGallery(presenterFieldRow: PresenterFieldRow)

}

//There will be multiple types of viewholder here: Text, DropDown, Date, Header
class PresenterFieldRowEditRecyclerViewAdapter(var imageHelper: PresenterFieldImageHelper?) : ListAdapter<PresenterFieldRow, PresenterFieldRowEditRecyclerViewAdapter.PresenterFieldRowViewHolder>(DIFF_UTIL) {

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

    class PictureFieldRowViewHolder(var binding: ItemPresenterFieldRowPictureBinding,
                                    var imageHelper: PresenterFieldImageHelper?) : PresenterFieldRowViewHolder(binding.root), View.OnClickListener {

        init {
            binding.itemPresenterFieldRowPicturePhotoicon.setOnClickListener(this)
        }


        override var presenterFieldRow: PresenterFieldRow?
            get() = super.presenterFieldRow
            set(value) {
                super.presenterFieldRow = value
                binding.customFieldValue = value?.customFieldValue
            }

        override fun onClick(v: View) {
            val items = listOf(R.string.remove_photo, R.string.take_new_photo_from_camera,
                    R.string.select_new_photo_from_gallery).map { v.context.getString(it) }.toTypedArray()
            MaterialAlertDialogBuilder(itemView.context)
                    .setTitle(R.string.change_photo)
                    .setItems(items) {dialog, which ->
                        //handle this
                        val presenterFieldRowVal = presenterFieldRow ?: return@setItems
                        if(which == 1) {
                            imageHelper?.onClickTakePicture(presenterFieldRowVal)
                        }
                    }
                    .show()
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
                    ItemPresenterFieldRowPictureBinding.inflate(inflater, parent, false), imageHelper)
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
        imageHelper = null
    }

    companion object {
        val DIFF_UTIL = object: DiffUtil.ItemCallback<PresenterFieldRow>() {
            override fun areItemsTheSame(oldItem: PresenterFieldRow, newItem: PresenterFieldRow): Boolean {
                return oldItem.presenterField?.fieldUid == newItem.presenterField?.fieldUid
            }

            override fun areContentsTheSame(oldItem: PresenterFieldRow, newItem: PresenterFieldRow): Boolean {
                //Because we are using two way data binding...
                return oldItem === newItem
            }
        }
    }

}