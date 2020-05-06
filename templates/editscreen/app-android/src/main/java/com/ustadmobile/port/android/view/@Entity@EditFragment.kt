package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.Fragment@Entity_ViewBinding_VariableName@EditBinding
import com.ustadmobile.core.controller.@Entity@EditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.@Entity@EditView
import com.ustadmobile.lib.db.entities.@Entity@
@EditEntity_Import@
import com.ustadmobile.port.android.view.ext.setEditFragmentTitle

interface @Entity@EditFragmentEventHandler {

}

class @Entity@EditFragment: UstadEditFragment<@Entity@>(), @Entity@EditView, @Entity@EditFragmentEventHandler {

    private var rootView: Fragment@Entity_ViewBinding_VariableName@EditBinding? = null

    private var mPresenter: @Entity@EditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, @Entity@>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = Fragment@Entity_ViewBinding_VariableName@EditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = @Entity@EditPresenter(requireContext(), arguments.toStringMap(), this,
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
        setEditFragmentTitle(R.string.@Entity_LowerCase@)
    }

    override var entity: @EditEntity@? = null
        get() = field
        set(value) {
            field = value
            mBinding?.schedule = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }
}