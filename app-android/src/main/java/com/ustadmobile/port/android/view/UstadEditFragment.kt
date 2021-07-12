package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.port.android.util.ext.saveStateToCurrentBackStackStateHandle
import com.ustadmobile.port.android.view.ext.saveResultToBackStackSavedStateHandle
import com.ustadmobile.port.android.view.util.PresenterViewLifecycleObserver

abstract class UstadEditFragment<T: Any>: UstadBaseFragment(), UstadEditView<T> {

    abstract protected val mEditPresenter : UstadEditPresenter<*, T>?

    private var presenterLifecycleObserver: PresenterViewLifecycleObserver? = null

    override var fieldsEnabled: Boolean = true
        set(value) {
            if(field != value) {
                field = value
                activity?.invalidateOptionsMenu()
            }
        }

    override fun finishWithResult(result: List<T>) {
        saveResultToBackStackSavedStateHandle(result)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        presenterLifecycleObserver = PresenterViewLifecycleObserver(mEditPresenter).also {
            viewLifecycleOwner.lifecycle.addObserver(it)
        }
        return view
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

    protected open fun onSaveStateToBackStackStateHandle() = mEditPresenter?.saveStateToCurrentBackStackStateHandle(findNavController())

    override fun onDestroyView() {
        presenterLifecycleObserver?.also {
            viewLifecycleOwner.lifecycle.removeObserver(it)
        }
        presenterLifecycleObserver = null
        super.onDestroyView()
    }
}