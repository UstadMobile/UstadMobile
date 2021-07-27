package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSaleItemEditBinding
import com.ustadmobile.core.controller.SaleItemEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UMAndroidUtil.getCurrentLocale
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SaleItemEditView
import com.ustadmobile.lib.db.entities.SaleItemWithProduct
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.binding.MODE_START_OF_DAY

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
            it.locale = getCurrentLocale(requireContext())

        }

        mBinding?.fragmentSaleItemEditQuantityTiet?.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val quantity = p0.toString()
                val quantityValue = quantity.toLongOrNull()?:0L
                val price =mBinding?.fragmentSaleItemEditPriceeachTiet?.text.toString()
                val priceFloat = price.toFloatOrNull()?:0F
                if(price.isNotEmpty() && quantity.isNotEmpty()) {
                    val total: Float = quantityValue * priceFloat
                    updateTotal(total)
                }
            }
        })

        mBinding?.fragmentSaleItemEditPriceeachTiet?.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val price = p0.toString()
                val quantity = mBinding?.fragmentSaleItemEditQuantityTiet?.text.toString()
                val quantityValue = quantity.toLongOrNull()?:0L
                val priceValue: Float = price.toFloatOrNull()?:0F
                if(price.isNotEmpty() && quantity.isNotEmpty()) {
                    val total: Float = priceValue * quantityValue
                    updateTotal(total)
                }
            }
        })

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
            mBinding?.dateTimeMode = MODE_START_OF_DAY
            mBinding?.timeZoneId = "Asia/Kabul"
        }

    override fun goToNewSale(saleItem: SaleItemWithProduct) {

    }

    override fun updateTotal(total: Float){
        mBinding?.fragmentSaleItemEditTotalValueTv?.text = total.toString()
    }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }
}