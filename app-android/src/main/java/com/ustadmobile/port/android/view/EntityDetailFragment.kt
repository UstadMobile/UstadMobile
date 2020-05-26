package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentEntityDetailBinding
import com.ustadmobile.core.controller.EntityDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.EntityDetailView
import com.ustadmobile.lib.db.entities.Entity
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.lib.db.entities.ClazzWorkWithSubmission

interface EntityDetailFragmentEventHandler {

}

class EntityDetailFragment: UstadDetailFragment<ClazzWorkWithSubmission>(), EntityDetailView, EntityDetailFragmentEventHandler {

    private var mBinding: FragmentEntityDetailBinding? = null

    private var mPresenter: EntityDetailPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentEntityDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = EntityDetailPresenter(requireContext(), arguments.toStringMap(), this,
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

    override var entity: ClazzWorkWithSubmission? = null
        get() = field
        set(value) {
            field = value
            mBinding?.entity = value
        }

    override var editButtonMode: EditButtonMode = EditButtonMode.GONE
        get() = field
        set(value) {
            mBinding?.editButtonMode = value
            field = value
        }

}