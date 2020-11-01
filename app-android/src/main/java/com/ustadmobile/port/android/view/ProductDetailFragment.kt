package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentProductDetailBinding
import com.ustadmobile.core.controller.ProductDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ProductDetailView
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount

interface ProductDetailFragmentEventHandler {

}

class ProductDetailFragment: UstadDetailFragment<ProductWithInventoryCount>(), ProductDetailView, ProductDetailFragmentEventHandler {

    private var mBinding: FragmentProductDetailBinding? = null

    private var mPresenter: ProductDetailPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentProductDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = ProductDetailPresenter(requireContext(), arguments.toStringMap(), this,
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

    override var entity: ProductWithInventoryCount? = null
        get() = field
        set(value) {
            field = value
            mBinding?.product = value
        }

    override var editButtonMode: EditButtonMode = EditButtonMode.GONE
        get() = field
        set(value) {
            mBinding?.editButtonMode = value
            field = value
        }

}