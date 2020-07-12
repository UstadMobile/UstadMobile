package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentHolidayEditBinding
import com.ustadmobile.core.controller.HolidayEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.HolidayEditView
import com.ustadmobile.lib.db.entities.Holiday

class HolidayEditFragment(): UstadEditFragment<Holiday>(), HolidayEditView {

    private var mBinding: FragmentHolidayEditBinding? = null

    private var mPresenter: HolidayEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Holiday>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentHolidayEditBinding.inflate(inflater, container,false).also {
            rootView = it.root
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setEditFragmentTitle(R.string.holiday)

        mPresenter = HolidayEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
    }

    override fun onResume() {
        super.onResume()
    }

    override var entity: Holiday? = null
        get() = field
        set(value) {
            field = value
            mBinding?.holiday = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    override var loading: Boolean = false

}