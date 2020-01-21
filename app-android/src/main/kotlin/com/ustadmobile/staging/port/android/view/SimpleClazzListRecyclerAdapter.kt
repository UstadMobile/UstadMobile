package com.ustadmobile.staging.port.android.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents

/**
 * Simple Clazz List recycler adapter -  simple mini recycler adapter just to show a list of classes
 * assigned to this person in the person detail .
 */
internal class SimpleClazzListRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<ClazzWithNumStudents>, var theContext: Context)
    : PagedListAdapter<ClazzWithNumStudents,
        SimpleClazzListRecyclerAdapter.ClazzLogDetailViewHolder>(diffCallback) {

    class ClazzLogDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzLogDetailViewHolder {

        val clazzLogDetailListItem = LayoutInflater.from(theContext).inflate(
                R.layout.item_clazzlist_clazz_simple, parent, false)
        return ClazzLogDetailViewHolder(clazzLogDetailListItem)
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     *
     * Every item in the recycler view will have set its colors if no attendance status is set.
     * every attendance button will have it-self mapped to tints on activation.
     *
     * @param holder    View holder
     * @param position  The position
     */
    override fun onBindViewHolder(holder: ClazzLogDetailViewHolder, position: Int) {
        val thisClazz = getItem(position)!!

        (holder.itemView.findViewById<View>(R.id.item_clazzlist_clazz_simple_clazz_name) as TextView).text = thisClazz.clazzName

    }
}