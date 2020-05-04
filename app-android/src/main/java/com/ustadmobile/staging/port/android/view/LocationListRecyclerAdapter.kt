package com.ustadmobile.staging.port.android.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.PopupMenu
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.LocationListPresenter
import com.ustadmobile.lib.db.entities.LocationWithSubLocationCount

class LocationListRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<LocationWithSubLocationCount>,
        internal var mPresenter: LocationListPresenter,
        internal var theActivity: Activity,
        internal var theContext: Context)
    : PagedListAdapter<LocationWithSubLocationCount, LocationListRecyclerAdapter.LocationListViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationListViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false)
        return LocationListViewHolder(list)

    }

    override fun onBindViewHolder(holder: LocationListViewHolder, position: Int) {

        val entity = getItem(position)

        val title = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_title)
        val desc = holder.itemView.findViewById<TextView>(R.id.item_title_with_desc_and_dots_desc)
        val menu = holder.itemView.findViewById<AppCompatImageView>(R.id
                .item_title_with_desc_and_dots_dots)

        title.setText(entity!!.title)
        val subLocationCount = entity.subLocations
        var entitiesString = theActivity.getText(R.string.sub_locations).toString()
        if (subLocationCount == 1) {
            entitiesString = theActivity.getText(R.string.sub_location).toString()
        }

        desc.setText(subLocationCount.toString() + " " + entitiesString)

        holder.itemView.setOnClickListener({ v -> mPresenter.handleClickEditLocation(entity.locationUid) })
        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener{ v: View ->
            //creating a popup menu
            val popup = PopupMenu(theActivity, v)

            popup.setOnMenuItemClickListener{ item ->
                val i = item.getItemId()
                if (i == R.id.edit) {
                    mPresenter.handleClickEditLocation(entity.locationUid)
                    true
                } else if (i == R.id.delete) {
                    mPresenter.handleDeleteLocation(entity.locationUid)
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

    inner class LocationListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
