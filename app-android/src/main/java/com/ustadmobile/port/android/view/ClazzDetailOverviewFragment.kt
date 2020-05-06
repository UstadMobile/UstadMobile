package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzDetailBinding
import com.toughra.ustadmobile.databinding.FragmentClazzDetailOverviewBinding
import com.ustadmobile.core.controller.ClazzDetailOverviewPresenter
import com.ustadmobile.core.controller.ClazzDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails

interface ClazzDetailOverviewEventHandler {

}

class ClazzDetailOverviewFragment: UstadDetailFragment<ClazzWithDisplayDetails>(), ClazzDetailOverviewView, ClazzDetailFragmentEventHandler {

    private var mBinding: FragmentClazzDetailOverviewBinding? = null

    private var mPresenter: ClazzDetailOverviewPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzDetailOverviewBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = ClazzDetailOverviewPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

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

        //TODO: Set title here
    }

    override var entity: ClazzWithDisplayDetails? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazz = value
        }


}