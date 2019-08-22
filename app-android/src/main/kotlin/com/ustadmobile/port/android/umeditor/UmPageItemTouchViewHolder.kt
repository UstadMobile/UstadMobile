package com.ustadmobile.port.android.umeditor

/**
 * Interface to notify an item ViewHolder callbacks from [ ].
 *
 * @author kileha3
 */

interface UmPageItemTouchViewHolder {
    /**
     * Called when the [androidx.recyclerview.widget.ItemTouchHelper]
     * first registers an item as being dragged.
     */
    fun onPageItemSelected()


    /**
     * Called when the [androidx.recyclerview.widget.ItemTouchHelper]
     * has completed move, and the active item state should be cleared.
     */
    fun onPageItemClear()
}