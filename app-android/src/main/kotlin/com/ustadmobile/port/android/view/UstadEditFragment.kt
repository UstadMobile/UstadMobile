package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.port.android.view.ext.saveResultToBackStackSavedStateHandle

abstract class UstadEditFragment<T>: UstadBaseFragment(), UstadEditView<T> {

    override val viewContext: Any
        get() = requireContext()

    //TODO: this should really add the done checkbox to the menu as standard, and handle when
    // passing that event to the presenter

    override fun finishWithResult(result: List<T>) {
        saveResultToBackStackSavedStateHandle(result)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_done, menu)
    }

}