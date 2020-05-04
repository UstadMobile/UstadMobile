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
import com.ustadmobile.core.controller.PersonDetailPresenter
import com.ustadmobile.core.controller.PersonEditPresenter
import com.ustadmobile.core.controller.RoleAssignmentListPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.*

class RoleAssignmentListRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<EntityRoleWithGroupName>,
        internal var mPresenter: RoleAssignmentListPresenter?,
        internal var theActivity: Activity,
        internal var theContext: Context)
    : PagedListAdapter<EntityRoleWithGroupName, RoleAssignmentListRecyclerAdapter.RoleAssignmentListViewHolder>(diffCallback) {

    val impl = UstadMobileSystemImpl.instance

    private var pPresenter: PersonDetailPresenter? = null
    private var pePresenter: PersonEditPresenter? = null


    constructor(diffCallback: DiffUtil.ItemCallback<EntityRoleWithGroupName>,
                mPresenter: RoleAssignmentListPresenter?,
                personDetailPresenter: PersonDetailPresenter,
                theActivity: Activity,
                theContext: Context):this(diffCallback, mPresenter, theActivity, theContext){
        if(personDetailPresenter != null) {
            pPresenter = personDetailPresenter
        }
    }

    constructor(diffCallback: DiffUtil.ItemCallback<EntityRoleWithGroupName>,
                mPresenter: RoleAssignmentListPresenter?,
                personEditPresenter: PersonEditPresenter,
                theActivity: Activity,
                theContext: Context):this(diffCallback, mPresenter, theActivity, theContext){
        if(personEditPresenter != null) {
            pePresenter = personEditPresenter
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoleAssignmentListViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false)
        return RoleAssignmentListViewHolder(list)

    }

    override fun onBindViewHolder(holder: RoleAssignmentListViewHolder, position: Int) {

        val entity = getItem(position)

        val title = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_title)
        val desc = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_desc)
        val menu = holder.itemView.findViewById<AppCompatImageView>(R.id
                .item_title_with_desc_and_dots_dots)

        holder.itemView.setOnClickListener {
            if(mPresenter != null) {
                mPresenter!!.handleEditRoleAssignment(entity!!.erUid)
            }
        }

        //Usually called in Person edit/detail
        if(mPresenter == null){
            //Reduce size
            title.textSize = 14.toFloat()
            desc.textSize = 14.toFloat()
        }


        var groupName = entity!!.groupName

        if(entity.groupPersonName != null){
            groupName = entity.groupPersonName
        }else{
            "A person group"
        }

        val titleText = groupName + " -> " + entity!!.roleName

        var scopeName: String? = null
        var assigneeName: String? = null
        when (entity.erTableId) {
            Clazz.TABLE_ID -> {
                scopeName = theActivity.getText(R.string.clazz).toString()
                assigneeName = entity.clazzName
            }
            Location.TABLE_ID -> {
                scopeName = theActivity.getText(R.string.location).toString()
                assigneeName = entity.locationName
            }
            Person.TABLE_ID -> {
                scopeName = theActivity.getText(R.string.person).toString()
                assigneeName = entity.personName
            }
            else -> {
            }
        }

        var descText = ""
        if (scopeName != null && assigneeName != null) {
            descText = (theActivity.getText(R.string.in_literal).toString()
                    + " " + scopeName + ": " + assigneeName)
        }

        title.setText(titleText)
        desc.setText(descText)

        if(mPresenter != null) {
            //Options to Edit/Delete every schedule in the list
            menu.setOnClickListener { v: View ->
                //creating a popup menu
                val popup = PopupMenu(theActivity.applicationContext, v)

                popup.setOnMenuItemClickListener { item ->
                    val i = item.itemId
                    if (i == R.id.edit) {
                        mPresenter!!.handleEditRoleAssignment(entity.erUid)
                        true
                    } else if (i == R.id.delete) {
                        mPresenter!!.handleDeleteRoleAssignment(entity.erUid)
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
        }else{
            menu.visibility = View.GONE
        }

    }

    class RoleAssignmentListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
