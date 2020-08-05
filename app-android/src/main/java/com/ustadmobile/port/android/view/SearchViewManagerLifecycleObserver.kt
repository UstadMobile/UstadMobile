package com.ustadmobile.port.android.view

import androidx.appcompat.widget.SearchView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ustadmobile.core.controller.OnSearchSubmitted

class SearchViewManagerLifecycleObserver(private var searchView: SearchView?) : DefaultLifecycleObserver, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private var active: Boolean = false

    var searchListener: OnSearchSubmitted? = null

    override fun onResume(owner: LifecycleOwner) {
        searchView?.setOnQueryTextListener(this)
        searchView?.setOnCloseListener(this)
        active = true
    }

    override fun onPause(owner: LifecycleOwner) {
        searchView?.setOnQueryTextListener(null)
        searchView?.setOnCloseListener(null)
        active = false
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        searchListener?.onSearchSubmitted(query)
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        searchListener?.onSearchSubmitted(newText)
        return false
    }

    override fun onClose(): Boolean {
        searchListener?.onSearchSubmitted("")
        return false
    }


    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        searchView = null
        searchListener = null
    }



}