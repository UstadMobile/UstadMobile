package com.ustadmobile.port.android.view.util

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ListAdapter

class ListSubmitObserver<T>(val listAdapter: ListAdapter<T, *>) : Observer<List<T>> {
    override fun onChanged(t: List<T>?) {
        listAdapter.submitList(t)
    }
}