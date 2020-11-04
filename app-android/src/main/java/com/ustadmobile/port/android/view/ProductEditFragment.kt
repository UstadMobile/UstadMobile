package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentProductEditBinding
import com.ustadmobile.core.controller.ProductEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ProductEditView
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.port.android.util.ext.*

import com.ustadmobile.port.android.view.ext.setEditFragmentTitle

interface ProductEditFragmentEventHandler {

}

class ProductEditFragment: UstadEditFragment<Product>(), ProductEditView, ProductEditFragmentEventHandler {

    private var mBinding: FragmentProductEditBinding? = null

    private var mPresenter: ProductEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Product>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentProductEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = ProductEditPresenter(requireContext(), arguments.toStringMap(), this,
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
        setEditFragmentTitle(R.string.product)
    }

    override var entity: Product? = null
        get() = field
        set(value) {
            field = value
            mBinding?.product = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }
}