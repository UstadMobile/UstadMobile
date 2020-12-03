package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSaleItemEditBinding
import com.ustadmobile.core.controller.SaleItemEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SaleItemEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.SaleItem
import com.ustadmobile.lib.db.entities.SaleItemWithProduct
import com.ustadmobile.port.android.util.ext.*
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import kotlinx.android.synthetic.main.fragment_sale_edit.*


interface SaleItemEditFragmentEventHandler {

}

class SaleItemEditFragment: UstadEditFragment<SaleItemWithProduct>(), SaleItemEditView,
        SaleItemEditFragmentEventHandler {

    private var mBinding: FragmentSaleItemEditBinding? = null

    private var mPresenter: SaleItemEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, SaleItemWithProduct>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSaleItemEditBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
            it.activityEventHandler = this
        }

        mPresenter = SaleItemEditPresenter(requireContext(), arguments.toStringMap(),
                this, di, viewLifecycleOwner)

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
        setEditFragmentTitle(R.string.add_item, R.string.edit_item)
    }

    override var entity: SaleItemWithProduct? = null
        get() = field
        set(value) {
            field = value
            if(value?.saleItemPricePerPiece == 0F){
                value?.saleItemPricePerPiece = value?.saleItemProduct?.productBasePrice?:0F
            }
            mBinding?.saleItem = value
        }

    override fun goToNewSale(saleItem: SaleItemWithProduct) {
        //TODO :Fix this
        //navigateToEditEntity(saleItem, R.id.sale_edit_dest, SaleItemWithProduct::class.java)
    }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }
}