package com.ustadmobile.port.android.view.util

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ListAdapter

class ListSubmitObserver<T>(val listAdapter: ListAdapter<T, *>) : Observer<List<T>> {
    override fun onChanged(value: List<T>) {
        listAdapter.submitList(value)
    }
}