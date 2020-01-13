package com.ustadmobile.port.android.view

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADED_NODATA

interface FistItemLoadedListener{

    fun onFirstItemLoaded()

    fun onEmptyTopEntryList(status: Int)
}

abstract class RepoLoadingPageListAdapter<T, VH : RecyclerView.ViewHolder>(itemCallback: DiffUtil.ItemCallback<T>): PagedListAdapter<T, VH>(itemCallback){

    var firstItemLoaded: Boolean = false

    var fistItemLoadedListener: FistItemLoadedListener? = null


    var isTopEntryList: Boolean = true

    override fun onBindViewHolder(holder: VH, position: Int) {
        if(!firstItemLoaded && position > 1){
            firstItemLoaded = true
            fistItemLoadedListener?.onFirstItemLoaded()
        }
    }
}