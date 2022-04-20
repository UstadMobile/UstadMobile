package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.port.android.util.ext.saveStateToCurrentBackStackStateHandle

abstract class UstadEditFragment<T: Any>: UstadBaseFragment(), UstadEditView<T> {

    abstract protected val mEditPresenter : UstadEditPresenter<*, T>?

    override var fieldsEnabled: Boolean = true
        set(value) {
            if(field != value) {
                field = value
                activity?.invalidateOptionsMenu()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.visible = false
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_done, menu)

        val menuItem = menu.findItem(R.id.menu_done)

        menuItem.title = if(mEditPresenter?.persistenceMode == UstadSingleEntityPresenter.PersistenceMode.DB) {
            requireContext().getString(R.string.save)
        }else {
            requireContext().getString(R.string.done)
        }

        menuItem.isEnabled = fieldsEnabled
    }

    protected fun setEditFragmentTitle(newTitleId: Int, editStringId: Int) {
        val entityUid = arguments?.getString(ARG_ENTITY_UID)?.toLong() ?: 0L
        val entityJsonStr = arguments?.getString(UstadEditView.ARG_ENTITY_JSON)
        ustadFragmentTitle = if(entityUid != 0L || entityJsonStr != null){
            getString(editStringId)
        }else {
            getString(newTitleId)
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

    @Deprecated("This should not be used. Saving to the state handle should be done in " +
        "multiplatform code by the presenter.")
    protected open fun onSaveStateToBackStackStateHandle() = mEditPresenter?.saveStateToCurrentBackStackStateHandle(findNavController())

}