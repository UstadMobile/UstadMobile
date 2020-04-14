package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.room.Embedded
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.lib.db.entities.SelQuestion
import com.ustadmobile.lib.db.entities.SelQuestionOption
import com.ustadmobile.port.android.util.ext.saveStateToCurrentBackStackStateHandle
import com.ustadmobile.port.android.view.ext.saveResultToBackStackSavedStateHandle
import kotlinx.serialization.Serializable

abstract class UstadEditFragment<T>: UstadBaseFragment(), UstadEditView<T> {

    abstract protected val mEditPresenter : UstadEditPresenter<*, T>?

    override val viewContext: Any
        get() = requireContext()

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_done -> {
                val entityVal = entity ?: return false
                mEditPresenter?.handleClickSave(entityVal)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply { mEditPresenter?.onSaveInstanceState(this) }.toBundle())
    }

    protected fun onSaveStateToBackStackStateHandle() = mEditPresenter?.saveStateToCurrentBackStackStateHandle(findNavController())

}