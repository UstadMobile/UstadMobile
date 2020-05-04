package com.ustadmobile.port.android.umeditor


import androidx.recyclerview.widget.RecyclerView

/**
 * Interface which listen for the drag event on recycler view holder
 *
 * @author kileha3
 */
interface UmOnStartDragListener {

    /**
     * invoked when a view is requesting a drag start.
     *
     * @param viewHolder View holder to grad
     */
    fun onDragStarted(viewHolder: RecyclerView.ViewHolder)
}
