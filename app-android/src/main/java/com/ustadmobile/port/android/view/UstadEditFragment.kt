package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.port.android.util.ext.saveStateToCurrentBackStackStateHandle
import com.ustadmobile.port.android.view.ext.saveResultToBackStackSavedStateHandle

abstract class UstadEditFragment<T: Any>: UstadBaseFragment(), UstadEditView<T> {

    abstract protected val mEditPresenter : UstadEditPresenter<*, T>?

    override val viewContext: Any
        get() = requireContext()

    override fun finishWithResult(result: List<T>) {
        saveResultToBackStackSavedStateHandle(result)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.visible = false
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_done, menu)
    }

    /**
     * Set the title of an edit fragment to "New Widget" or "Edit Widget" based on whether or not a new
     * entity is being created.
     *
     * @param entityTitleId the string id for the entity type (e.g. Widget)
     */
    protected fun setEditFragmentTitle(entityTitleId: Int)  {
        val entityUid = arguments?.getString(ARG_ENTITY_UID)?.toLong() ?: 0L
        val entityJsonStr = arguments?.getString(UstadEditView.ARG_ENTITY_JSON)
        ustadFragmentTitle = if(entityUid != 0L || entityJsonStr != null){
            getString(R.string.edit_entity, getString(entityTitleId))
        }else {
            getString(R.string.new_entity, getString(entityTitleId))
        }

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

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        (activity as? MainActivity)?.showSnackBar(message, action, actionMessageId)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply { mEditPresenter?.onSaveInstanceState(this) }.toBundle())
    }

    protected fun onSaveStateToBackStackStateHandle() = mEditPresenter?.saveStateToCurrentBackStackStateHandle(findNavController())

}