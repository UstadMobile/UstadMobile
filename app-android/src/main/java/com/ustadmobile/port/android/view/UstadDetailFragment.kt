package com.ustadmobile.port.android.view

import android.content.Context
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
            mActivityWithFab?.activityFloatingActionButton?.visibility = if(value == EditButtonMode.FAB) {
                mActivityWithFab?.activityFloatingActionButton?.text = requireContext().getText(R.string.edit)
                View.VISIBLE
            }else {
                View.GONE
            }
        }

    protected var mActivityWithFab: UstadListViewActivityWithFab? = null
        get() {
            /*
             The getter will return null so that if the current fragment is not actually visible
             no changes will be sent through
             */
            return if(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                field
            }else {
                null
            }
        }

        set(value) {
            field = value
        }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivityWithFab = (context as? UstadListViewActivityWithFab)
    }

    override fun onDetach() {
        super.onDetach()
        mActivityWithFab = null
    }

    override fun onResume() {
        super.onResume()

        val theFab = mActivityWithFab?.activityFloatingActionButton
        theFab?.text = requireContext().getText(R.string.edit)
        theFab?.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_edit_white_24dp)

        theFab?.setOnClickListener {
            detailPresenter?.handleClickEdit()
        }
    }
}