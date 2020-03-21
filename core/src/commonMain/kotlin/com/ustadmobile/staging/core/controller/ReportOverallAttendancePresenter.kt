package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.ArgumentUtil
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_CLAZZ_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_FROM_DATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_GENDER_DISAGGREGATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_LOCATION_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_STUDENT_IDENTIFIER_NUMBER
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_STUDENT_IDENTIFIER_PERCENTAGE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_TO_DATE
import com.ustadmobile.core.view.ReportOverallAttendanceView
import com.ustadmobile.core.view.ReportOverallAttendanceView.Companion.ATTENDANCE_LINE_AVERAGE_LABEL_DESC
import com.ustadmobile.core.view.ReportOverallAttendanceView.Companion.ATTENDANCE_LINE_FEMALE_LABEL_DESC
import com.ustadmobile.core.view.ReportOverallAttendanceView.Companion.ATTENDANCE_LINE_MALE_LABEL_DESC
import com.ustadmobile.staging.core.xlsx.UmSheet
import com.ustadmobile.staging.core.xlsx.UmXLSX
import com.ustadmobile.staging.core.xlsx.ZipUtil
import com.ustadmobile.lib.db.entities.DailyAttendanceNumbers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.IOException


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

    internal lateinit var dataMaps: LinkedHashMap<String, LinkedHashMap<Float, Float>>

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {

        locationList = ArrayList()
        clazzesList = ArrayList()

        if (arguments!!.containsKey(ARG_FROM_DATE)) {
            fromDate = arguments!!.get(ARG_FROM_DATE)!!.toLong()
        }
        if (arguments!!.containsKey(ARG_TO_DATE)) {
            toDate = arguments!!.get(ARG_TO_DATE)!!.toLong()
        }
        if (arguments!!.containsKey(ARG_LOCATION_LIST)) {
            //TODO: Get recursive all sub locations as well.
            locationList = ArgumentUtil.convertCSVStringToLongList(arguments!!.get(ARG_LOCATION_LIST)!!)
        }
        if (arguments!!.containsKey(ARG_CLAZZ_LIST)) {
            clazzesList = ArgumentUtil.convertCSVStringToLongList(arguments!!.get(ARG_CLAZZ_LIST)!!)
        }
        if (arguments!!.containsKey(ARG_GENDER_DISAGGREGATE)) {
            isGenderDisaggregate = arguments!!.get(ARG_GENDER_DISAGGREGATE)!!.toBoolean()
        }

        if (arguments!!.containsKey(ARG_STUDENT_IDENTIFIER_NUMBER)) {
            val numberIdentifier = arguments!!.get(ARG_STUDENT_IDENTIFIER_NUMBER)!!.toBoolean()
            if (numberIdentifier) {
                showPercentages = false
            } else {
                showPercentages = true
            }
        }

        if (arguments!!.containsKey(ARG_STUDENT_IDENTIFIER_PERCENTAGE)) {
            val percentageIdentifier = arguments!!.get(ARG_STUDENT_IDENTIFIER_PERCENTAGE)!!.toBoolean()
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

            //TODOne: Fix this or move it to UMCalendarUtil.
            //Remove time and just get date

            val d = UMCalendarUtil.zeroOutTimeForGivenLongDate(dd)

            //Put just date and attendance value
            lineDataMap[d.toFloat() / 1000] = everyDayAttendance.attendancePercentage
            lineDataMapMale[d.toFloat() / 1000] = everyDayAttendance.maleAttendance
            lineDataMapFemale[d.toFloat() / 1000] = everyDayAttendance.femaleAttendance

            //TODO: KMP Locale replace.
            val localeUS = "localUS"
            tableDataAverage[UMCalendarUtil.getPrettyDateSuperSimpleFromLong(d, localeUS)] = everyDayAttendance.attendancePercentage * 100
            tableDataMale[UMCalendarUtil.getPrettyDateSuperSimpleFromLong(d, localeUS)] = everyDayAttendance.maleAttendance * 100
            tableDataFemale[UMCalendarUtil.getPrettyDateSuperSimpleFromLong(d, localeUS)] = everyDayAttendance.femaleAttendance * 100

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
        GlobalScope.launch {
            val result = attendanceRecordDao.findOverallDailyAttendanceNumbersByDateAndStuff(fromDate,
                    toDate, clazzesList!!, locationList!!)
            processDailyAttendanceNumbers(result!!)
        }

    }

    fun dataToCSV() {
        view.generateCSVReport()
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        getAttendanceDataAndUpdateCharts()
    }

    val z = ZipUtil()

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

    companion object {

        fun convertLongArray(array: LongArray): ArrayList<Long> {
            val result = ArrayList<Long>(array.size)
            for (item in array)
                result.add(item)
            return result
        }

        fun convertLongList(list: List<Long>): Array<Long?> {
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
