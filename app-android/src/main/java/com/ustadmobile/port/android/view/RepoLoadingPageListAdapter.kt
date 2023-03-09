package com.ustadmobile.port.android.view

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * Note: It might be worth replacing this listener with using a LiveData object for this instead
 */
interface FistItemLoadedListener{

    fun onFirstItemLoaded()
}

abstract class RepoLoadingPageListAdapter<T : Any, VH : RecyclerView.ViewHolder>(itemCallback: DiffUtil.ItemCallback<T>): PagedListAdapter<T, VH>(itemCallback){

    var firstItemLoaded: Boolean = false

    //This should probably be a livedata object
    var firstItemLoadedListener: FistItemLoadedListener? = null

    var isTopEntryList: Boolean = true

    override fun onBindViewHolder(holder: VH, position: Int) {
        if(!firstItemLoaded){
            firstItemLoaded = true
            firstItemLoadedListener?.onFirstItemLoaded()
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        firstItemLoadedListener = null
    }
}