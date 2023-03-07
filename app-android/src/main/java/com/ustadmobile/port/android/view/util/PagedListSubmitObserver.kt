package com.ustadmobile.port.android.view.util

import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter

class PagedListSubmitObserver<T: Any> (val pagedListAdapter: PagedListAdapter<T, *>) : Observer<PagedList<T>> {

    override fun onChanged(t: PagedList<T>?) {
        pagedListAdapter.submitList(t)
    }
}