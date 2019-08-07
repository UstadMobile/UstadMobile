package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ReportOverallAttendanceView
import com.ustadmobile.core.xlsx.UmSheet
import com.ustadmobile.core.xlsx.UmXLSX
import com.ustadmobile.core.xlsx.ZipUtil
import com.ustadmobile.lib.db.entities.DailyAttendanceNumbers

import com.ustadmobile.core.view.ReportEditView.Companion.ARG_CLAZZ_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_FROM_DATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_GENDER_DISAGGREGATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_LOCATION_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_STUDENT_IDENTIFIER_NUMBER
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_STUDENT_IDENTIFIER_PERCENTAGE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_TO_DATE
import com.ustadmobile.core.view.ReportOverallAttendanceView.Companion.ATTENDANCE_LINE_AVERAGE_LABEL_DESC
import com.ustadmobile.core.view.ReportOverallAttendanceView.Companion.ATTENDANCE_LINE_FEMALE_LABEL_DESC
import com.ustadmobile.core.view.ReportOverallAttendanceView.Companion.ATTENDANCE_LINE_MALE_LABEL_DESC


/**
 * The ReportOverallAttendance Presenter.
 */
class ReportOverallAttendancePresenter(context: Any, arguments: Map<String, String>?,
                                       view: ReportOverallAttendanceView) :
        UstadBaseController<ReportOverallAttendanceView>(context, arguments!!, view) {

    //Any arguments stored as variables here
    //eg: private long clazzUid = -1;
    private var fromDate: Long = 0
    private var toDate: Long = 0
    private var locationList: List<Long>? = null
    private var clazzesList: List<Long>? = null
    var isGenderDisaggregate: Boolean = false
    var showPercentages: Boolean? = false

    internal var dataMaps: LinkedHashMap<String, LinkedHashMap<Float, Float>>

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {

        locationList = ArrayList()
        clazzesList = ArrayList()

        if (arguments!!.containsKey(ARG_FROM_DATE)) {
            fromDate = arguments!!.get(ARG_FROM_DATE)
        }
        if (arguments!!.containsKey(ARG_TO_DATE)) {
            toDate = arguments!!.get(ARG_TO_DATE)
        }
        if (arguments!!.containsKey(ARG_LOCATION_LIST)) {
            val locations = arguments!!.get(ARG_LOCATION_LIST) as LongArray
            //TODO: Get recursive all sub locations as well.
            locationList = convertLongArray(locations)
        }
        if (arguments!!.containsKey(ARG_CLAZZ_LIST)) {
            val clazzes = arguments!!.get(ARG_CLAZZ_LIST) as LongArray
            clazzesList = convertLongArray(clazzes)
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

    private fun processDailyAttendanceNumbers(result: List<DailyAttendanceNumbers>) {
        val lineDataMap = LinkedHashMap<Float, Float>()
        val lineDataMapMale = LinkedHashMap<Float, Float>()
        val lineDataMapFemale = LinkedHashMap<Float, Float>()

        val tableData = LinkedHashMap<String, LinkedHashMap<String, Float>>()

        val tableDataAverage = LinkedHashMap<String, Float>()
        val tableDataMale = LinkedHashMap<String, Float>()
        val tableDataFemale = LinkedHashMap<String, Float>()

        for (everyDayAttendance in result) {

            //Get date and time.
            val dd = everyDayAttendance.logDate

            //Remove time and just get date
            val calendar = Calendar.instance
            calendar.setTimeInMillis(dd)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val d = calendar.getTimeInMillis()

            //Put just date and attendance value
            lineDataMap[d.toFloat() / 1000] = everyDayAttendance.attendancePercentage
            lineDataMapMale[d.toFloat() / 1000] = everyDayAttendance.maleAttendance
            lineDataMapFemale[d.toFloat() / 1000] = everyDayAttendance.femaleAttendance

            tableDataAverage[UMCalendarUtil.getPrettyDateSuperSimpleFromLong(d, Locale.US)] = everyDayAttendance.attendancePercentage * 100
            tableDataMale[UMCalendarUtil.getPrettyDateSuperSimpleFromLong(d, Locale.US)] = everyDayAttendance.maleAttendance * 100
            tableDataFemale[UMCalendarUtil.getPrettyDateSuperSimpleFromLong(d, Locale.US)] = everyDayAttendance.femaleAttendance * 100

        }

        tableData[ATTENDANCE_LINE_AVERAGE_LABEL_DESC] = tableDataAverage
        tableData[ATTENDANCE_LINE_MALE_LABEL_DESC] = tableDataMale
        tableData[ATTENDANCE_LINE_FEMALE_LABEL_DESC] = tableDataFemale


        dataMaps = LinkedHashMap()

        if (isGenderDisaggregate) {
            dataMaps[ATTENDANCE_LINE_MALE_LABEL_DESC] = lineDataMapMale
            dataMaps[ATTENDANCE_LINE_FEMALE_LABEL_DESC] = lineDataMapFemale
            dataMaps[ATTENDANCE_LINE_AVERAGE_LABEL_DESC] = lineDataMap
        } else {
            dataMaps[ATTENDANCE_LINE_AVERAGE_LABEL_DESC] = lineDataMap
        }

        view.updateAttendanceMultiLineChart(dataMaps, tableData)
    }

    fun getAttendanceDataAndUpdateCharts() {


        val attendanceRecordDao = repository.clazzLogAttendanceRecordDao

        attendanceRecordDao.findOverallDailyAttendanceNumbersByDateAndStuff(fromDate, toDate,
                clazzesList!!, locationList!!, object : UmCallback<List<DailyAttendanceNumbers>> {

            override fun onSuccess(result: List<DailyAttendanceNumbers>?) {
                processDailyAttendanceNumbers(result!!)
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })

    }

    fun dataToCSV() {
        view.generateCSVReport()
    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        getAttendanceDataAndUpdateCharts()
    }


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

    companion object {

        fun convertLongArray(array: LongArray): ArrayList<Long> {
            val result = ArrayList<Long>(array.size)
            for (item in array)
                result.add(item)
            return result
        }

        fun convertLongList(list: List<Long>): Array<Long> {
            val array = arrayOfNulls<Long>(list.size)
            var i = 0
            for (everyList in list) {
                array[i] = everyList
                i++
            }
            return array
        }
    }
}
