package com.ustadmobile.port.android.view.ext

import android.view.View
import androidx.recyclerview.widget.DiffUtil

/**
 * This is a convenience extension function to help manage selectable items that are in a
 * recyclerview.
 *
 * It will use the DiffUtil.Itemcallback so it can quickly determine if a given item is selected (e.g.
 * the ItemCallback areItemsTheSame will consider only the primary key, not the rest of the object)
 *
 * @param item The item to look for
 * @param selectedItemsList The list of those items that are selected
 * @param differ The ItemCallback used with the recyclerview
 */
fun <T> View.setSelectedIfInList(item: T?, selectedItemsList: List<T>, differ: DiffUtil.ItemCallback<T>) {
    //All lists in use are converted to jetpack compose.
    //isSelected = item != null && selectedItemsList.any { differ.areItemsTheSame(it, item) }
}