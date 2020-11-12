package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSaleEditBinding
import com.ustadmobile.core.controller.SaleEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SaleEditView
import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.port.android.util.ext.*

import com.ustadmobile.port.android.view.ext.setEditFragmentTitle

interface SaleEditFragmentEventHandler {

}

class SaleEditFragment: UstadEditFragment<Sale>(), SaleEditView, SaleEditFragmentEventHandler {

    private var mBinding: FragmentSaleEditBinding? = null

    private var mPresenter: SaleEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Sale>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSaleEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = SaleEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

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
        setEditFragmentTitle(R.string.sale)
    }

    override var entity: Sale? = null
        get() = field
        set(value) {
            field = value
            mBinding?.sale = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }
}