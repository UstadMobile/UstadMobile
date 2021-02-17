package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzEnrolmentBinding
import com.ustadmobile.core.controller.ClazzEnrolmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzEnrolmentEditView
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList


interface ClazzEnrolmentFragmentEventHandler {
    fun handleReasonLeavingClicked()
}

class ClazzEnrolmentEditFragment: UstadEditFragment<ClazzEnrolmentWithLeavingReason>(), ClazzEnrolmentEditView, ClazzEnrolmentFragmentEventHandler {

    private var mBinding: FragmentClazzEnrolmentBinding? = null

    private var mPresenter: ClazzEnrolmentEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzEnrolmentWithLeavingReason>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzEnrolmentBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = ClazzEnrolmentEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.new_enrolment, R.string.edit_enrolment)
        val navController = findNavController()

        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                LeavingReason::class.java) {
            val reason = it.firstOrNull() ?: return@observeResult
            val savedEntity = entity ?: return@observeResult
            savedEntity.leavingReason = reason
            entity = savedEntity
        }
    }

    override fun handleReasonLeavingClicked() {
        navigateToPickEntityFromList(LeavingReason::class.java, R.id.leaving_reason_list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: ClazzEnrolmentWithLeavingReason? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzEnrolment = value
        }

    override var roleList: List<IdOption>? = null
        get() = field
        set(value) {
            field = value
            mBinding?.roleOptions = value
        }
    override var statusList: List<IdOption>? = null
        get() = field
        set(value) {
            field = value
            mBinding?.statusOptions = value
        }

    override var startDateError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.startDateError = value
        }

    override var endDateError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.endD = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }
}