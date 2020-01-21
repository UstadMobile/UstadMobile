package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
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
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView
import com.ustadmobile.core.view.SaleDetailView
import com.ustadmobile.core.view.SaleDetailView.Companion.ARG_SALE_GEN_NAME
import com.ustadmobile.core.view.SaleDetailView.Companion.ARG_SALE_UID
import com.ustadmobile.core.view.SaleListSearchView
import com.ustadmobile.core.view.SaleListView
import com.ustadmobile.lib.db.entities.SaleListDetail


/**
 * Presenter for SaleList view
 */
class SaleListPresenter(context: Any,
                        arguments: Map<String, String>?,
                        view: SaleListView,
                        val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : CommonHandlerPresenter<SaleListView>(context, arguments!!, view) {

    private var umProvider: DataSource.Factory<Int, SaleListDetail>? = null
    internal var repository: UmAppDatabase
    private val saleDao: SaleDao
    private val salePaymentDao: SalePaymentDao

    private var idToOrderInteger: MutableMap<Long, Int>? = null

    private var filterSelected: Int = 0

    private var currentSortOrder = 0

    private var personUid = 0L

    private var loggedInPersonUid : Long ? = 0

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        saleDao = repository.saleDao
        salePaymentDao = repository.salePaymentDao

        loggedInPersonUid = UmAccountManager.getActivePersonUid(context)

    }

    //Can be used to put "static" methods in for kotlin
    companion object{
        /**
         * Common method to convert Array List to String Array
         *
         * @param presetAL The array list of string type
         * @return  String array
         */
        fun arrayListToStringArray(presetAL: ArrayList<String>): Array<String?> {
            val objectArr = presetAL.toTypedArray()
            val strArr = arrayOfNulls<String>(objectArr.size)
            for (j in objectArr.indices) {
                strArr[j] = objectArr[j]
            }
            return strArr
        }
    }

    fun handleChangeSortOrder(order: Long) {
        var orderI = order + 1

        if (idToOrderInteger!!.containsKey(orderI)) {
            currentSortOrder = idToOrderInteger!![orderI]!!
            getAndSetProvider(currentSortOrder)
        }
    }

    /**
     * Updates the sort by drop down (spinner) on the Class list. For now the sort options are
     * defined within this method and will automatically update the sort options without any
     * database call.
     */
    private fun updateSortSpinnerPreset() {
        val presetAL = ArrayList<String>()

        idToOrderInteger = HashMap<Long, Int>()

        presetAL.add(impl.getString(MessageID.sort_by_name_asc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_NAME_ASC)
        presetAL.add(impl.getString(MessageID.sort_by_name_desc, context))
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

        if(personUid != 0L){
            umProvider = saleDao.filterAndSortSaleByWeUid(loggedInPersonUid!!, sortCode, personUid)
        }else {
            umProvider = saleDao.filterAndSortSaleByLeUid(loggedInPersonUid!!, filterSelected,
                    sortCode, impl.getLocale(context))
        }
        view.setListProvider(umProvider!!, false, false)

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        //Get provider
        umProvider = saleDao.findAllActiveAsSaleListDetailProvider(loggedInPersonUid!!)
        view.setListProvider(umProvider!!, false, false)

        idToOrderInteger = HashMap()
        updateSortSpinnerPreset()

        observePreOrderAndPaymentCounters()

        if(arguments.containsKey(PersonWithSaleInfoDetailView.ARG_WE_UID)){
            personUid = arguments.get(PersonWithSaleInfoDetailView.ARG_WE_UID).toString().toLong()
        }

    }

    fun observePreOrderAndPaymentCounters() {
        val preOrderLiveData = saleDao.getPreOrderSaleCountLive(loggedInPersonUid!!)
        val paymentsDueLiveData = salePaymentDao.getPaymentsDueCountLive(loggedInPersonUid!!)

        preOrderLiveData.observeWithPresenter(this, this::handlePreOrderCountUpdate)
        paymentsDueLiveData.observeWithPresenter(this, this::handlePaymentDueCountUpdate)

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
        umProvider = saleDao.findAllActiveAsSaleListDetailProvider(loggedInPersonUid!!)
        view.setListProvider(umProvider!!, false, false)

    }

    fun filterPreOrder() {
        filterSelected = PREORDER_SELECTED
        umProvider = saleDao.findAllActiveSaleListDetailPreOrdersProvider(loggedInPersonUid!!)
        view.setListProvider(umProvider!!, false, true)

    }

    fun filterPaymentDue() {
        filterSelected = PAYMENT_SELECTED
        umProvider = saleDao.findAllActiveSaleListDetailPaymentDueProvider(loggedInPersonUid!!)
        view.setListProvider(umProvider!!, true, false)
    }

    internal fun handleClickSale(saleUid: Long, saleName: String?) {
        val args = HashMap<String, String>()
        args.put(ARG_SALE_UID, saleUid.toString())
        if (saleName != null && !saleName.isEmpty())
            args.put(ARG_SALE_GEN_NAME, saleName)
        impl.go(SaleDetailView.VIEW_NAME, args, context)

    }

    fun handleClickPrimaryActionButton() {

        val args = HashMap<String, String>()
        impl.go(SaleDetailView.VIEW_NAME, args, context)
    }

    fun handleClickSearch() {
        val args = HashMap<String, String>()
        impl.go(SaleListSearchView.VIEW_NAME, args, context)
    }

    override fun handleCommonPressed(arg: Any, arg2: Any) {
        handleClickSale(arg as Long, arg2 as String)
    }

    override fun handleSecondaryPressed(arg: Any) {

    }
}
