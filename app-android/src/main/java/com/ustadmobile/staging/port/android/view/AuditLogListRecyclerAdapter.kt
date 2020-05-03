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
import com.ustadmobile.core.controller.AuditLogListPresenter
import com.ustadmobile.lib.db.entities.AuditLogWithNames
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Person

class AuditLogListRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<AuditLogWithNames>,
        internal var mPresenter: AuditLogListPresenter,
        internal var theActivity: Activity,
        internal var theContext: Context) : PagedListAdapter<AuditLogWithNames,
        AuditLogListRecyclerAdapter.AuditLogListViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuditLogListViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false)
        return AuditLogListViewHolder(list)

    }

    override fun onBindViewHolder(holder: AuditLogListViewHolder, position: Int) {

        val entity = getItem(position)

        val title = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_title)
        val desc = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_desc)
        val menu = holder.itemView.findViewById<AppCompatImageView>(R.id
                .item_title_with_desc_and_dots_dots)

        //"Actor changed Entity Type Entity Name at Time"
        var entityType = ""
        var entityName: String? = ""
        when (entity!!.auditLogTableUid) {
            Clazz.TABLE_ID -> {
                entityType = theActivity.getText(R.string.clazz).toString()
                entityName = entity!!.clazzName
            }
            Person.TABLE_ID -> {
                entityType = theActivity.getText(R.string.person).toString()
                entityName = entity!!.personName
            }
            else -> {
            }
        }
        val logString = entity!!.actorName + " " + theActivity.getText(R.string.changed) + " " +
                entityType + " " + entityName
        title.setText(logString)

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener{ v: View ->
            //creating a popup menu
            val popup = PopupMenu(theActivity.applicationContext, v)

            popup.setOnMenuItemClickListener { item ->
                val i = item.itemId
                when (i) {
                    R.id.edit -> true
                    R.id.delete -> true
                    else -> false
                }
            }
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_item_schedule)

            //displaying the popup
            popup.show()
        }

        menu.setVisibility(View.GONE)
        desc.setVisibility(View.GONE)
    }

    class AuditLogListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
