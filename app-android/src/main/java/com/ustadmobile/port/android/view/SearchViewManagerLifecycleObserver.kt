package com.ustadmobile.port.android.view

import android.os.Handler
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ustadmobile.core.controller.OnSearchSubmitted

class SearchViewManagerLifecycleObserver(searchView: SearchView?) : DefaultLifecycleObserver, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private var active: Boolean = false

    var searchListener: OnSearchSubmitted? = null

    var searchView: SearchView? = searchView
        set(value) {
            field = value
            if (active && value != null) {
                value.setOnQueryTextListener(this)
                value.setOnCloseListener(this)
            }
        }

    private var query: String? = null

    private var inputCheckHandler: Handler? = Handler()

    private val inputCheckDelay: Long = 500

    private val inputCheckerCallback = Runnable {
        val typedText = query ?: ""
        searchListener?.onSearchSubmitted(typedText)
    }

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

    private fun postText(query: String?) {
        this.query = query
        inputCheckHandler?.removeCallbacks(inputCheckerCallback)
        inputCheckHandler?.postDelayed(inputCheckerCallback, inputCheckDelay)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        postText(query)
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        postText(newText)
        return false
    }

    override fun onClose(): Boolean {
        postText("")
        return false
    }


    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        searchView = null
        searchListener = null
        inputCheckHandler = null
    }


}