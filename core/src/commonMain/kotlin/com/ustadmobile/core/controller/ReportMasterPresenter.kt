package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.view.ReportMasterView
import com.ustadmobile.core.xlsx.UmSheet
import com.ustadmobile.core.xlsx.UmXLSX
import com.ustadmobile.core.xlsx.ZipUtil
import com.ustadmobile.lib.db.entities.ReportMasterItem

import com.ustadmobile.core.controller.ReportOverallAttendancePresenter.Companion.convertLongArray
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_CLAZZ_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_FROM_DATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_GENDER_DISAGGREGATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_LOCATION_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_TO_DATE

class ReportMasterPresenter(context: Any, arguments: Map<String, String>?, view:
ReportMasterView) : UstadBaseController<ReportMasterView>(context, arguments!!, view) {

    private var fromDate: Long = 0
    private var toDate: Long = 0
    private var clazzList: List<Long>? = null
    private var locationList: List<Long>? = null
    private var genderDisaggregated = false

    private val dataMap: List<ReportMasterItem>

    internal var repository: UmAppDatabase


    init {
        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        dataMap = ArrayList()
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

        if (arguments!!.containsKey(ARG_GENDER_DISAGGREGATE)) {
            genderDisaggregated = arguments!!.get(ARG_GENDER_DISAGGREGATE)
        }

    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        getDataAndUpdateTable()
    }

    /**
     * Separated out method that queries the database and updates the report upon getting and
     * ordering the result.
     */
    private fun getDataAndUpdateTable() {

        val currentTime = System.currentTimeMillis()

        val attendanceRecordDao = repository.clazzLogAttendanceRecordDao

        attendanceRecordDao.findMasterReportDataForAllAsync(fromDate, toDate,
                object : UmCallback<List<ReportMasterItem>> {
                    override fun onSuccess(result: List<ReportMasterItem>?) {
                        view.runOnUiThread({ view.updateTables(result!!) })
                    }

                    override fun onFailure(exception: Throwable?) {

                    }
                })


    }

    fun dataToXLSX(title: String, xlsxReportPath: String, workingDir: String,
                   tableTextData: MutableList<Array<String>>) {

        try {
            ZipUtil.createEmptyZipFile(xlsxReportPath)

            val umXLSX = UmXLSX(title, xlsxReportPath, workingDir)

            val reportSheet = UmSheet("Report")
            reportSheet.addValueToSheet(0, 0, "Class ID")
            reportSheet.addValueToSheet(0, 1, "First name")
            reportSheet.addValueToSheet(0, 2, "Last name")
            reportSheet.addValueToSheet(0, 3, "Student ID")
            reportSheet.addValueToSheet(0, 4, "Number days present")
            reportSheet.addValueToSheet(0, 5, "Number absent")
            reportSheet.addValueToSheet(0, 6, "Number partial")
            reportSheet.addValueToSheet(0, 7, "Total class days")
            reportSheet.addValueToSheet(0, 8, "Date left")
            reportSheet.addValueToSheet(0, 9, "Active")
            reportSheet.addValueToSheet(0, 10, "Gender")
            reportSheet.addValueToSheet(0, 11, "Birthday")

            //Remove already put headers
            tableTextData.removeAt(0)

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
}
