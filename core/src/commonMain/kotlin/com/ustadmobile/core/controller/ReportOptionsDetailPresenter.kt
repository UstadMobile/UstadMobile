package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.DashboardEntryDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.ReportOptions
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ReportOptionsDetailView.Companion.ARG_DASHBOARD_ENTRY_UID
import com.ustadmobile.core.view.ReportOptionsDetailView.Companion.ARG_REPORT_OPTIONS
import com.ustadmobile.core.view.ReportOptionsDetailView.Companion.ARG_REPORT_TYPE
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView.Companion.ARG_LOCATIONS_SET
import com.ustadmobile.core.view.SelectMultiplePeopleView.Companion.ARG_SELECTED_PEOPLE
import com.ustadmobile.core.view.SelectMultipleProductTypeTreeDialogView.Companion.ARG_PRODUCT_SELECTED_SET
import com.ustadmobile.lib.db.entities.DashboardEntry
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject


/**
 * Presenter for ReportOptionsDetail view
 */
class ReportOptionsDetailPresenter(context: Any, arguments: Map<String, String>?,
                                   view: ReportOptionsDetailView)
    : UstadBaseController<ReportOptionsDetailView>(context, arguments!!, view) {

    internal var repository: UmAppDatabase
    private val dashboardEntryDao: DashboardEntryDao
    private val personDao: PersonDao

    private var idToGroupByInteger: HashMap<Long, Int>? = null

    private var currentGroupBy = 0

    private var reportType = 0
    internal var impl: UstadMobileSystemImpl
    private var currentDashboardEntry: DashboardEntry? = null
    private var dashboardEntryUid = 0L
    private var loggedInPerson: Person? = null

    private var fromDate: Long = 0
    private var toDate: Long = 0
    private var fromPrice: Int = 0
    private var toPrice: Int = 0

    private var selectedLocations: List<Long>? = null
    private var selectedProducts: List<Long>? = null
    private var selectedLEs: List<Long>? = null

    private var reportOptions: ReportOptions? = null
    private var reportOptionString: String? = null

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        dashboardEntryDao = repository.dashboardEntryDao
        personDao = repository.personDao

        impl = UstadMobileSystemImpl.instance

        if (arguments!!.containsKey(ARG_REPORT_TYPE)) {
            reportType = arguments.get(ARG_REPORT_TYPE).toString().toInt()
        }

        if (arguments.containsKey(ARG_DASHBOARD_ENTRY_UID)) {
            dashboardEntryUid = arguments.get(ARG_DASHBOARD_ENTRY_UID).toString().toLong()
        }

        if (arguments.containsKey(ARG_REPORT_OPTIONS)) {
            reportOptionString = arguments.get(ARG_REPORT_OPTIONS).toString()
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        val loggedInPersonUid = UmAccountManager.getActivePersonUid(context)
        GlobalScope.launch {
            loggedInPerson = personDao.findByUid(loggedInPersonUid)
            if(loggedInPerson != null){
                if(loggedInPerson!!.admin){
                    view.runOnUiThread(Runnable {
                        view.showLEsOption(true)
                    })
                }else{
                    view.runOnUiThread(Runnable {
                        view.showLEsOption(false)
                    })
                }
            }else{
                view.runOnUiThread(Runnable {
                    view.showLEsOption(false)
                })
            }
        }

        if (dashboardEntryUid != 0L) {
            GlobalScope.launch {
                val result = dashboardEntryDao.findByUidAsync(dashboardEntryUid)
                if (result != null) {
                    view.runOnUiThread(Runnable {
                        view.setTitle(result.dashboardEntryTitle!!)
                    })
                    currentDashboardEntry = result
                    initFromDashboardEntry()
                }
            }
        } else {
            reportOptions = ReportOptions()
            //Set title based on given.
            setTitleFromArgs()
            initFromDashboardEntry()
        }
    }

    private fun setReportOptionFromString(reportOptionsString: String) {
        val json = Json(JsonConfiguration.Stable)
        reportOptions = json.parse(ReportOptions.serializer(), reportOptionsString)

        val pts = (json.parseJson(reportOptionsString) as JsonObject).get("productTypes")?.jsonArray
        val pt = mutableListOf<Long>()
        if(pts!= null) {
            for (i in 0 until pts.size) {
                val item = pts[i].toString().toLong()
                pt.add(i,item)
            }
        }
        reportOptions?.productTypes = pt
        var ptFlag = 0
        if(pt.isNotEmpty()){
            ptFlag = 1
        }

        val jal = (json.parseJson(reportOptionsString) as JsonObject).get("locations")?.jsonArray
        val l = mutableListOf<Long>()
        if(jal!= null) {
            for (i in 0 until jal.size) {
                val item = jal[i].toString().toLong()
                l.add(i,item)
            }
        }
        reportOptions?.locations = l
        var lFlag = 0
        if(l.isNotEmpty()){
            lFlag = 1
        }

        val jale = (json.parseJson(reportOptionsString) as JsonObject).get("les")?.jsonArray
        val le = mutableListOf<Long>()
        if(jale!= null) {
            for (i in 0 until jale.size) {
                val item = jale[i].toString().toLong()
                le.add(i,item)
            }
        }
        reportOptions?.les = le
        var leFlag = 0
        if(le.isNotEmpty()){
            leFlag = 1
        }


        fromDate = reportOptions!!.fromDate
        toDate = reportOptions!!.toDate
        fromPrice = reportOptions!!.fromPrice
        toPrice = reportOptions!!.toPrice
    }

    private fun initFromDashboardEntry() {
        if (currentDashboardEntry != null) {
            //Populate filter from entity.
            val dashboardReportOptionsString = currentDashboardEntry!!.dashboardEntryReportParam

            if (dashboardReportOptionsString != null && !dashboardReportOptionsString!!.isEmpty()) {
                setReportOptionFromString(dashboardReportOptionsString)
            }
        } else if (reportOptionString != null && !reportOptionString!!.isEmpty()) {
            setReportOptionFromString(reportOptionString!!)
        }

        view.runOnUiThread(Runnable {
            view.setEditMode(currentDashboardEntry != null)
        })


        //Build report options on view:

        selectedLocations = reportOptions!!.locations
        selectedLEs = reportOptions!!.les
        if(loggedInPerson!= null && !loggedInPerson!!.admin){
            val les = ArrayList<Long>()
            les.add(loggedInPerson!!.personUid)
            selectedLEs = les

        }
        selectedProducts = reportOptions!!.productTypes
        currentGroupBy = reportOptions!!.groupBy

        //Date range
        updateDateRangeOnView()

        //Sale price rage
        updateSalePriceRangeOnView()

        //Show Average
        view.runOnUiThread(Runnable {
            view.setShowAverage(reportOptions!!.isShowAverage)
        })


        if (selectedLocations!!.isEmpty()) {
            view.runOnUiThread(Runnable {
                view.setLocationSelected(impl.getString(MessageID.all, context))
            })
        } else {
            val locationDao = repository.locationDao
            GlobalScope.launch {
                val result = locationDao.findAllLocationNamesInUidList(selectedLocations!!)
                view.runOnUiThread(Runnable {
                    view.setLocationSelected(result!!)
                })
            }

        }

        if (selectedProducts!!.isEmpty()) {
            view.runOnUiThread(Runnable {
                view.setProductTypeSelected(impl.getString(MessageID.all, context))
            })
        } else {
            val saleProductDao = repository.saleProductDao
            GlobalScope.launch {
                val result = saleProductDao.findAllProductNamesInUidList(selectedProducts!!)
                view.runOnUiThread(Runnable {
                    view.setProductTypeSelected(result!!)
                })
            }
        }

        if (selectedLEs!!.isEmpty()) {
            view.runOnUiThread(Runnable {
                view.setLESelected(impl.getString(MessageID.all, context))
            })
        } else {
            val personDao = repository.personDao
            GlobalScope.launch {
                val result = personDao.findAllPeopleNamesInUidList(selectedLEs!!)
                view.runOnUiThread(Runnable {
                    view.setLESelected(result!!)
                })
            }
        }

        //Group by
        populateGroupBy()

    }

    fun updateSalePriceRangeOnView() {

        val toS = toPrice.toLong()
        val fromS = fromPrice.toLong()

        val rangeText = (impl.getString(MessageID.from, context) + " "
                + fromS + " Afs - " + toS + " Afs")
        view.runOnUiThread(Runnable {
            view.setSalePriceRangeSelected(fromPrice, toPrice, rangeText)
        })


    }

    fun updateDateRangeOnView() {

        if (fromDate == 0L && toDate == 0L) {
            fromDate = UMCalendarUtil.getDateInMilliPlusDays(-31)
            toDate = UMCalendarUtil.getDateInMilliPlusDays(0)
        }

        view.runOnUiThread(Runnable {
            view.setDateRangeSelectedLongs(fromDate, toDate)
        })

    }

    private fun setTitleFromArgs() {
        var reportTitle = impl.getString(MessageID.report_options, context)
        when (reportType) {
            DashboardEntry.REPORT_TYPE_SALES_PERFORMANCE -> reportTitle = impl.getString(MessageID.sales_performance_report_options, context)
            DashboardEntry.REPORT_TYPE_SALES_LOG -> reportTitle = impl.getString(MessageID.sales_log_report_options, context)
            DashboardEntry.REPORT_TYPE_TOP_LES -> reportTitle = impl.getString(MessageID.top_les_report_options, context)
            else -> {
            }
        }

        view.runOnUiThread(Runnable {
            view.setTitle(reportTitle)
        })
    }

    private fun populateGroupBy() {
        val presetAL = ArrayList<String>()
        idToGroupByInteger = HashMap<Long, Int>()

        presetAL.add(impl.getString(MessageID.location, context))
        idToGroupByInteger!!.put(presetAL.size.toLong(), GROUP_BY_LOCATION)
        presetAL.add(impl.getString(MessageID.product_type, context))
        idToGroupByInteger!!.put(presetAL.size.toLong(), GROUP_BY_PRODUCT_TYPE)
        presetAL.add(impl.getString(MessageID.grantee, context))
        idToGroupByInteger!!.put(presetAL.size.toLong(), GROUP_BY_GRANTEE)

        val sortPresets = arrayListToStringArray(presetAL)

        view.runOnUiThread(Runnable {
            view.setGroupByPresets(sortPresets, currentGroupBy - 1)
        })
    }

    fun handleChangeGroupBy(order: Long) {
        var order = order
        order = order + 1

        if (idToGroupByInteger!!.containsKey(order)) {
            currentGroupBy = idToGroupByInteger!![order]!!
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

    fun handleClickCreateReport() {

        //Add remainder bits
        reportOptions!!.fromPrice = fromPrice
        reportOptions!!.toPrice = toPrice
        reportOptions!!.fromDate = fromDate
        reportOptions!!.toDate = toDate

        val s = selectedLocations
        if (s != null) {
            reportOptions!!.locations = s.toMutableList()
        }

        reportOptions!!.productTypes = selectedProducts
        reportOptions!!.les = selectedLEs

        //Create json from reportOptions
        val json = Json(JsonConfiguration.Stable)
        val reportOptionsString = json.toJson(ReportOptions.serializer(), reportOptions!!).toString()

        //Update dashboard entry
        if (currentDashboardEntry != null) {
            currentDashboardEntry!!.dashboardEntryReportParam = reportOptionsString
            GlobalScope.launch {
                dashboardEntryDao.updateAsync(currentDashboardEntry!!)
                view.finish()
            }

        } else {

            val args = HashMap<String, String>()
            args.put(ARG_REPORT_TYPE, reportType.toString())
            args.put(ARG_REPORT_OPTIONS, reportOptionsString)

            impl.go(ReportDetailView.VIEW_NAME, args, context)

            view.finish()
        }
    }

    fun goToProductSelect() {
        val args = HashMap<String, String>()

        if (selectedProducts != null && !selectedProducts!!.isEmpty()) {
            val selectedProductTypesCSString = convertLongListToStringCSV(selectedProducts!!)
            args.put(ARG_PRODUCT_SELECTED_SET, selectedProductTypesCSString)
        }

        impl.go(SelectMultipleProductTypeTreeDialogView.VIEW_NAME, args, context)
    }

    fun goToLEsSelect() {
        val args = HashMap<String, String>()
        if (selectedLEs != null && !selectedLEs!!.isEmpty()) {
            val selectedLEsCSSting = convertLongListToStringCSV(selectedLEs!!)
            args.put(ARG_SELECTED_PEOPLE, selectedLEsCSSting)
        }
        impl.go(SelectMultiplePeopleView.VIEW_NAME, args, context)
    }

    fun goToLocationSelect() {
        val args = HashMap<String, String>()

        if (selectedLocations != null && !selectedLocations!!.isEmpty()) {
            val selectedLocationsCSString = convertLongListToStringCSV(selectedLocations!!)
            args.put(ARG_LOCATIONS_SET, selectedLocationsCSString)
        }

        impl.go(SelectMultipleLocationTreeDialogView.VIEW_NAME, args, context)
    }

    fun setSelectedLocations(selectedLocations: List<Long>) {
        this.selectedLocations = selectedLocations
    }

    fun setSelectedProducts(selectedProducts: List<Long>) {
        this.selectedProducts = selectedProducts
    }

    fun setSelectedLEs(selectedLEs: List<Long>) {
        this.selectedLEs = selectedLEs
    }

    fun handleToggleAverage(ticked: Boolean) {
        reportOptions!!.isShowAverage = ticked
    }

    fun setFromDate(fromDate: Long) {
        this.fromDate = fromDate
    }

    fun setToDate(toDate: Long) {
        this.toDate = toDate
    }

    fun setFromPrice(fromPrice: Int) {
        this.fromPrice = fromPrice
    }

    fun setToPrice(toPrice: Int) {
        this.toPrice = toPrice
    }

    companion object {
        val GROUP_BY_LOCATION = 1
        val GROUP_BY_PRODUCT_TYPE = 2
        val GROUP_BY_GRANTEE = 3

        /**
         * Convert LongList to CSV String
         * @param longList    Long list
         */
        fun convertLongListToStringCSV(longList: List<Long>): String {
            return longList.toString().replace("\\[|\\]".toRegex(), "")
        }

    }
}
