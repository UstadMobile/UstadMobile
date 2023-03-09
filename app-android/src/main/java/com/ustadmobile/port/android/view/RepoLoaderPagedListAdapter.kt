package com.ustadmobile.port.android.view

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * This basic extension of PagedListAdapter has a simple callback to tell listeners (e.g. components
 * that tell the user about loading status) when the first view has been bound.
 */
abstract class RepoLoaderPagedListAdapter<T: Any, VH : RecyclerView.ViewHolder>: PagedListAdapter<T, VH> {

    interface OnFirstViewBoundListener {

        fun onFirstViewBound()

    }

    private var firstItemLoaded = false

    private var onFirstViewBoundListener: OnFirstViewBoundListener? = null

    constructor(diffCallback: DiffUtil.ItemCallback<T>) : super(diffCallback)

    constructor(config: AsyncDifferConfig<T>) : super(config)

    override fun onBindViewHolder(holder: VH, position: Int) {
        if(!firstItemLoaded) {
            firstItemLoaded = true
            onFirstViewBoundListener?.onFirstViewBound()
        }
    }
}
