package com.ustadmobile.port.android.view

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.core.view.UstadDetailView

abstract class UstadDetailFragment<T>: UstadBaseFragment(), UstadDetailView<T> {

    override val viewContext: Any
        get() = requireContext()

    override var loading: Boolean = false
        get() = false
        set(value) {
            //TODO: set this on the main activity
            field = value
        }

    abstract val detailPresenter: UstadDetailPresenter<*, *>?

    override var editButtonMode: EditButtonMode = EditButtonMode.GONE
        get() = field
        set(value) {
            field = value
            fabManager?.visible = (value == EditButtonMode.FAB)
        }

    protected open var mActivityWithFab: UstadListViewActivityWithFab? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivityWithFab = (context as? UstadListViewActivityWithFab)
    }

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        (activity as? MainActivity)?.showSnackBar(message, action, actionMessageId)
    }

    override fun onDetach() {
        super.onDetach()
        mActivityWithFab = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getString(R.string.edit)
        fabManager?.icon = R.drawable.ic_edit_white_24dp
        fabManager?.onClickListener = {
            detailPresenter?.handleClickEdit()
        }
    }
}