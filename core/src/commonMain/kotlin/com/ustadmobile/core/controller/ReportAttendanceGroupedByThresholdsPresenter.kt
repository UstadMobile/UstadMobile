package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao
import com.ustadmobile.core.db.dao.LocationDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.view.ReportAttendanceGroupedByThresholdsView

import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao.AttendanceResultGroupedByAgeAndThreshold
import com.ustadmobile.core.xlsx.UmSheet
import com.ustadmobile.core.xlsx.UmXLSX
import com.ustadmobile.core.xlsx.ZipUtil
import com.ustadmobile.lib.db.entities.Location

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

    private val dataMapsMap: LinkedHashMap<String, List<AttendanceResultGroupedByAgeAndThreshold>>

    internal var repository: UmAppDatabase

    fun dataToXLSX(title: String, xlsxReportPath: String, workingDir: String,
                   tableTextData: List<Array<String>>) {

        try {
            ZipUtil.createEmptyZipFile(xlsxReportPath)

            val umXLSX = UmXLSX(title, xlsxReportPath, workingDir)

            val reportSheet = UmSheet("Report")

            //Loop over tableTextData
            var r = 1
            for (tableTextDatum in tableTextData) {
                var c = 0
                for (i in tableTextDatum.indices) {
                    val value = tableTextDatum[i]
                    reportSheet.addValueToSheet(r, c, value)
                    c++
                }
                r++
            }
            umXLSX.addSheet(reportSheet)

            //Generate the xlsx report from the xlsx object.
            umXLSX.createXLSX()
            view.generateXLSXReport(xlsxReportPath)

        } catch (e: IOException) {
            e.printStackTrace()
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
            fromDate = arguments!!.get(ARG_FROM_DATE)
        }
        if (arguments!!.containsKey(ARG_TO_DATE)) {
            toDate = arguments!!.get(ARG_TO_DATE)
        }

        if (arguments!!.containsKey(ARG_LOCATION_LIST)) {
            val locations = arguments!!.get(ARG_LOCATION_LIST) as LongArray
            locationList = convertLongArray(locations)
        }
        if (arguments!!.containsKey(ARG_CLAZZ_LIST)) {
            val clazzes = arguments!!.get(ARG_CLAZZ_LIST) as LongArray
            clazzList = convertLongArray(clazzes)
        }

        if (arguments!!.containsKey(ARG_THRESHOLD_LOW)) {
            thresholdValues!!.low = arguments!!.get(ARG_THRESHOLD_LOW)
        }
        if (arguments!!.containsKey(ARG_THRESHOLD_MID)) {
            thresholdValues!!.med = arguments!!.get(ARG_THRESHOLD_MID)
        }
        if (arguments!!.containsKey(ARG_THRESHOLD_HIGH)) {
            thresholdValues!!.high = arguments!!.get(ARG_THRESHOLD_HIGH)
        }

        if (arguments!!.containsKey(ARG_GENDER_DISAGGREGATE)) {
            isGenderDisaggregate = arguments!!.get(ARG_GENDER_DISAGGREGATE)
        }

        if (arguments!!.containsKey(ARG_STUDENT_IDENTIFIER_NUMBER)) {
            val numberIdentifier = arguments!!.get(ARG_STUDENT_IDENTIFIER_NUMBER) as Boolean
            if (numberIdentifier) {
                showPercentages = false
            } else {
                showPercentages = true
            }
        }

        if (arguments!!.containsKey(ARG_STUDENT_IDENTIFIER_PERCENTAGE)) {
            val percentageIdentifier = arguments!!.get(ARG_STUDENT_IDENTIFIER_PERCENTAGE) as Boolean
            if (percentageIdentifier) {
                showPercentages = true
            } else {
                showPercentages = false
            }
        }

    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        getDataAndUpdateTable()
    }

    private fun buildMapAndUpdateView(theLocationName: String?,
                                      result: List<AttendanceResultGroupedByAgeAndThreshold>?) {
        dataMapsMap[theLocationName] = result
        if (index >= locationList!!.size) {
            view.updateTables(dataMapsMap)
        }
    }

    /**
     * Separated out method that queries the database and updates the report upon getting and
     * ordering the result.
     */
    private fun getDataAndUpdateTable() {

        val currentTime = System.currentTimeMillis()
        val recordDao = repository.clazzLogAttendanceRecordDao
        val locationdao = repository.locationDao

        //Loop over locations
        if (!locationList!!.isEmpty()) {
            for (locationUid in locationList!!) {

                locationdao.findByUidAsync(locationUid, object : UmCallback<Location> {
                    override fun onSuccess(theLocation: Location?) {
                        val theLocationName = theLocation!!.title

                        if (!clazzList!!.isEmpty()) {
                            recordDao.getAttendanceGroupedByThresholds(currentTime, fromDate, toDate,
                                    thresholdValues!!.low.toFloat() / 100,
                                    thresholdValues!!.med.toFloat() / 100,
                                    clazzList!!, locationUid,
                                    object : UmCallback<List<AttendanceResultGroupedByAgeAndThreshold>> {
                                        override fun onSuccess(result: List<AttendanceResultGroupedByAgeAndThreshold>?) {
                                            index++
                                            buildMapAndUpdateView(theLocationName, result)
                                        }

                                        override fun onFailure(exception: Throwable?) {
                                            print(exception!!.message)
                                        }
                                    })
                        } else {
                            recordDao.getAttendanceGroupedByThresholds(currentTime, fromDate, toDate,
                                    thresholdValues!!.low.toFloat() / 100,
                                    thresholdValues!!.med.toFloat() / 100,
                                    locationUid,
                                    object : UmCallback<List<AttendanceResultGroupedByAgeAndThreshold>> {
                                        override fun onSuccess(result: List<AttendanceResultGroupedByAgeAndThreshold>?) {
                                            index++
                                            buildMapAndUpdateView(theLocationName, result)
                                        }

                                        override fun onFailure(exception: Throwable?) {
                                            print(exception!!.message)
                                        }
                                    })
                        }
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })

            }

        } else {

            val overallLocation = "Overall"
            recordDao.getAttendanceGroupedByThresholdsAndClasses(
                    currentTime, fromDate, toDate, thresholdValues!!.low.toFloat() / 100,
                    thresholdValues!!.med.toFloat() / 100, clazzList!!,
                    object : UmCallback<List<AttendanceResultGroupedByAgeAndThreshold>> {

                        override fun onSuccess(result: List<AttendanceResultGroupedByAgeAndThreshold>?) {
                            dataMapsMap[overallLocation] = result
                            view.updateTables(dataMapsMap)
                        }

                        override fun onFailure(exception: Throwable?) {
                            print(exception!!.message)
                        }
                    })
        }

    }

    fun dataToCSV() {
        view.generateCSVReport()
    }

}
