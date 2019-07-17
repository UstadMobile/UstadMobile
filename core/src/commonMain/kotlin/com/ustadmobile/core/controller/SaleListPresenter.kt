package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.db.dao.SaleDao.Companion.ALL_SELECTED
import com.ustadmobile.core.db.dao.SaleDao.Companion.PAYMENT_SELECTED
import com.ustadmobile.core.db.dao.SaleDao.Companion.PREORDER_SELECTED
import com.ustadmobile.core.db.dao.SaleDao.Companion.SORT_ORDER_AMOUNT_ASC
import com.ustadmobile.core.db.dao.SaleDao.Companion.SORT_ORDER_AMOUNT_DESC
import com.ustadmobile.core.db.dao.SaleDao.Companion.SORT_ORDER_DATE_CREATED_ASC
import com.ustadmobile.core.db.dao.SaleDao.Companion.SORT_ORDER_DATE_CREATED_DESC
import com.ustadmobile.core.db.dao.SaleDao.Companion.SORT_ORDER_NAME_ASC
import com.ustadmobile.core.db.dao.SaleDao.Companion.SORT_ORDER_NAME_DESC
import com.ustadmobile.core.db.dao.SalePaymentDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SaleDetailView
import com.ustadmobile.core.view.SaleDetailView.Companion.ARG_SALE_GEN_NAME
import com.ustadmobile.core.view.SaleDetailView.Companion.ARG_SALE_UID
import com.ustadmobile.core.view.SaleListSearchView
import com.ustadmobile.core.view.SaleListView
import com.ustadmobile.lib.db.entities.SaleListDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Presenter for SaleList view
 */
class SaleListPresenter(context: Any,
                        arguments: Map<String, String>?,
                        view: SaleListView)
    : CommonHandlerPresenter<SaleListView>(context, arguments!!, view) {

    private var umProvider: DataSource.Factory<Int, SaleListDetail>? = null
    internal var repository: UmAppDatabase
    private val saleDao: SaleDao
    private val salePaymentDao: SalePaymentDao

    private var idToOrderInteger: MutableMap<Long, Int>? = null

    private var filterSelected: Int = 0

    private var currentSortOrder = 0

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        saleDao = repository.saleDao
        salePaymentDao = repository.salePaymentDao

    }

    fun handleChangeSortOrder(order: Long) {
        var order = order
        order = order + 1

        if (idToOrderInteger!!.containsKey(order)) {
            currentSortOrder = idToOrderInteger!![order]!!
            getAndSetProvider(currentSortOrder)
        }
    }

    /**
     * Common method to convert Array List to String Array
     *
     * @param presetAL The array list of string type
     * @return  String array
     */
    private fun arrayListToStringArray(presetAL: ArrayList<String>): Array<String?> {
        val objectArr = presetAL.toTypedArray()
        val strArr = arrayOfNulls<String>(objectArr.size)
        for (j in objectArr.indices) {
            strArr[j] = objectArr[j]
        }
        return strArr
    }

    /**
     * Updates the sort by drop down (spinner) on the Class list. For now the sort options are
     * defined within this method and will automatically update the sort options without any
     * database call.
     */
    private fun updateSortSpinnerPreset() {
        val presetAL = ArrayList<String>()
        val impl = UstadMobileSystemImpl.instance

        idToOrderInteger = HashMap<Long, Int>()

        presetAL.add(impl.getString(MessageID.sort_by_name_asc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_NAME_ASC)
        presetAL.add(impl.getString(MessageID.sorT_by_name_desc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_NAME_DESC)
        presetAL.add(impl.getString(MessageID.sale_list_sort_by_total_asc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_AMOUNT_ASC)
        presetAL.add(impl.getString(MessageID.sale_list_sort_by_total_desc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_AMOUNT_DESC)
        presetAL.add(impl.getString(MessageID.sale_list_sort_by_date_desc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_DATE_CREATED_DESC)
        presetAL.add(impl.getString(MessageID.sale_list_sort_by_date_asc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_DATE_CREATED_ASC)

        val sortPresets = arrayListToStringArray(presetAL)

        view.updateSortSpinner(sortPresets)
    }


    private fun getAndSetProvider(sortCode: Int) {

        umProvider = saleDao.filterAndSortSale(filterSelected, sortCode)
        view.setListProvider(umProvider!!, false, false)

    }

    fun filterAndSetProvider(search: String) {
        umProvider = saleDao.filterAndSortSale(filterSelected, search, currentSortOrder)
        view.setListProvider(umProvider!!, false, false)
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        //Get provider
        umProvider = saleDao.findAllActiveAsSaleListDetailProvider()
        view.setListProvider(umProvider!!, false, false)

        idToOrderInteger = HashMap<Long, Int>()
        updateSortSpinnerPreset()

        observePreOrderAndPaymentCounters()

    }

    fun observePreOrderAndPaymentCounters() {
        val preOrderLiveData = saleDao.getPreOrderSaleCountLive()
        val paymentsDueLiveData = salePaymentDao.getPaymentsDueCountLive()

        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            preOrderLiveData.observe(thisP, thisP::handlePreOrderCountUpdate)
            paymentsDueLiveData.observe(thisP, thisP::handlePaymentDueCountUpdate)
        }
    }

    fun handlePreOrderCountUpdate(count: Int?) {
        if (count != null) {
            view.updatePreOrderCounter(count)
        }
    }

    fun handlePaymentDueCountUpdate(count: Int?) {
        if (count != null) {
            view.updatePaymentDueCounter(count)
        }
    }

    fun filterAll() {
        filterSelected = ALL_SELECTED
        umProvider = saleDao.findAllActiveAsSaleListDetailProvider()
        view.setListProvider(umProvider!!, false, false)

    }

    fun filterPreOrder() {
        filterSelected = PREORDER_SELECTED
        umProvider = saleDao.findAllActiveSaleListDetailPreOrdersProvider()
        view.setListProvider(umProvider!!, false, true)

    }

    fun filterPaymentDue() {
        filterSelected = PAYMENT_SELECTED
        umProvider = saleDao.findAllActiveSaleListDetailPaymentDueProvider()
        view.setListProvider(umProvider!!, true, false)
    }

    internal fun handleClickSale(saleUid: Long, saleName: String?) {
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(ARG_SALE_UID, saleUid.toString())
        if (saleName != null)
            args.put(ARG_SALE_GEN_NAME, saleName)
        impl.go(SaleDetailView.VIEW_NAME, args, context)

    }

    fun handleClickPrimaryActionButton() {

        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        impl.go(SaleDetailView.VIEW_NAME, args, context)
    }

    fun handleClickSearch() {
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        impl.go(SaleListSearchView.VIEW_NAME, args, context)
    }

    override fun handleCommonPressed(arg: Any, arg2: Any) {
        handleClickSale(arg as Long, arg2 as String)
    }

    override fun handleSecondaryPressed(arg: Any) {

    }
}
