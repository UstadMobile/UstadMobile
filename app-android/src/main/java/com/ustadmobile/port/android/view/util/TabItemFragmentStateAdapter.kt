package com.ustadmobile.port.android.view.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ustadmobile.core.impl.appstate.TabItem
import com.ustadmobile.core.util.ext.toBundle

/**
 * FragmentStateAdapter that will accept a list of TabItems  to implement a tabbed screen on Android.
 * Each TabItem specifies the VIEW_NAME, which is mapped to a Fragment class. The TabItem also
 * specifies the arguments that will be provided (the same VIEW_NAME can be used on more than one
 * tab with different arguments)
 *
 *  See https://proandroiddev.com/viewpager2-and-diffutil-d853cdab5f4a
 *
 *  @param viewNameToFragmentClassMap Map of VIEW_NAME to the fragmnet class that implements the given view
 */
class TabItemFragmentStateAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    private val viewNameToFragmentClassMap: Map<String, Class<out Fragment>>,
) : FragmentStateAdapter(fm, lifecycle){

    class TabItemDiffUtil(
        private val oldList: List<TabItem>,
        private val newList: List<TabItem>,
    ): DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].viewName == newList[newItemPosition].viewName &&
                oldList[oldItemPosition].args == newList[newItemPosition].args
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return true
        }
    }

    private val items: MutableList<TabItem> = arrayListOf()

    override fun getItemCount(): Int {
        return items.size
    }

    override fun createFragment(position: Int): Fragment {
        val viewName = items[position].viewName
        return viewNameToFragmentClassMap[viewName]?.newInstance()?.also {
            it.arguments = items[position].args.toBundle()
        } ?: throw IllegalArgumentException("ViewNameToClassMap: No Fragment class found for $viewName")
    }

    fun setItems(tabItems: List<TabItem>) {
        val callback = TabItemDiffUtil(items, tabItems)
        val diff = DiffUtil.calculateDiff(callback)
        items.clear()
        items.addAll(tabItems)
        diff.dispatchUpdatesTo(this)
    }

}