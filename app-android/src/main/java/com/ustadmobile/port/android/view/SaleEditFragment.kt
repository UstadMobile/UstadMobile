package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSaleEditBinding
import com.ustadmobile.core.controller.*
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SaleEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CREATE_SALE
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList

interface SaleEditFragmentEventHandler {

    fun addSaleItem()
    fun addDelivery()
    fun addPayment()
    fun selectCustomer()
    fun selectLocation()
}

class SaleEditFragment: UstadEditFragment<SaleWithCustomerAndLocation>(), SaleEditView,
        SaleEditFragmentEventHandler, SaleItemListItemListener,
        SaleDeliveryItemListener, SalePaymentItemListener{

    private var mBinding: FragmentSaleEditBinding? = null

    private var mPresenter: SaleEditPresenter? = null

    private var saleItemRecyclerAdpater: SaleItemRecyclerAdapter? = null
    private var saleItemRecyclerView : RecyclerView? = null
    private val saleItemObserver = Observer<List<SaleItemWithProduct>?> {
        t ->
        run {
            saleItemRecyclerAdpater?.submitList(t)
        }
    }

    private var saleDeliveryRecyclerAdapter: SaleDeliveryRecyclerAdapter? = null
    private var saleDeliveryRecyclerView : RecyclerView? = null
    private val saleDeliveryObserver = Observer<List<SaleDelivery>?> {
        t ->
        run {
            saleDeliveryRecyclerAdapter?.submitList(t)
        }
    }

    private var salePaymentRecyclerAdpater : SalePaymentRecyclerAdapter? = null
    private var salePaymentRecyclerView : RecyclerView? = null
    private val salePaymentObserver = Observer<List<SalePayment>?> {
        t ->
        run {
            salePaymentRecyclerAdpater?.submitList(t)
        }
    }

    override val mEditPresenter: UstadEditPresenter<*, SaleWithCustomerAndLocation>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSaleEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.activityEventHandler = this
            it.orderTotal = orderTotal
            it.paymentTotal = paymentTotal
        }

        saleItemRecyclerAdpater = SaleItemRecyclerAdapter(this)
        saleItemRecyclerView = rootView.findViewById(R.id.fragment_sale_edit_saleitem_rv)
        saleItemRecyclerView?.adapter = saleItemRecyclerAdpater
        saleItemRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        saleDeliveryRecyclerAdapter = SaleDeliveryRecyclerAdapter(this)
        saleDeliveryRecyclerView = rootView.findViewById(R.id.fragment_sale_edit_delivery_rv)
        saleDeliveryRecyclerView?.adapter = saleDeliveryRecyclerAdapter
        saleDeliveryRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        salePaymentRecyclerAdpater = SalePaymentRecyclerAdapter(this)
        salePaymentRecyclerView = rootView.findViewById(R.id.fragment_sale_edit_payment_rv)
        salePaymentRecyclerView?.adapter = salePaymentRecyclerAdpater
        salePaymentRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = SaleEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                SaleItemWithProduct::class.java) {
            val saleItem = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleAddOrEditSaleItem(saleItem)
            //Add numbers
            orderTotal = orderTotal?.plus(saleItem.saleItemQuantity * saleItem.saleItemPricePerPiece.toLong())
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                SaleDelivery::class.java) {
            val saleDelivery = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleAddOrEditSaleDelivery(saleDelivery)
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                SalePayment::class.java) {
            val salePayment = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleAddOrEditSalePayment(salePayment)
            //Add numbers
            paymentTotal = paymentTotal?.plus(salePayment.salePaymentPaidAmount)
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                Person::class.java) {
            val customer = it.firstOrNull() ?: return@observeResult
            entity?.saleCustomerUid = customer.personUid
            entity?.person = customer
            mBinding?.fragmentSaleEditCustomerTiet?.setText(customer.fullName())
            mBinding?.sale = entity
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                Location::class.java) {
            val province = it.firstOrNull() ?: return@observeResult
            entity?.saleLocationUid = province.locationUid
            entity?.location = province
            mBinding?.fragmentSaleEditProvinceTiet?.setText(province.locationTitle)
            mBinding?.sale = entity
        }

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

    }

    override var saleItemList: DoorMutableLiveData<List<SaleItemWithProduct>>? = null
        set(value) {
            field?.removeObserver(saleItemObserver)
            field = value
            value?.observe(this, saleItemObserver)
        }

    override var saleDeliveryList: DoorMutableLiveData<List<SaleDelivery>>? = null
        set(value) {
            field?.removeObserver(saleDeliveryObserver)
            field = value
            value?.observe(this, saleDeliveryObserver)
        }

    override var salePaymentList: DoorMutableLiveData<List<SalePayment>>? = null
        set(value) {
            field?.removeObserver(salePaymentObserver)
            field = value
            value?.observe(this, salePaymentObserver)
        }


    override var orderTotal: Long? = 0
        set(value) {
            mBinding?.orderTotal = value
            field = value
        }

    override var paymentTotal: Long? = 0
        set(value) {
            mBinding?.paymentTotal = value
            field = value
        }


    override var balanceDue: Long? = 0
        set(value) {
            mBinding?.fragmentSaleEditBalanceDueTv?.text = value.toString()
            field = value
        }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        saleItemRecyclerView?.adapter = null
        saleItemRecyclerView = null
        saleItemRecyclerAdpater = null

        saleDeliveryRecyclerView?.adapter = null
        saleDeliveryRecyclerView = null
        saleDeliveryRecyclerAdapter = null

        salePaymentRecyclerView?.adapter = null
        salePaymentRecyclerView = null
        salePaymentRecyclerAdpater = null

    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.add_sale, R.string.edit_sale)
    }

    override var entity: SaleWithCustomerAndLocation? = null
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


    override fun addSaleItem() {
        //Go to product list followed by sale item edit
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(SaleItemWithProduct::class.java, R.id.product_list_dest, args= bundleOf(
                ARG_CREATE_SALE to "true"
        ))
    }

    override fun addDelivery() {
        if(entity?.saleUid != 0L) {
            onSaveStateToBackStackStateHandle()
            val saleDeliveryWithItems = SaleDeliveryAndItems().apply{
                delivery = null
                saleItems = saleItemList?.value?: listOf()
            }

            navigateToEditEntity(saleDeliveryWithItems, R.id.saledelivery_edit_dest, SaleDeliveryAndItems::class.java,
                    argBundle = bundleOf(
                            UstadView.ARG_SALE_UID to entity?.saleUid.toString()))
        }else{
            showSnackBar(requireContext().getString(R.string.save_sale_before_delivery))
        }

    }

    override fun addPayment() {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(null, R.id.salepayment_edit_dest, SalePayment::class.java)
    }

    override fun selectCustomer() {
        // Go to person select
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(Person::class.java, R.id.home_personlist_dest,
                args = bundleOf(UstadView.ARG_FILTER_PERSON_CUSTOMER to "true")
        )
    }

    override fun selectLocation() {
        //Go to location select
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(Location::class.java, R.id.location_list_dest,
                args = bundleOf())
    }

    override fun onClickSaleItem(saleItem: SaleItemWithProduct) {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(saleItem, R.id.saleitem_edit_dest, SaleItem::class.java)
    }

    override fun onClickSaleDelivery(saleDelivery: SaleDelivery) {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(saleDelivery, R.id.saledelivery_edit_dest,
                SaleDelivery::class.java, argBundle = bundleOf(
                    UstadView.ARG_SALE_UID to entity?.saleUid.toString()))
    }

    override fun onClickRemoveSaleDelivery(saleDelivery: SaleDelivery) {
        mPresenter?.handleRemoveSaleDelivery(saleDelivery)
    }

    override fun onClickSalePayment(salePayment: SalePayment) {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(salePayment, R.id.salepayment_edit_dest, SalePayment::class.java)
    }

    override fun onClickRemoveSalePayment(salePayment: SalePayment) {
        mPresenter?.handleRemoveSalePayment(salePayment)
    }
}