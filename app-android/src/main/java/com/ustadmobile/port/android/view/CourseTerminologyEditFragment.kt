package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.databinding.FragmentCourseTerminologyEditBinding
import com.ustadmobile.core.controller.CourseTerminologyEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseTerminologyEditView
import com.ustadmobile.lib.db.entities.CourseTerminologyWithLabel


class CourseTerminologyEditFragment: UstadEditFragment<CourseTerminologyWithLabel>(), CourseTerminologyEditView {

    private var mBinding: FragmentCourseTerminologyEditBinding? = null

    private var mPresenter: CourseTerminologyEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseTerminologyWithLabel>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentCourseTerminologyEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = CourseTerminologyEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: CourseTerminologyWithLabel? = null
        get() = field
        set(value) {
            field = value
            mBinding?.courseTerminology = value
        }
    override var titleErrorText: String? = null
        set(value) {
            field = value
            mBinding?.titleErrorText = value
        }
    override var teacherErrorText: String? = null
        set(value) {
            field = value
            mBinding?.teacherErrorText = value
        }
    override var studentErrorText: String? = null
        set(value) {
            field = value
            mBinding?.studentErrorText = value
        }
    override var addTeacherErrorText: String? = null
        set(value) {
            field = value
            mBinding?.addTeacherErrorText = value
        }
    override var addStudentErrorText: String? = null
        set(value) {
            field = value
            mBinding?.addStudentErrorText = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }
}