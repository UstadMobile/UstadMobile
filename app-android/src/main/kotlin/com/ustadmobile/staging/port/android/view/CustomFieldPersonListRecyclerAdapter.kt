package com.ustadmobile.staging.port.android.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.staging.core.controller.CustomFieldPersonListPresenter

class CustomFieldPersonListRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<CustomField>,
        internal var mPresenter: CustomFieldPersonListPresenter, internal var theActivity: Activity,
        internal var theContext: Context)
    : PagedListAdapter<CustomField, CustomFieldPersonListRecyclerAdapter.CustomFieldListViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomFieldListViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false)
        return CustomFieldListViewHolder(list)

    }

    override fun onBindViewHolder(holder: CustomFieldListViewHolder, position: Int) {

        val entity = getItem(position)

        val title = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_title)
        val desc = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_desc)
        val menu = holder.itemView.findViewById<AppCompatImageView>(R.id.item_title_with_desc_and_dots_dots)

        val impl = UstadMobileSystemImpl.instance
        assert(entity != null)
        title.setText(entity!!.customFieldName)
        when (entity.customFieldType) {
            CustomField.FIELD_TYPE_TEXT -> desc.setText(impl.getString(MessageID.text, theContext))
            CustomField.FIELD_TYPE_DROPDOWN -> desc.setText(impl.getString(MessageID.dropdown, theContext))
            else -> {
            }
        }

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener{ v: View ->
            //creating a popup menu
            val popup = PopupMenu(theActivity.applicationContext, v)

            popup.setOnMenuItemClickListener { item ->
                val i = item.itemId
                if (i == R.id.edit) {
                    mPresenter.handleClickEditCustomField(entity.customFieldUid)
                    true
                } else if (i == R.id.delete) {
                    mPresenter.handleClickDeleteCustomField(entity.customFieldUid)
                    true
                } else {
                    false
                }
            }
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_item_schedule)

            //displaying the popup
            popup.show()
        }


    }

    inner class CustomFieldListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
