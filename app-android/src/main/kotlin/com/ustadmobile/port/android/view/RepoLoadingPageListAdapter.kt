package com.ustadmobile.port.android.view

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

interface FistItemLoadedListener{

    fun onFirstItemLoaded()
}

abstract class RepoLoadingPageListAdapter<T, VH : RecyclerView.ViewHolder>(itemCallback: DiffUtil.ItemCallback<T>): PagedListAdapter<T, VH>(itemCallback){

    var firstItemLoaded: Boolean = false

    var firstItemLoadedListener: FistItemLoadedListener? = null


    var isTopEntryList: Boolean = true

    override fun onBindViewHolder(holder: VH, position: Int) {
        if(!firstItemLoaded){
            firstItemLoaded = true
            firstItemLoadedListener?.onFirstItemLoaded()
        }
    }
}