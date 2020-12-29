package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentWorkSpaceEditBinding
import com.ustadmobile.core.controller.WorkSpaceEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.WorkSpaceEditView
import com.ustadmobile.lib.db.entities.WorkSpace
import com.ustadmobile.port.android.util.ext.*

interface WorkSpaceEditFragmentEventHandler {

}

class WorkSpaceEditFragment: UstadEditFragment<WorkSpace>(), WorkSpaceEditView, WorkSpaceEditFragmentEventHandler {

    private var mBinding: FragmentWorkSpaceEditBinding? = null

    private var mPresenter: WorkSpaceEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, WorkSpace>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentWorkSpaceEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = WorkSpaceEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di)
        mPresenter?.onCreate(backStackSavedState)

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()
    }

    override var entity: WorkSpace? = null
        get() = field
        set(value) {
            field = value
            mBinding?.workSpace = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }
}