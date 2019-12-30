package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ReportAttendanceGroupedByThresholdsView
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_CLAZZ_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_FROM_DATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_GENDER_DISAGGREGATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_LOCATION_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_STUDENT_IDENTIFIER_NUMBER
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_STUDENT_IDENTIFIER_PERCENTAGE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_THRESHOLD_HIGH
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_THRESHOLD_LOW
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_THRESHOLD_MID
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_TO_DATE
import com.ustadmobile.core.xlsx.UmSheet
import com.ustadmobile.core.xlsx.UmXLSX
import com.ustadmobile.core.xlsx.ZipUtil
import com.ustadmobile.lib.db.entities.AttendanceResultGroupedByAgeAndThreshold
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.IOException


/**
 * The ReportNumberOfDaysClassesOpen Presenter.
 */
class ReportAttendanceGroupedByThresholdsPresenter(context: Any, arguments: Map<String, String>?,
                                                   view: ReportAttendanceGroupedByThresholdsView)
    : UstadBaseController<ReportAttendanceGroupedByThresholdsView>(context, arguments!!, view) {

    private var fromDate: Long = 0
    private var toDate: Long = 0
    private var clazzList: List<Long>? = null
    private var locationList: List<Long>? = null
    var thresholdValues: ThresholdValues? = null
    private var index: Int = 0

    var isGenderDisaggregate = true
        private set
    var showPercentages: Boolean? = false

    val z = ZipUtil()

    private val dataMapsMap: LinkedHashMap<String, List<AttendanceResultGroupedByAgeAndThreshold>>

    internal var repository: UmAppDatabase

    fun dataToXLSX(title: String, xlsxReportPath: String, workingDir: String,
                   tableTextData: List<Array<String?>>) {

        try {
            z.createEmptyZipFile(xlsxReportPath)

            val umXLSX = UmXLSX(title, xlsxReportPath, workingDir)

            val reportSheet = UmSheet("Report")

            //Loop over tableTextData
            var r = 1
            for (tableTextDatum in tableTextData) {
                var c = 0
                for (i in tableTextDatum.indices) {
                    val value = tableTextDatum[i]
                    reportSheet.addValueToSheet(r, c, value!!)
                    c++
                }
                r++
            }
            umXLSX.addSheet(reportSheet)

            //Generate the xlsx report from the xlsx object.
            umXLSX.createXLSX()
            view.generateXLSXReport(xlsxReportPath)

        } catch (e: IOException) {
            print(e.message)
        }

    }

    class ThresholdValues {
        var low: Int = 0
        var med: Int = 0
        var high: Int = 0
    }

    private fun convertLongArray(array: LongArray): ArrayList<Long> {
        val result = ArrayList<Long>(array.size)
        for (item in array)
            result.add(item)
        return result
    }

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        dataMapsMap = LinkedHashMap()
        index = 0

        thresholdValues = ThresholdValues()
        clazzList = ArrayList()
        locationList = ArrayList()

        if (arguments!!.containsKey(ARG_FROM_DATE)) {
            fromDate = arguments!!.get(ARG_FROM_DATE)!!.toLong()
        }
        if (arguments!!.containsKey(ARG_TO_DATE)) {
            toDate = arguments!!.get(ARG_TO_DATE)!!.toLong()
        }

        if (arguments!!.containsKey(ARG_LOCATION_LIST)) {
            //TODO: Flatten this from String CSV
            val locations = arguments!!.get(ARG_LOCATION_LIST) as LongArray
            locationList = convertLongArray(locations)
        }
        if (arguments!!.containsKey(ARG_CLAZZ_LIST)) {
            //TODO: Flatten this from String CSV
            val clazzes = arguments!!.get(ARG_CLAZZ_LIST) as LongArray
            clazzList = convertLongArray(clazzes)
        }

        if (arguments!!.containsKey(ARG_THRESHOLD_LOW)) {
            thresholdValues!!.low = arguments!!.get(ARG_THRESHOLD_LOW)!!.toInt()
        }
        if (arguments!!.containsKey(ARG_THRESHOLD_MID)) {
            thresholdValues!!.med = arguments!!.get(ARG_THRESHOLD_MID)!!.toInt()
        }
        if (arguments!!.containsKey(ARG_THRESHOLD_HIGH)) {
            thresholdValues!!.high = arguments!!.get(ARG_THRESHOLD_HIGH)!!.toInt()
        }

        if (arguments!!.containsKey(ARG_GENDER_DISAGGREGATE)) {
            //TODO: KMP Check if boolean conversion works. Fix this. Most probably wrong
            isGenderDisaggregate = arguments!!.get(ARG_GENDER_DISAGGREGATE)!!.toBoolean()
        }

        if (arguments!!.containsKey(ARG_STUDENT_IDENTIFIER_NUMBER)) {
            //TODO: Boolean conversion check. Most probably wrong.
            val numberIdentifier = arguments!!.get(ARG_STUDENT_IDENTIFIER_NUMBER)!!.toBoolean()
            showPercentages = !numberIdentifier
        }

        if (arguments!!.containsKey(ARG_STUDENT_IDENTIFIER_PERCENTAGE)) {
            //TODO: Boolean conversion check. Most probably wrong.
            val percentageIdentifier = arguments!!.get(ARG_STUDENT_IDENTIFIER_PERCENTAGE)!!.toBoolean()
            showPercentages = percentageIdentifier
        }

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        getDataAndUpdateTable()
    }

    private fun buildMapAndUpdateView(theLocationName: String?,
                                      result: List<AttendanceResultGroupedByAgeAndThreshold>?) {
        dataMapsMap[theLocationName!!] = result!!
        if (index >= locationList!!.size) {
            view.updateTables(dataMapsMap)
        }
    }

    /**
     * Separated out method that queries the database and updates the report upon getting and
     * ordering the result.
     */
    private fun getDataAndUpdateTable() {

        val currentTime = UMCalendarUtil.getDateInMilliPlusDays(0)
        val recordDao = repository.clazzLogAttendanceRecordDao
        val locationdao = repository.locationDao

        //Loop over locations
        if (!locationList!!.isEmpty()) {
            for (locationUid in locationList!!) {
                GlobalScope.launch {
                    val theLocation = locationdao.findByUidAsync(locationUid)
                    val theLocationName = theLocation!!.title

                    if (!clazzList!!.isEmpty()) {
                        val result = recordDao.getAttendanceGroupedByThresholdsWithClazzAndLocation(currentTime,
                        fromDate, toDate,thresholdValues!!.low.toFloat() / 100,
    thresholdValues!!.med.toFloat() / 100,clazzList!!, locationUid)
                        index++
                        buildMapAndUpdateView(theLocationName, result)

                    } else {
                        val result = recordDao.getAttendanceGroupedByThresholdsWithLocation(currentTime,
                            fromDate, toDate,thresholdValues!!.low.toFloat() / 100,
        thresholdValues!!.med.toFloat() / 100,locationUid)
                        index++
                        buildMapAndUpdateView(theLocationName, result)
                    }
                }

            }

        } else {
            val overallLocation = "Overall"
            GlobalScope.launch {
                val result = recordDao.getAttendanceGroupedByThresholdsAndClasses(
                        currentTime, fromDate, toDate, thresholdValues!!.low.toFloat() / 100,
                        thresholdValues!!.med.toFloat() / 100, clazzList!!)
                dataMapsMap[overallLocation] = result
                view.updateTables(dataMapsMap)
            }
        }

    }

    fun dataToCSV() {
        view.generateCSVReport()
    }

}
