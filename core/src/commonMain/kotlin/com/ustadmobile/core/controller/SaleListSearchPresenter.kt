package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.dao.LocationDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.UmAppDatabase

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SaleDetailView
import com.ustadmobile.core.view.SaleListSearchView

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.view.SelectDateRangeDialogView
import com.ustadmobile.lib.db.entities.Location
import com.ustadmobile.lib.db.entities.SaleListDetail

import com.ustadmobile.core.db.dao.SaleDao

import com.ustadmobile.core.view.SaleDetailView.Companion.ARG_SALE_UID
import com.ustadmobile.core.view.SaleListSearchView.Companion.SORT_HIGHEST_PRICE
import com.ustadmobile.core.view.SaleListSearchView.Companion.SORT_LOWEST_PRICE
import com.ustadmobile.core.view.SaleListSearchView.Companion.SORT_MOST_RECENT
import com.ustadmobile.core.view.SelectDateRangeDialogView.Companion.ARG_FROM_DATE
import com.ustadmobile.core.view.SelectDateRangeDialogView.Companion.ARG_TO_DATE
import com.ustadmobile.door.DoorLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Presenter for SaleListSearch view
 */
class SaleListSearchPresenter(context: Any,
                              arguments: Map<String, String>?,
                              view: SaleListSearchView)
    : CommonHandlerPresenter<SaleListSearchView>(context, arguments!!, view) {

    private lateinit var umProvider: DataSource.Factory<Int, SaleListDetail>
    private var locationLiveData: DoorLiveData<List<Location>>? = null
    internal var repository: UmAppDatabase
    private val saleDao: SaleDao
    private val locationDao: LocationDao

    private var locationToPosition: HashMap<Long, Int>? = null
    private var positionToLocation: HashMap<Int, Long>? = null

    private var idToOrderInteger: HashMap<Long, Int>? = null

    private var from: Long = 0
    private var to: Long = 0
    private var amountFrom: Int = 0
    private var amountTo: Int = 0
    private var stringQuery = "%%"

    private var sort = SORT_MOST_RECENT

    var locationUidSelected: Long = 0
    var loggedInPersonUid : Long = 0

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        saleDao = repository.saleDao
        locationDao = repository.locationDao
        loggedInPersonUid = UmAccountManager.getActivePersonUid(context)

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

        presetAL.add(impl.getString(MessageID.most_recent, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_MOST_RECENT)

        presetAL.add(impl.getString(MessageID.lowest_price, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_LOWEST_PRICE)

        presetAL.add(impl.getString(MessageID.highest_price, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_HIGHEST_PRICE)

        val sortPresets = arrayListToStringArray(presetAL)

        view.updateSortSpinner(sortPresets)
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

    fun handleLocationSelected(selected: Int) {
        if (positionToLocation!!.containsKey(selected)) {

            if (positionToLocation!!.containsKey(selected)) {
                locationUidSelected = positionToLocation!![selected]!!
            } else {
                locationUidSelected = 0L
            }
            //TODO: Update filter and set provider.

            setProvider()
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        //Update location spinner
        locationLiveData = locationDao.findAllActiveLocationsProvider()
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            locationLiveData!!.observe(thisP, thisP::handleLocationsChanged)
        }

        idToOrderInteger = HashMap<Long, Int>()

        updateSortSpinnerPreset()

        //Get provider
        umProvider = saleDao.findAllSaleFilterAndSearchProvider(loggedInPersonUid!!,
                0, 0, 1, from, to, "%", sort)
        setProvider()
    }

    private fun handleLocationsChanged(locations: List<Location>?) {

        locationToPosition = HashMap()
        positionToLocation = HashMap()

        val locationList = ArrayList<String>()
        var pos = 0
        if (locations != null) {
            for (el in locations) {
                locationList.add(el.title!!)
                locationToPosition!![el.locationUid] = pos
                positionToLocation!![pos] = el.locationUid
                pos++
            }
        }
        var locationPresets = locationList.toTypedArray()
        view.updateLocationSpinner(locationPresets)
    }

    fun updateFilter(value: String) {
        stringQuery = "%$value%"
        setProvider()
    }

    fun updateFilter(spl: Int, sph: Int, value: String) {
        stringQuery = "%$value%"
        amountFrom = spl
        amountTo = sph
        setProvider()
    }

    /**
     * Sets the people list provider set in the Presenter to the View.
     */
    private fun setProvider() {
        umProvider = saleDao.findAllSaleFilterAndSearchProvider(loggedInPersonUid!!,
                locationUidSelected,
                amountFrom.toLong(), amountTo.toLong(), from, to, stringQuery, sort)
        view.setListProvider(umProvider!!)
    }

    fun goToSelectDateRange(from: Long, to: Long) {
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        if (from > 0 && to > 0) {
            args.put(ARG_FROM_DATE, from.toString())
            args.put(ARG_TO_DATE, to.toString())
        }
        impl.go(SelectDateRangeDialogView.VIEW_NAME, args, context)
    }


    private fun handleClickSale(saleUid: Long) {
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(ARG_SALE_UID, saleUid.toString())
        impl.go(SaleDetailView.VIEW_NAME, args, context)

    }

    override fun handleCommonPressed(arg: Any, arg2:Any) {
        handleClickSale(arg as Long)
    }

    override fun handleSecondaryPressed(arg: Any) {}

    fun handleDateSelected(fromDate: Long, toDate: Long, dateRangeText: String) {
        //Update Date range text.
        from = fromDate
        to = toDate

        //Update filter and setprovider
        setProvider()

        view.updateDateRangeText(dateRangeText)

    }

    fun handleChangeSortOrder(order: Long) {
        var order = order

        order = order + 1
        if (idToOrderInteger!!.containsKey(order)) {
            sort = idToOrderInteger!!.get(order)!!

            //TODO: Update provider
            setProvider()
        }
    }
}
