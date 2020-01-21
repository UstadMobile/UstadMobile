package com.ustadmobile.staging.port.android.view


import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ReportOverallAttendancePresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ReportOverallAttendanceView
import com.ustadmobile.core.view.ReportOverallAttendanceView.Companion.ATTENDANCE_LINE_AVERAGE_LABEL_DESC
import com.ustadmobile.core.view.ReportOverallAttendanceView.Companion.ATTENDANCE_LINE_CHART_COLOR_AVERAGE
import com.ustadmobile.core.view.ReportOverallAttendanceView.Companion.ATTENDANCE_LINE_CHART_COLOR_FEMALE
import com.ustadmobile.core.view.ReportOverallAttendanceView.Companion.ATTENDANCE_LINE_CHART_COLOR_MALE
import com.ustadmobile.core.view.ReportOverallAttendanceView.Companion.ATTENDANCE_LINE_CHART_HEIGHT
import com.ustadmobile.core.view.ReportOverallAttendanceView.Companion.ATTENDANCE_LINE_FEMALE_LABEL_DESC
import com.ustadmobile.core.view.ReportOverallAttendanceView.Companion.ATTENDANCE_LINE_MALE_LABEL_DESC
import com.ustadmobile.port.android.view.UstadBaseActivity
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*


/**
 * The ReportOverallAttendance activity.
 *
 *
 * This Activity extends UstadBaseActivity and implements ReportOverallAttendanceView
 */
class ReportOverallAttendanceActivity : UstadBaseActivity(), ReportOverallAttendanceView, PopupMenu.OnMenuItemClickListener {

    //RecyclerView
    private var mPresenter: ReportOverallAttendancePresenter? = null
    internal lateinit var lineChart: LineChart
    internal lateinit var tableLayout: TableLayout

    /**
     * Used to construct the export report (has line by line information)
     */
    internal lateinit var tableTextData: MutableList<Array<String?>>

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_report_overall_attendance)

        //Toolbar:
        val toolbar = findViewById<Toolbar>(R.id.activity_report_overall_attendance_toolbar)
        toolbar.setTitle(R.string.overall_attendance_report)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        tableLayout = findViewById(R.id.activity_report_overall_attendance_table)

        //Call the Presenter
        mPresenter = ReportOverallAttendancePresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        //eg:
        val fab = findViewById<FloatingTextButton>(R.id.activity_report_overall_attendance_fab)
        fab.setOnClickListener { this.showPopup(it) }


    }

    fun showPopup(v: View) {
        val popup = PopupMenu(this, v)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.menu_export, popup.menu)
        popup.setOnMenuItemClickListener(this)
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
        } else {
            return false
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

        val title = "overall_attendance_activity_" + System.currentTimeMillis()

        val output = File(dir, "$title.xlsx")
        xlsxReportPath = output.absolutePath

        val testDir = File(dir, title)
        testDir.mkdir()
        val workingDir = testDir.absolutePath

        mPresenter!!.dataToXLSX(title, xlsxReportPath, workingDir, tableTextData)

    }


    /**
     * Handles what happens when toolbar menu option selected. Here it is handling what happens when
     * back button is pressed.
     *
     * @param item  The item selected.
     * @return      true if accounted for.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Sets up charts UI elements on activity start. Sets legend, colors, size and axis look.
     */
    fun setUpCharts() {

        val currentLocale = resources.configuration.locale

        //Get the chart view
        lineChart = findViewById(R.id.activity_report_overall_attendance_line_chart)
        lineChart.minimumHeight = dpToPx(ATTENDANCE_LINE_CHART_HEIGHT)

        val description = Description()
        lineChart.description = description

        lineChart.axisLeft.setValueFormatter { value, axis -> value.toInt().toString() + "%" }
        lineChart.xAxis.setValueFormatter { value, axis -> value.toInt().toString() + "" }
        lineChart.setTouchEnabled(false)
        lineChart.xAxis.setLabelCount(4, true)

        lineChart.xAxis.setValueFormatter { value, axis ->
            UMCalendarUtil.getPrettyDateSuperSimpleFromLong(value.toLong() * 1000,
                    currentLocale)
        }
    }

    /**
     * Generates Views for the report data provided to it
     * @param valueIdentifier   Depends on if the data is in numbers or in percentage. This will be
     * the '%' character if the raw report data is counted in percentages
     * or blank '' if just in numbers.
     * @param dataTableMaps     The report's raw data from the presenter
     * @return                  A list of Views.
     */
    fun generateAllViewRowsForTable(valueIdentifier: String,
                                    dataTableMaps: LinkedHashMap<String, LinkedHashMap<String, Float>>): List<View> {
        //Build a string array of the data
        tableTextData = ArrayList()

        //RETURN THIS LIST OF VIEWS
        val addThese = ArrayList<View>()

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

        val averageHeading = TextView(applicationContext)
        averageHeading.setTextColor(Color.BLACK)
        averageHeading.layoutParams = everyItemParam
        averageHeading.setText(R.string.average)

        headingRow.addView(dateHeading)
        headingRow.addView(averageHeading)

        if (mPresenter!!.isGenderDisaggregate) {

            val maleHeading = TextView(applicationContext)
            maleHeading.setTextColor(Color.BLACK)
            maleHeading.layoutParams = everyItemParam
            maleHeading.setText(R.string.male)

            val femaleHeading = TextView(applicationContext)
            femaleHeading.setTextColor(Color.BLACK)
            femaleHeading.layoutParams = everyItemParam
            femaleHeading.setText(R.string.female)

            headingRow.addView(maleHeading)
            headingRow.addView(femaleHeading)
        }

        val tableDataAverage: LinkedHashMap<String, Float>?
        tableDataAverage = dataTableMaps[ATTENDANCE_LINE_AVERAGE_LABEL_DESC]

        assert(tableDataAverage != null)
        if (tableDataAverage!!.size > 0) {   //ie: if there is any data
            addThese.add(headingRow)
        }

        //MAKE TABLE TEXT DATA:
        val headingItems = arrayOfNulls<String>(headingRow.childCount)
        for (i in 0 until headingRow.childCount) {
            headingItems[i] = (headingRow.getChildAt(i) as TextView).text.toString()
        }
        tableTextData.add(headingItems)


        //DATA ROWS
        var tableDataMale: LinkedHashMap<String, Float>? = LinkedHashMap()
        var tableDataFemale: LinkedHashMap<String, Float>? = LinkedHashMap()


        if (mPresenter!!.isGenderDisaggregate) {
            tableDataMale = dataTableMaps[ATTENDANCE_LINE_MALE_LABEL_DESC]
            tableDataFemale = dataTableMaps[ATTENDANCE_LINE_FEMALE_LABEL_DESC]
        }

        val dates = ArrayList(tableDataAverage.keys)
        for (every_date in dates) {
            val iRow = TableRow(applicationContext)
            iRow.layoutParams = rowParams

            val dateView = TextView(applicationContext)
            dateView.setTextColor(Color.BLACK)
            dateView.layoutParams = everyItemParam
            dateView.text = every_date

            val averageP = tableDataAverage[every_date]
            val averageView = TextView(applicationContext)
            averageView.setTextColor(Color.BLACK)
            averageView.layoutParams = everyItemParam
            val averageViewText = averageP.toString() + valueIdentifier
            averageView.text = averageViewText

            iRow.addView(dateView)
            iRow.addView(averageView)


            if (mPresenter!!.isGenderDisaggregate) {
                assert(tableDataMale != null)
                val maleP = tableDataMale!![every_date]
                val maleView = TextView(applicationContext)
                maleView.setTextColor(Color.BLACK)
                maleView.layoutParams = everyItemParam
                val maleViewString = maleP.toString() + valueIdentifier
                maleView.text = maleViewString

                assert(tableDataFemale != null)
                val femaleP = tableDataFemale!![every_date]
                val femaleView = TextView(applicationContext)
                femaleView.setTextColor(Color.BLACK)
                femaleView.layoutParams = everyItemParam
                val femaleViewString = femaleP.toString() + valueIdentifier
                femaleView.text = femaleViewString

                iRow.addView(maleView)
                iRow.addView(femaleView)
            }

            addThese.add(iRow)

            //BUILD TABLE TEXT DATA
            val rowItems = arrayOfNulls<String>(iRow.childCount)
            for (i in 0 until iRow.childCount) {
                rowItems[i] = (iRow.getChildAt(i) as TextView).text.toString()
            }
            tableTextData.add(rowItems)
        }

        //RETURN LIST OF ROW VIEWS
        return addThese
    }


    override fun updateAttendanceMultiLineChart(dataMaps: LinkedHashMap<String, LinkedHashMap<Float, Float>>,
                                                tableDataMap: LinkedHashMap<String,
                                                        LinkedHashMap<String, Float>>) {


        val lineData = LineData()
        var hasSomething = false

        var valueIdentifier = ""

        if (mPresenter!!.showPercentages!!) {
            valueIdentifier = "%"
        }

        //Generate and draw lines for line chart
        for ((dataSetType, dataMap) in dataMaps) {


            val labelDesc: String
            val labelColor: String
            when (dataSetType) {
                ATTENDANCE_LINE_MALE_LABEL_DESC -> {
                    labelDesc = ATTENDANCE_LINE_MALE_LABEL_DESC
                    labelColor = ATTENDANCE_LINE_CHART_COLOR_MALE
                }
                ATTENDANCE_LINE_FEMALE_LABEL_DESC -> {
                    labelDesc = ATTENDANCE_LINE_FEMALE_LABEL_DESC
                    labelColor = ATTENDANCE_LINE_CHART_COLOR_FEMALE
                }
                ATTENDANCE_LINE_AVERAGE_LABEL_DESC -> {
                    labelDesc = ATTENDANCE_LINE_AVERAGE_LABEL_DESC
                    labelColor = ATTENDANCE_LINE_CHART_COLOR_AVERAGE
                }
                else -> {
                    labelDesc = "-"
                    labelColor = "#000000"
                }
            }

            val lineDataEntries = ArrayList<Entry>()
            for ((key, value) in dataMap) {
                hasSomething = true
                val anEntry = Entry()
                anEntry.x = key
                anEntry.y = value * 100
                lineDataEntries.add(anEntry)
            }

            //Create a line data set (one line)
            val dataSetLine1 = LineDataSet(lineDataEntries, labelDesc)
            dataSetLine1.color = Color.parseColor(labelColor)
            dataSetLine1.valueTextColor = Color.BLACK
            //Don't want to see the values on the data points.
            dataSetLine1.setDrawValues(false)
            //Don't want to see the circles
            dataSetLine1.setDrawCircles(false)

            lineData.addDataSet(dataSetLine1)

        }

        val addThese = generateAllViewRowsForTable(valueIdentifier, tableDataMap)

        //Update the lineChart on the UI thread (since this method is called via the Presenter)
        val finalHasSomething = hasSomething
        runOnUiThread {
            setUpCharts()
            if (finalHasSomething) {
                lineChart.data = lineData
                lineChart.invalidate()
            } else {
                lineChart.data = null
                lineChart.invalidate()
            }

            for (everyRow in addThese) {
                tableLayout.addView(everyRow)

            }

        }


    }

    override fun generateCSVReport() {

        val csvReportFilePath: String
        //Create the file.

        val dir = filesDir
        val output = File(dir, "overall_attendance_activity_" +
                System.currentTimeMillis() + ".csv")
        csvReportFilePath = output.absolutePath

        try {
            val fileWriter = FileWriter(csvReportFilePath)

            for (aTableTextData in tableTextData) {
                var firstDone = false
                for (aLineArray in aTableTextData) {
                    if (firstDone) {
                        fileWriter.append(",")
                    }
                    firstDone = true
                    fileWriter.append(aLineArray)
                }
                fileWriter.append("\n")
            }
            fileWriter.close()


        } catch (e: IOException) {
            e.printStackTrace()
        }

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
