package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.ArgumentUtil
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_CLAZZ_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_FROM_DATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_LOCATION_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_TO_DATE
import com.ustadmobile.core.view.ReportNumberOfDaysClassesOpenView
import com.ustadmobile.staging.core.xlsx.UmSheet
import com.ustadmobile.staging.core.xlsx.UmXLSX
import com.ustadmobile.staging.core.xlsx.ZipUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * The ReportNumberOfDaysClassesOpen Presenter.
 */
class ReportNumberOfDaysClassesOpenPresenter(context: Any, arguments: Map<String, String>?,
                                             view: ReportNumberOfDaysClassesOpenView) :
        UstadBaseController<ReportNumberOfDaysClassesOpenView>(context, arguments!!, view) {

    private var fromDate: Long = 0
    private var toDate: Long = 0
    private var clazzList: List<Long>? = null
    private var locationList: List<Long>? = null
    var barChartTimestamps: ArrayList<Long>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {

        clazzList = ArrayList()
        locationList = ArrayList()

        if (arguments!!.containsKey(ARG_FROM_DATE)) {
            fromDate = arguments!!.get(ARG_FROM_DATE)!!.toLong()
        }
        if (arguments!!.containsKey(ARG_TO_DATE)) {
            toDate = arguments!!.get(ARG_TO_DATE)!!.toLong()
        }
        if (arguments!!.containsKey(ARG_LOCATION_LIST)) {
            //TODOne: String CSV to List
            locationList = ArgumentUtil.convertCSVStringToLongList(
                    arguments!!.get(ARG_LOCATION_LIST)!!.toString())

        }
        if (arguments!!.containsKey(ARG_CLAZZ_LIST)) {
            //TODOne: String CSV to List
            clazzList = ArgumentUtil.convertCSVStringToLongList(
                    arguments!!.get(ARG_CLAZZ_LIST)!!.toString())
        }

    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        getNumberOfDaysOpenDataAndUpdateCharts()
    }

    /**
     * Separated out method that queries the database and updates the report upon getting and
     * ordering the result.
     */
    private fun getNumberOfDaysOpenDataAndUpdateCharts() {

        val dataMap = LinkedHashMap<Float, Float>()
        GlobalScope.launch {
            val resultList = repository.clazzLogDao.getNumberOfClassesOpenForDateClazzes(fromDate,
                    toDate, clazzList!!, locationList!!)
            for (everyResult in resultList!!) {
                dataMap[everyResult.date / 1000f] = everyResult.number.toFloat()
            }
            //Update the report data on the view:
            view.updateBarChart(dataMap)
        }
    }


    fun dataToCSV() {
        view.generateCSVReport()
    }

    val z = ZipUtil()

    fun dataToXLSX(title: String, xlsxReportPath: String, theWorkingPath: String,
                   tableTextData: MutableList<Array<String?>>) {

        try {
            z.createEmptyZipFile(xlsxReportPath)

            val umXLSX = UmXLSX(title, xlsxReportPath, theWorkingPath)

            val reportSheet = UmSheet("Report")
            reportSheet.addValueToSheet(0, 0, "Date")
            reportSheet.addValueToSheet(0, 1, "Number of classes")


            tableTextData.removeAt(0)
            /*
                Single Sheet
                Date     | Classes
                27/May/19| 42

             */
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

        } catch (e: Exception) {
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
    }
}
