package com.ustadmobile.port.android.view.util

import android.view.View
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.door.DoorMutableLiveData

/**
 * This PagedListAdapter helps manage selections.
 */
abstract class SelectablePagedListAdapter<T, VH: RecyclerView.ViewHolder>(diffcallback: DiffUtil.ItemCallback<T>)
    : PagedListAdapter<T, VH>(diffcallback), PagedItemSelectionListener<T>, SelectableViewHelper{

    protected val selectedItems = mutableListOf<T>()

    val selectedItemsLiveData: DoorMutableLiveData<List<T>> = DoorMutableLiveData(listOf())

    override fun onItemSelectedChanged(view: View, item: T) {
        if(view.isSelected) {
            selectedItems += item
        }else {
            selectedItems -= item
        }

        selectedItemsLiveData.sendValue(selectedItems.toList())
    }

    fun clearSelection() {
        selectedItems.clear()
        selectedItemsLiveData.sendValue(selectedItems.toList())
    }

    override val isInSelectionMode: Boolean
        get() = selectedItems.isNotEmpty()


    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        selectedItems.clear()
    }


}