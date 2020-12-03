package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSalePaymentEditBinding
import com.ustadmobile.core.controller.SalePaymentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SalePaymentEditView
import com.ustadmobile.lib.db.entities.SalePayment
import com.ustadmobile.lib.db.entities.SalePaymentWithSaleItems
import com.ustadmobile.port.android.util.ext.*


interface SalePaymentEditFragmentEventHandler {

}

class SalePaymentEditFragment: UstadEditFragment<SalePaymentWithSaleItems>(), SalePaymentEditView,
        SalePaymentEditFragmentEventHandler {

    private var mBinding: FragmentSalePaymentEditBinding? = null

    private var mPresenter: SalePaymentEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, SalePaymentWithSaleItems>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSalePaymentEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = SalePaymentEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.new_payment, R.string.edit_payment)
    }

    override var entity: SalePaymentWithSaleItems? = null
        get() = field
        set(value) {
            field = value
            mBinding?.salePayment = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }
}