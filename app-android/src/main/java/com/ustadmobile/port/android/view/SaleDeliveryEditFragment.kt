package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSaleDeliveryEditBinding
import com.ustadmobile.core.controller.SaleDeliveryEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SaleDeliveryEditView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

interface SaleDeliveryEditFragmentEventHandler {

}

class SaleDeliveryEditFragment: UstadEditFragment<SaleDelivery>(), SaleDeliveryEditView,
        SaleDeliveryEditFragmentEventHandler {

    private var mBinding: FragmentSaleDeliveryEditBinding? = null

    private var mPresenter: SaleDeliveryEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, SaleDelivery>?
        get() = mPresenter

    private var saleItemsRecyclerAdapter: SaleItemWithProducerInventorySelectionRecyclerAdapter? = null
    private var saleItemListRecyclerView: RecyclerView? = null
    private val saleItemObserver = Observer<List<SaleItemWithProduct>?>{
        t ->
        run {
            saleItemsRecyclerAdapter?.submitList(t)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSaleDeliveryEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        saleItemsRecyclerAdapter = SaleItemWithProducerInventorySelectionRecyclerAdapter()
        saleItemListRecyclerView = rootView.findViewById(R.id.fragment_sale_delivery_edit_items_rv)
        saleItemListRecyclerView?.adapter = saleItemsRecyclerAdapter
        saleItemListRecyclerView?.layoutManager = LinearLayoutManager(requireContext())


        mPresenter = SaleDeliveryEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()


        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                SaleItemWithProduct::class.java) {
            val producer = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleAddOrEditSaleItemWithProduct(producer)
        }


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
        setEditFragmentTitle(R.string.delivery, R.string.delivery)
    }

    override var entity: SaleDelivery? = null
        get() = field
        set(value) {
            field = value
            mBinding?.saleDelivery = value
        }
    override var saleItems: DoorMutableLiveData<List<SaleItemWithProduct>>? = null
        set(value) {
            field?.removeObserver(saleItemObserver)
            field = value
            value?.observe(this, saleItemObserver)
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }
}