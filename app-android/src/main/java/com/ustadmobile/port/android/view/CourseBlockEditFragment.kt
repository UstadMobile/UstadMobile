package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentCourseBlockEditBinding
import com.ustadmobile.core.controller.CourseBlockEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseBlockEditView
import com.ustadmobile.lib.db.entities.CourseBlock

class CourseBlockEditFragment: UstadEditFragment<CourseBlock>(), CourseBlockEditView {

    private var mBinding: FragmentCourseBlockEditBinding? = null

    private var mPresenter: CourseBlockEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseBlock>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentCourseBlockEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = CourseBlockEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.add_module, R.string.edit_module)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var loading: Boolean = false

    override var entity: CourseBlock? = null
        get() = field
        set(value) {
            field = value
            mBinding?.block = value
        }
    override var blockTitleError: String? = null
        set(value) {
            field = value
            mBinding?.blockTitleError = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }

    override var startDate: Long
        get() = mBinding?.startDate ?: 0
        set(value) {
            mBinding?.startDate = value
        }

    override var startTime: Long
        get() = mBinding?.startTime ?: 0
        set(value) {
            mBinding?.startTime = value
        }

    override var timeZone: String? = null
        set(value) {
            mBinding?.timeZone = value
            field = value
        }

}