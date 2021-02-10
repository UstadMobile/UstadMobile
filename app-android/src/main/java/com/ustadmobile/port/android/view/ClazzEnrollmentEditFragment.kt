package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.databinding.FragmentClazzEnrollmentBinding
import com.ustadmobile.core.controller.ClazzEnrollmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzEnrollmentEditView
import com.ustadmobile.lib.db.entities.ClazzEnrollment
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList


interface ClazzEnrollmentFragmentEventHandler {
    fun handleReasonLeavingClicked()
}

class ClazzEnrollmentEditFragment: UstadEditFragment<ClazzEnrollment>(), ClazzEnrollmentEditView, ClazzEnrollmentFragmentEventHandler {

    private var mBinding: FragmentClazzEnrollmentBinding? = null

    private var mPresenter: ClazzEnrollmentEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzEnrollment>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzEnrollmentBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = ClazzEnrollmentEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                LeavingReason::class.java) {
            val reason = it.firstOrNull() ?: return@observeResult
            // TODO updateString
        }
    }

    override fun handleReasonLeavingClicked() {
        navigateToPickEntityFromList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: ClazzEnrollment? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzEnrollment = value
        }

    override var roleList: List<ClazzEnrollmentEditPresenter.RoleMessageIdOption>? = null
        get() = field
        set(value) {
            field = value
        }
    override var statusList: List<ClazzEnrollmentEditPresenter.StatusMessageIdOption>? = null
        get() = field
        set(value) {
            field = value
        }


    override var startDateError: String? = null
        get() = field
        set(value) {
            field = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }
}