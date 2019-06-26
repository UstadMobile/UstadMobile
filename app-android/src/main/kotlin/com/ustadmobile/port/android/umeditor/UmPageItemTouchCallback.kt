package com.ustadmobile.port.android.umeditor

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


/**
 * Implementation of [ItemTouchHelper.Callback] which enables basic drag & drop.
 * Drag events are automatically started by an item long-press.
 *
 * @author kileha3
 */

class UmPageItemTouchCallback(private val touchAdapter: UmPageItemTouchAdapter) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        // Set movement flags based on the layout manager
        return if (recyclerView.layoutManager is LinearLayoutManager) {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            val swipeFlags = 0
            ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
        } else {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
        }
    }

    override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        if (source.itemViewType != target.itemViewType) {
            return false
        }

        touchAdapter.onPageItemMove(source.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {

    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is UmPageItemTouchViewHolder) {
                val itemViewHolder = viewHolder as UmPageItemTouchViewHolder?
                itemViewHolder!!.onPageItemSelected()
            }
        }

        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        viewHolder.itemView.alpha = ALPHA_FULL

        if (viewHolder is UmPageItemTouchViewHolder) {
            val itemViewHolder = viewHolder as UmPageItemTouchViewHolder
            itemViewHolder.onPageItemClear()
        }
    }

    companion object {
        private const val ALPHA_FULL = 1.0f
    }
}
