package com.ustadmobile.port.android.view

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

interface FistItemLoadedListener{

    fun onFirstItemLoaded()
}

abstract class RepoLoadingPageListAdapter<T, VH : RecyclerView.ViewHolder>(itemCallback: DiffUtil.ItemCallback<T>): PagedListAdapter<T, VH>(itemCallback){

    private var firstItemLoaded: Boolean = false

    var fistItemLoadedListener: FistItemLoadedListener? = null

    override fun onBindViewHolder(holder: VH, position: Int) {
        if(!firstItemLoaded){
            firstItemLoaded = true
            fistItemLoadedListener?.onFirstItemLoaded()
        }
    }
}