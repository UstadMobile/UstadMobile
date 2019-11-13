package com.ustadmobile.port.android.view


import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ReportAttendanceGroupedByThresholdsPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ReportAttendanceGroupedByThresholdsView
import com.ustadmobile.lib.db.entities.AttendanceResultGroupedByAgeAndThreshold
import com.ustadmobile.lib.db.entities.Person
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*


/**
 * The ReportNumberOfDaysClassesOpen activity.
 *
 *
 * This Activity extends UstadBaseActivity and implements ReportNumberOfDaysClassesOpenView
 */
class ReportAttendanceGroupedByThresholdsActivity : UstadBaseActivity(),
        ReportAttendanceGroupedByThresholdsView, PopupMenu.OnMenuItemClickListener {

    private var toolbar: Toolbar? = null
    private var reportLinearLayout: LinearLayout? = null
    private var mPresenter: ReportAttendanceGroupedByThresholdsPresenter? = null

    /**
     * Used to construct the export report (has line by line information)
     */
    internal lateinit var tableTextData: MutableList<Array<String?>>


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_report_attendance_grouped_by_thresholds)

        //Toolbar:
        toolbar = findViewById(R.id.activity_report_attendance_grouped_by_thresholds_toolbar)
        toolbar!!.setTitle(R.string.attendance_grouped_by_threshold)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        reportLinearLayout = findViewById(R.id.actvity_report_attendance_grouped_by_thresholds_ll)

        //Call the Presenter
        mPresenter = ReportAttendanceGroupedByThresholdsPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        //eg:
        val fab = findViewById<FloatingTextButton>(R.id.activity_report_attendance_grouped_by_thresholds_fab)
        fab.setOnClickListener { v -> showPopup(v) }


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

        val title = "report_attendance_grouped_by_threshold_" + System.currentTimeMillis()

        val output = File(dir, "$title.xlsx")
        xlsxReportPath = output.absolutePath

        val testDir = File(dir, title)
        testDir.mkdir()
        val workingDir = testDir.absolutePath

        mPresenter!!.dataToXLSX(title, xlsxReportPath, workingDir, tableTextData)

    }


    override fun updateTables(dataMaps: LinkedHashMap<String, List<AttendanceResultGroupedByAgeAndThreshold>>) {

        //Build a string array of the data
        tableTextData = ArrayList()

        for (locationName in dataMaps.keys) {
            val dataMapList = dataMaps[locationName]!!
            if (!dataMapList.isEmpty()) {

                //Add title to tableTextData
                val titleItems = arrayOf<String?>(locationName)
                tableTextData.add(titleItems)

                val addThese = generateAllViewRowsForTable(dataMapList)

                //heading
                val heading = TextView(applicationContext)
                heading.setTextColor(Color.BLACK)
                heading.setTypeface(null, Typeface.BOLD)
                heading.setText(locationName)

                val tableLayout = TableLayout(applicationContext)

                runOnUiThread {

                    for (everyRow in addThese) {
                        tableLayout.addView(everyRow)

                    }

                    reportLinearLayout!!.addView(heading)
                    reportLinearLayout!!.addView(tableLayout)

                }

            }
        }

    }

    override fun generateCSVReport() {

        var csvReportFilePath = ""
        //Create the file.

        val dir = filesDir
        val output = File(dir, "report_attendance_grouped_by_threshold_" +
                System.currentTimeMillis() + ".csv")
        csvReportFilePath = output.absolutePath

        try {
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

    fun generateAllViewRowsForTable(dataMapList: List<AttendanceResultGroupedByAgeAndThreshold>): List<View> {

        val addThese = ArrayList<View>()

        //LAYOUT
        val rowParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)

        val everyItemParam = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
        everyItemParam.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
        everyItemParam.gravity = Gravity.CENTER_VERTICAL


        //HEADING
        val superHeadingRow = TableRow(applicationContext)
        superHeadingRow.layoutParams = rowParams

        superHeadingRow.addView(TextView(applicationContext))

        val attendanceLowHeading = TextView(applicationContext)
        var params = superHeadingRow.layoutParams as TableRow.LayoutParams
        params.span = 2
        attendanceLowHeading.layoutParams = params
        attendanceLowHeading.textSize = 10f
        attendanceLowHeading.setTextColor(Color.BLACK)
        attendanceLowHeading.text = getText(R.string.attendance).toString() + " >" + mPresenter!!.thresholdValues!!.low + "%"


        val attendanceMidHeading = TextView(applicationContext)
        params = superHeadingRow.layoutParams as TableRow.LayoutParams
        params.span = 2
        attendanceMidHeading.layoutParams = params
        attendanceMidHeading.textSize = 10f
        attendanceMidHeading.setTextColor(Color.BLACK)
        attendanceMidHeading.text = getText(R.string.attendance).toString() + " >" + mPresenter!!.thresholdValues!!.med + "%"

        val attendanceHighHeading = TextView(applicationContext)
        params = superHeadingRow.layoutParams as TableRow.LayoutParams
        params.span = 2
        attendanceHighHeading.layoutParams = params
        attendanceHighHeading.textSize = 10f
        attendanceHighHeading.setTextColor(Color.BLACK)
        attendanceHighHeading.text = getText(R.string.attendance).toString() + " >" + mPresenter!!.thresholdValues!!.high + "%"


        superHeadingRow.addView(attendanceLowHeading)
        superHeadingRow.addView(attendanceMidHeading)
        superHeadingRow.addView(attendanceHighHeading)

        addThese.add(superHeadingRow)

        //Super heading row to tableTextData
        val superHeadingItems = arrayOfNulls<String?>(superHeadingRow.childCount + 3)
        var j = 0
        for (i in 0 until superHeadingRow.childCount) {
            val addThis = (superHeadingRow.getChildAt(i) as TextView).text.toString()
            superHeadingItems[j] = addThis

            if (addThis.startsWith(getText(R.string.attendance).toString())) {
                j++
                superHeadingItems[j] = ""
            }
            j++
        }
        tableTextData.add(superHeadingItems)

        val headingRow = TableRow(applicationContext)
        headingRow.layoutParams = rowParams

        val ageHeading = TextView(applicationContext)
        ageHeading.setTextColor(Color.BLACK)
        ageHeading.layoutParams = everyItemParam
        ageHeading.setText(R.string.age)

        val maleLowHeading = TextView(applicationContext)
        maleLowHeading.setTextColor(Color.BLACK)
        maleLowHeading.layoutParams = everyItemParam
        maleLowHeading.setText(R.string.male)

        val femaleLowHeading = TextView(applicationContext)
        femaleLowHeading.setTextColor(Color.BLACK)
        femaleLowHeading.layoutParams = everyItemParam
        femaleLowHeading.setText(R.string.female)

        val maleMidHeading = TextView(applicationContext)
        maleMidHeading.setTextColor(Color.BLACK)
        maleMidHeading.layoutParams = everyItemParam
        maleMidHeading.setText(R.string.male)

        val femaleMidHeading = TextView(applicationContext)
        femaleMidHeading.setTextColor(Color.BLACK)
        femaleMidHeading.layoutParams = everyItemParam
        femaleMidHeading.setText(R.string.female)

        val maleHighHeading = TextView(applicationContext)
        maleHighHeading.setTextColor(Color.BLACK)
        maleHighHeading.layoutParams = everyItemParam
        maleHighHeading.setText(R.string.male)

        val femaleHighHeading = TextView(applicationContext)
        femaleHighHeading.setTextColor(Color.BLACK)
        femaleHighHeading.layoutParams = everyItemParam
        femaleHighHeading.setText(R.string.female)


        if (!mPresenter!!.isGenderDisaggregate) {
            maleLowHeading.setText(R.string.average)
            maleMidHeading.setText(R.string.average)
            maleHighHeading.setText(R.string.average)

            femaleLowHeading.text = ""
            femaleMidHeading.text = ""
            femaleHighHeading.text = ""
        }

        //Add all individual headings to the heading row.
        headingRow.addView(ageHeading)
        headingRow.addView(maleLowHeading)
        headingRow.addView(femaleLowHeading)
        headingRow.addView(maleMidHeading)
        headingRow.addView(femaleMidHeading)
        headingRow.addView(maleHighHeading)
        headingRow.addView(femaleHighHeading)

        //ADD HEADING ROW to the View to return
        addThese.add(headingRow)

        //heading row to tableTextData
        val headingItems = arrayOfNulls<String>(headingRow.childCount)
        for (i in 0 until headingRow.childCount) {
            headingItems[i] = (headingRow.getChildAt(i) as TextView).text.toString()
        }
        tableTextData.add(headingItems)

        //Horizontal line
        val v = View(this)
        val hlineParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, 2)
        v.setBackgroundColor(Color.GRAY)
        v.layoutParams = hlineParams
        addThese.add(v)


        val ages = ArrayList<Int>()

        val maleLowMap = HashMap<Int, Int>()
        val femaleLowMap = HashMap<Int, Int>()
        val maleMidMap = HashMap<Int, Int>()
        val femaleMidMap = HashMap<Int, Int>()
        val maleHighMap = HashMap<Int, Int>()
        val femaleHighMap = HashMap<Int, Int>()

        for (every_result in dataMapList) {
            //Get ages
            ages.add(every_result.age)

            if (every_result.thresholdGroup!!.toLowerCase().equals("high")) {
                if (every_result.gender === Person.GENDER_MALE) {
                    maleHighMap[every_result.age] = every_result.total
                } else if (every_result.gender === Person.GENDER_FEMALE) {
                    femaleHighMap[every_result.age] = every_result.total
                }
            }
            if (every_result.thresholdGroup!!.toLowerCase().equals("medium")) {
                if (every_result.gender === Person.GENDER_MALE) {
                    maleMidMap[every_result.age] = every_result.total
                } else if (every_result.gender === Person.GENDER_FEMALE) {
                    femaleMidMap[every_result.age] = every_result.total
                }
            }
            if (every_result.thresholdGroup!!.toLowerCase().equals("low")) {
                if (every_result.gender === Person.GENDER_MALE) {
                    maleLowMap[every_result.age] = every_result.total
                } else if (every_result.gender === Person.GENDER_FEMALE) {
                    femaleLowMap[every_result.age] = every_result.total
                }
            }

        }

        //remove duplicates on ages
        val hs = HashSet<Int>()
        hs.addAll(ages)
        ages.clear()
        ages.addAll(hs)
        //Sort ages
        Collections.sort(ages)


        for (age in ages) {
            var totalMaleStudentsAtThisAge = 0
            var totalFemaleStudentsAtThisAge = 0
            var totalStudentsAtThisAge = 0
            val everyAgeRow = TableRow(applicationContext)
            everyAgeRow.layoutParams = rowParams

            val ageView = TextView(applicationContext)
            ageView.setTextColor(Color.BLACK)
            ageView.layoutParams = everyItemParam
            ageView.text = age.toString()

            val maleLowView: TextView
            val femaleLowView: TextView
            val maleMidView: TextView
            val femaleMidView: TextView
            val maleHighView: TextView
            val femaleHighView: TextView

            var maleLow = 0f
            var maleMid = 0f
            var maleHigh = 0f
            var femaleLow = 0f
            var femaleMid = 0f
            var femaleHigh = 0f

            maleLowView = TextView(applicationContext)
            maleLowView.setTextColor(Color.BLACK)
            maleLowView.layoutParams = everyItemParam
            if (maleLowMap.containsKey(age)) {
                maleLowView.text = maleLowMap[age].toString()
                totalMaleStudentsAtThisAge += maleLowMap[age]!!
                maleLow = maleLowMap[age]!!.toFloat()
            } else
                maleLowView.text = "0"

            femaleLowView = TextView(applicationContext)
            femaleLowView.setTextColor(Color.BLACK)
            femaleLowView.layoutParams = everyItemParam
            if (femaleLowMap.containsKey(age)) {
                femaleLowView.text = femaleLowMap[age].toString()
                totalFemaleStudentsAtThisAge += femaleLowMap[age]!!
                femaleLow = femaleLowMap[age]!!.toFloat()
            } else
                femaleLowView.text = "0"

            maleMidView = TextView(applicationContext)
            maleMidView.setTextColor(Color.BLACK)
            maleMidView.layoutParams = everyItemParam
            if (maleMidMap.containsKey(age)) {
                maleMidView.text = maleMidMap[age].toString()
                totalMaleStudentsAtThisAge += maleMidMap[age]!!
                maleMid = maleMidMap[age]!!.toFloat()
            } else
                maleMidView.text = "0"

            femaleMidView = TextView(applicationContext)
            femaleMidView.setTextColor(Color.BLACK)
            femaleMidView.layoutParams = everyItemParam
            if (femaleMidMap.containsKey(age)) {
                femaleMidView.text = femaleMidMap[age].toString()
                totalFemaleStudentsAtThisAge += femaleMidMap[age]!!
                femaleMid = femaleMidMap[age]!!.toFloat()
            } else
                femaleMidView.text = "0"

            maleHighView = TextView(applicationContext)
            maleHighView.setTextColor(Color.BLACK)
            maleHighView.layoutParams = everyItemParam
            if (maleHighMap.containsKey(age)) {
                maleHighView.text = maleHighMap[age].toString()
                totalMaleStudentsAtThisAge += maleHighMap[age]!!
                maleHigh = maleHighMap[age]!!.toFloat()
            } else
                maleHighView.text = "0"

            femaleHighView = TextView(applicationContext)
            femaleHighView.setTextColor(Color.BLACK)
            femaleHighView.layoutParams = everyItemParam
            if (femaleHighMap.containsKey(age)) {
                femaleHighView.text = femaleHighMap[age].toString()
                totalFemaleStudentsAtThisAge += femaleHighMap[age]!!
                femaleHigh = femaleHighMap[age]!!.toFloat()
            } else
                femaleHighView.text = "0"


            var identifier = ""



            totalStudentsAtThisAge = totalMaleStudentsAtThisAge + totalFemaleStudentsAtThisAge


            if (mPresenter!!.showPercentages!!) {
                identifier = "%"
                if (mPresenter!!.isGenderDisaggregate) {
                    maleLow = maleLow / totalMaleStudentsAtThisAge * 100
                    maleMid = maleMid / totalMaleStudentsAtThisAge * 100
                    maleHigh = maleHigh / totalMaleStudentsAtThisAge * 100
                    femaleLow = femaleLow / totalFemaleStudentsAtThisAge * 100
                    femaleMid = femaleMid / totalFemaleStudentsAtThisAge * 100
                    femaleHigh = femaleHigh / totalFemaleStudentsAtThisAge * 100
                } else {
                    maleLow = (maleLow + femaleLow) / totalStudentsAtThisAge * 100
                    maleMid = (maleMid + femaleMid) / totalStudentsAtThisAge * 100
                    maleHigh = (maleHigh + femaleHigh) / totalStudentsAtThisAge * 100

                    maleLowView.text = maleLow.toString() + identifier
                    maleMidView.text = maleMid.toString() + identifier
                    maleHighView.text = maleHigh.toString() + identifier

                    femaleLowView.text = ""
                    femaleMidView.text = ""
                    femaleHighView.text = ""
                }
            } else {

                maleLowView.text = maleLow.toString() + identifier
                maleMidView.text = maleMid.toString() + identifier
                maleHighView.text = maleHigh.toString() + identifier

                femaleLowView.text = femaleLow.toString() + identifier
                femaleMidView.text = femaleMid.toString() + identifier
                femaleHighView.text = femaleHigh.toString() + identifier
            }

            if (!(mPresenter!!.showPercentages)!! && !mPresenter!!.isGenderDisaggregate) {


                if (maleLowMap.containsKey(age)) {
                    maleLow = maleLowMap[age]!!.toFloat()
                }
                if (maleMidMap.containsKey(age)) {
                    maleMid = maleMidMap[age]!!.toFloat()
                }
                if (maleHighMap.containsKey(age)) {
                    maleHigh = maleHighMap[age]!!.toFloat()
                }

                if (femaleLowMap.containsKey(age)) {
                    femaleLow = femaleLowMap[age]!!.toFloat()
                }
                if (femaleMidMap.containsKey(age)) {
                    femaleMid = femaleMidMap[age]!!.toFloat()
                }
                if (femaleHighMap.containsKey(age)) {
                    femaleHigh = femaleHighMap[age]!!.toFloat()
                }

                val currentAverageLowValue = (maleLow + femaleLow) / 2
                maleLowView.text = currentAverageLowValue.toString()
                val currentAverageMidValue = (maleMid + femaleMid) / 2
                maleMidView.text = currentAverageMidValue.toString()
                val currentAverageHighValue = (maleHigh + femaleHigh) / 2
                maleHighView.text = currentAverageHighValue.toString()

                femaleLowView.text = ""
                femaleMidView.text = ""
                femaleHighView.text = ""


            }

            everyAgeRow.addView(ageView)
            everyAgeRow.addView(maleLowView)
            everyAgeRow.addView(femaleLowView)
            everyAgeRow.addView(maleMidView)
            everyAgeRow.addView(femaleMidView)
            everyAgeRow.addView(maleHighView)
            everyAgeRow.addView(femaleHighView)


            addThese.add(everyAgeRow)

            //heading row to tableTextData
            //val everyAgeRowSA = arrayOfNulls<String>(everyAgeRow.childCount)

            val everyAgeRowSA = arrayOfNulls<String?>(headingRow.childCount)

            for (i in 0 until everyAgeRow.childCount) {
                everyAgeRowSA[i] = (everyAgeRow.getChildAt(i) as TextView).text.toString()
            }
            tableTextData.add(everyAgeRowSA)

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
