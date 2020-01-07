package com.ustadmobile.port.android.view


import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ReportNumberOfDaysClassesOpenPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ReportNumberOfDaysClassesOpenView
import com.ustadmobile.core.view.ReportNumberOfDaysClassesOpenView.Companion.BAR_CHART_BAR_COLOR
import com.ustadmobile.core.view.ReportNumberOfDaysClassesOpenView.Companion.BAR_CHART_HEIGHT
import com.ustadmobile.core.view.ReportNumberOfDaysClassesOpenView.Companion.BAR_LABEL
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.io.File
import java.io.FileWriter


/**
 * The ReportNumberOfDaysClassesOpen activity.
 *
 *
 * This Activity extends UstadBaseActivity and implements ReportNumberOfDaysClassesOpenView
 */
class ReportNumberOfDaysClassesOpenActivity : UstadBaseActivity(),
        ReportNumberOfDaysClassesOpenView, PopupMenu.OnMenuItemClickListener {

    private var toolbar: Toolbar? = null

    private lateinit var mPresenter: ReportNumberOfDaysClassesOpenPresenter
    internal lateinit var barChart: BarChart
    internal lateinit var tableLayout: TableLayout

    //Used for exporting
    internal lateinit var tableTextData: MutableList<Array<String?>>

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_report_number_of_days_classes_open)

        //Toolbar:
        toolbar = findViewById(R.id.activity_report_number_of_days_classes_open_toolbar)
        toolbar!!.setTitle(R.string.number_of_days_classes_open)
        setSupportActionBar(toolbar)
        toolbar!!.setTitle(R.string.number_of_days_classes_open)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        tableLayout = findViewById(R.id.activity_report_number_of_days_classes_open_table)

        //Call the Presenter
        mPresenter = ReportNumberOfDaysClassesOpenPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        //eg:
        val fab = findViewById<FloatingTextButton>(R.id.activity_report_number_of_days_classes_open_fab)
        fab.setOnClickListener { v -> showPopup(v) }


    }

    fun setUpChart() {

        val currentLocale = resources.configuration.locale

        toolbar!!.setTitle(R.string.number_of_days_classes_open)

        barChart = findViewById<BarChart>(R.id.activity_report_number_of_days_classes_open_bar_chart)

        barChart.setMinimumHeight(dpToPx(BAR_CHART_HEIGHT))

        val barChartDes = Description()
        barChartDes.setText("")
        barChart.setDescription(barChartDes)

        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)

        barChart.getXAxis().setValueFormatter({ value, axis ->
            val prettyDate = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                    mPresenter!!.barChartTimestamps!![value.toInt()], currentLocale
            )
            prettyDate
        })

        barChart.setTouchEnabled(false)
    }


    fun showPopup(v: View) {
        val popup = PopupMenu(this, v)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.menu_export, popup.menu)
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { this.onMenuItemClick(it) })
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == R.id.menu_export_csv) {
            mPresenter!!.dataToCSV()
            return true
        }
        if (i == R.id.menu_export_xls) {
            startXLSXReportGeneration()
            return true
        }
        return false
    }

    override fun updateBarChart(dataMap: LinkedHashMap<Float, Float>) {

        var hasSomething = false
        mPresenter!!.barChartTimestamps = ArrayList<Long>()
        var index:Float = 0F
        //RENDER HERE

        val barDataEntries = ArrayList<BarEntry>()
        for (nextEntry in dataMap.entries) {
            hasSomething = true
            mPresenter!!.barChartTimestamps!!.add((nextEntry.key * 1000).toLong())
            val anEntry = BarEntry(index as Float, nextEntry.value)
            barDataEntries.add(anEntry)
            index++
        }

        //Create Bar color
        val dataSetBar1 = BarDataSet(barDataEntries, BAR_LABEL)
        dataSetBar1.setValueTextColor(Color.BLACK)
        dataSetBar1.setDrawValues(true)
        dataSetBar1.setColor(Color.parseColor(BAR_CHART_BAR_COLOR))

        val barData = BarData(dataSetBar1)

        val addThese = generateAllViewRowsForTable(dataMap)

        val finalHasSomething = hasSomething
        runOnUiThread {
            setUpChart()
            if (finalHasSomething) {
                barChart.setData(barData)
                barChart.invalidate()
            } else {
                barChart.setData(null)
                barChart.invalidate()
            }

            for (everyRow in addThese) {
                tableLayout.addView(everyRow)
            }

        }
    }

    override fun generateCSVReport() {
        var csvReportFilePath = ""
        //Create the file.

        val dir = filesDir
        val output = File(dir, "number_of_days_classes_open_report_" +
                System.currentTimeMillis() + ".csv")
        csvReportFilePath = output.getAbsolutePath()

            val fileWriter = FileWriter(csvReportFilePath)
            val tableTextdataIterator = tableTextData.iterator()

            while (tableTextdataIterator.hasNext()) {
                var firstDone = false
                val lineArray = tableTextdataIterator.next()
                for (i in lineArray.indices) {
                    if (firstDone) {
                        fileWriter.append(",")
                    }
                    firstDone = true
                    fileWriter.append(lineArray[i])
                }
                fileWriter.append("\n")
            }
            fileWriter.close()

        val applicationId = packageName
        val sharedUri = FileProvider.getUriForFile(this,
                "$applicationId.provider",
                File(csvReportFilePath))
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "*/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, sharedUri)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (shareIntent.resolveActivity(packageManager) != null) {
            startActivity(shareIntent)
        }
    }

    override fun generateXLSXReport(xlsxReportPath: String) {
        val applicationId = packageName
        val sharedUri = FileProvider.getUriForFile(this,
                "$applicationId.provider",
                File(xlsxReportPath))
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "*/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, sharedUri)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (shareIntent.resolveActivity(packageManager) != null) {
            startActivity(shareIntent)
        }
    }

    /**
     * Starts the xlsx report process. Here it crates hte xlsx file.
     */
    private fun startXLSXReportGeneration() {

        val dir = filesDir
        val xlsxReportPath: String

        val title = "number_of_days_classes_open_report_" + System.currentTimeMillis()

        val output = File(dir, "$title.xlsx")
        xlsxReportPath = output.getAbsolutePath()

        val testDir = File(dir, title)
        testDir.mkdir()
        val workingDir = testDir.getAbsolutePath()

        mPresenter.dataToXLSX(title, xlsxReportPath, workingDir, tableTextData)

    }

    fun generateAllViewRowsForTable(dataTableMaps: LinkedHashMap<Float, Float>): List<View> {

        val currentLocale = resources.configuration.locale

        val addThese = arrayListOf<View>()

        //Build a string array of the data
        tableTextData = ArrayList()

        //LAYOUT
        val rowParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)

        val everyItemParam = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
        everyItemParam.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))


        //HEADING
        val headingRow = TableRow(applicationContext)
        headingRow.layoutParams = rowParams

        val dateHeading = TextView(applicationContext)
        dateHeading.setTextColor(Color.BLACK)
        dateHeading.layoutParams = everyItemParam
        dateHeading.setText(R.string.date)

        val valueHeading = TextView(applicationContext)
        valueHeading.setTextColor(Color.BLACK)
        valueHeading.layoutParams = everyItemParam
        valueHeading.setText(R.string.number_of_classes)

        headingRow.addView(dateHeading)
        headingRow.addView(valueHeading)

        //ADD HEADING
        addThese.add(headingRow)

        //MAKE TABLE TEXT DATA:
        val headingItems = arrayOfNulls<String?>(headingRow.childCount)
//        val headingItems = emptyArray()
        for (i in 0 until headingRow.childCount) {
            headingItems[i] = (headingRow.getChildAt(i) as TextView).text.toString()
        }

        //TODO: Check this on KMP :/
        tableTextData!!.add(headingItems)


        val dates = arrayListOf<Any>()
        dates.addAll(dataTableMaps.keys)

        for (everyDate in dates) {
            val everyDateString = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                    (everyDate as Float).toLong()  * 1000, currentLocale)

            val everyDateRow = TableRow(applicationContext)
            everyDateRow.layoutParams = rowParams

            val dateView = TextView(applicationContext)
            dateView.setTextColor(Color.BLACK)
            dateView.layoutParams = everyItemParam
            dateView.text = everyDateString

            val valueView = TextView(applicationContext)
            valueView.setTextColor(Color.BLACK)
            valueView.layoutParams = everyItemParam
            valueView.setText((Math.round(dataTableMaps[everyDate as Float]!!)).toString())

            everyDateRow.addView(dateView)
            everyDateRow.addView(valueView)

            addThese.add(everyDateRow)

            //BUILD TABLE TEXT DATA
            val rowItems = arrayOfNulls<String>(everyDateRow.childCount)
            for (i in 0 until everyDateRow.childCount) {
                rowItems[i] = (everyDateRow.getChildAt(i) as TextView).text.toString()
            }
            //TODO: KMP Check
            tableTextData!!.add(rowItems)
        }

        return addThese

    }

    companion object {

        /**
         * Converts dp to pixels (used in MPAndroid charts)
         *
         * @param dp    dp number
         * @return      pixels number
         */
        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }
    }

}
