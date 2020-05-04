package com.ustadmobile.staging.port.android.view

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
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
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ReportMasterPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ReportMasterView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ReportMasterItem
import com.ustadmobile.port.android.view.UstadBaseActivity
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

class ReportMasterActivity : UstadBaseActivity(), ReportMasterView, PopupMenu.OnMenuItemClickListener {

    private var toolbar: Toolbar? = null
    private var mPresenter: ReportMasterPresenter? = null
    private var tableLayout: TableLayout? = null

    //For export line by line data.
    internal lateinit var tableTextData: MutableList<Array<String?>>

    /**
     * Creates a new Horizontal line for a table's row.
     * @return  The horizontal line view.
     */
    //Horizontal line
    val horizontalLine: View
        get() {
            val hlineParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, 1)
            val hl = View(this)
            hl.setBackgroundColor(Color.GRAY)
            hl.layoutParams = hlineParams
            return hl
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Set layout
        setContentView(R.layout.activity_report_master)

        //Toolbar:
        toolbar = findViewById(R.id.activity_report_master_toolbar)
        toolbar!!.setTitle(R.string.irc_master_list_report)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        tableLayout = findViewById(R.id.activity_report_master_table)

        //Call the Presenter
        mPresenter = ReportMasterPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_report_master_fab)
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
            generateCSVReport()
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

        val title = "report_irc_master_list_" + System.currentTimeMillis()

        val output = File(dir, "$title.xlsx")
        xlsxReportPath = output.absolutePath

        val testDir = File(dir, title)
        testDir.mkdir()
        val workingDir = testDir.absolutePath

        mPresenter!!.dataToXLSX(title, xlsxReportPath, workingDir, tableTextData)

    }

    override fun generateCSVReport() {

        var csvReportFilePath = ""
        //Create the file.

        val dir = filesDir
        val output = File(dir, "report_irc_master_list_" +
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

    override fun updateTables(items: List<ReportMasterItem>) {
        println("Updating tables with : " + items.size + " items.")
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

        val classNameHTV: TextView
        val firstNameHTV: TextView
        val lastNameHTV: TextView
        val studentIDHTV: TextView
        val daysPresentHTV: TextView
        val daysAbsentHTV: TextView
        val daysPartialHTV: TextView
        val totalClazzDaysHTV: TextView
        val dateLeftHTV: TextView
        val activeStatusHTV: TextView
        val genderHTV: TextView
        val dobHTV: TextView

        classNameHTV = TextView(applicationContext)
        classNameHTV.layoutParams = everyItemParam
        classNameHTV.setTextColor(Color.BLACK)
        classNameHTV.setTypeface(null, Typeface.BOLD)
        classNameHTV.setText(R.string.class_id)

        firstNameHTV = TextView(applicationContext)
        firstNameHTV.layoutParams = everyItemParam
        firstNameHTV.setTextColor(Color.BLACK)
        firstNameHTV.setTypeface(null, Typeface.BOLD)
        firstNameHTV.setText(R.string.first_name)

        lastNameHTV = TextView(applicationContext)
        lastNameHTV.layoutParams = everyItemParam
        lastNameHTV.setTextColor(Color.BLACK)
        lastNameHTV.setTypeface(null, Typeface.BOLD)
        lastNameHTV.setText(R.string.last_name)

        studentIDHTV = TextView(applicationContext)
        studentIDHTV.layoutParams = everyItemParam
        studentIDHTV.setTextColor(Color.BLACK)
        studentIDHTV.setTypeface(null, Typeface.BOLD)
        studentIDHTV.setText(R.string.student_id)

        daysPresentHTV = TextView(applicationContext)
        daysPresentHTV.layoutParams = everyItemParam
        daysPresentHTV.setTextColor(Color.BLACK)
        daysPresentHTV.setTypeface(null, Typeface.BOLD)
        daysPresentHTV.setText(R.string.count_present_days)

        daysAbsentHTV = TextView(applicationContext)
        daysAbsentHTV.layoutParams = everyItemParam
        daysAbsentHTV.setTextColor(Color.BLACK)
        daysAbsentHTV.setTypeface(null, Typeface.BOLD)
        daysAbsentHTV.setText(R.string.count_absent_days)

        daysPartialHTV = TextView(applicationContext)
        daysPartialHTV.layoutParams = everyItemParam
        daysPartialHTV.setTextColor(Color.BLACK)
        daysPartialHTV.setTypeface(null, Typeface.BOLD)
        daysPartialHTV.setText(R.string.count_partial_days)

        totalClazzDaysHTV = TextView(applicationContext)
        totalClazzDaysHTV.layoutParams = everyItemParam
        totalClazzDaysHTV.setTextColor(Color.BLACK)
        totalClazzDaysHTV.setTypeface(null, Typeface.BOLD)
        totalClazzDaysHTV.setText(R.string.class_days)

        dateLeftHTV = TextView(applicationContext)
        dateLeftHTV.layoutParams = everyItemParam
        dateLeftHTV.setTextColor(Color.BLACK)
        dateLeftHTV.setTypeface(null, Typeface.BOLD)
        dateLeftHTV.setText(R.string.date_left)

        activeStatusHTV = TextView(applicationContext)
        activeStatusHTV.layoutParams = everyItemParam
        activeStatusHTV.setTextColor(Color.BLACK)
        activeStatusHTV.setTypeface(null, Typeface.BOLD)
        activeStatusHTV.setText(R.string.active)

        genderHTV = TextView(applicationContext)
        genderHTV.layoutParams = everyItemParam
        genderHTV.setTextColor(Color.BLACK)
        genderHTV.setTypeface(null, Typeface.BOLD)
        genderHTV.setText(R.string.gender_literal)

        dobHTV = TextView(applicationContext)
        dobHTV.layoutParams = everyItemParam
        dobHTV.setTextColor(Color.BLACK)
        dobHTV.setTypeface(null, Typeface.BOLD)
        dobHTV.setText(R.string.birthday)

        headingRow.addView(classNameHTV)
        headingRow.addView(firstNameHTV)
        headingRow.addView(lastNameHTV)
        headingRow.addView(studentIDHTV)
        headingRow.addView(daysPresentHTV)
        headingRow.addView(daysAbsentHTV)
        headingRow.addView(daysPartialHTV)
        headingRow.addView(totalClazzDaysHTV)
        headingRow.addView(dateLeftHTV)
        headingRow.addView(activeStatusHTV)
        headingRow.addView(genderHTV)
        headingRow.addView(dobHTV)


        tableLayout!!.addView(headingRow)
        tableLayout!!.addView(horizontalLine)

        //MAKE TABLE TEXT DATA:
        val headingItems = arrayOfNulls<String>(headingRow.childCount)
        for (i in 0 until headingRow.childCount) {
            headingItems[i] = (headingRow.getChildAt(i) as TextView).text.toString()
        }
        tableTextData.add(headingItems)


        if (!items.isEmpty()) {
            var classNameTV: TextView
            var firstNameTV: TextView
            var lastNameTV: TextView
            var studentIDTV: TextView
            var daysPresentTV: TextView
            var daysAbsentTV: TextView
            var daysPartialTV: TextView
            var totalClazzDaysTV: TextView
            var dateLeftTV: TextView
            var activeStatusTV: TextView
            var genderTV: TextView
            var dobTV: TextView

            for (everyItem in items) {
                val iRow = TableRow(applicationContext)
                iRow.layoutParams = rowParams


                classNameTV = TextView(applicationContext)
                classNameTV.layoutParams = everyItemParam
                classNameTV.setTextColor(Color.BLACK)
                classNameTV.text = everyItem.clazzName

                firstNameTV = TextView(applicationContext)
                firstNameTV.layoutParams = everyItemParam
                firstNameTV.setTextColor(Color.BLACK)
                firstNameTV.text = everyItem.firstNames

                lastNameTV = TextView(applicationContext)
                lastNameTV.layoutParams = everyItemParam
                lastNameTV.setTextColor(Color.BLACK)
                lastNameTV.text = everyItem.lastName

                studentIDTV = TextView(applicationContext)
                studentIDTV.layoutParams = everyItemParam
                studentIDTV.setTextColor(Color.BLACK)
                studentIDTV.text = everyItem.personUid.toString()

                daysPresentTV = TextView(applicationContext)
                daysPresentTV.layoutParams = everyItemParam
                daysPresentTV.setTextColor(Color.BLACK)
                daysPresentTV.text = everyItem.daysPresent.toString()

                daysAbsentTV = TextView(applicationContext)
                daysAbsentTV.layoutParams = everyItemParam
                daysAbsentTV.setTextColor(Color.BLACK)
                daysAbsentTV.text = everyItem.daysAbsent.toString()

                daysPartialTV = TextView(applicationContext)
                daysPartialTV.layoutParams = everyItemParam
                daysPartialTV.setTextColor(Color.BLACK)
                daysPartialTV.text = everyItem.daysPartial.toString()

                totalClazzDaysTV = TextView(applicationContext)
                totalClazzDaysTV.layoutParams = everyItemParam
                totalClazzDaysTV.setTextColor(Color.BLACK)
                totalClazzDaysTV.text = everyItem.clazzDays.toString()

                dateLeftTV = TextView(applicationContext)
                dateLeftTV.layoutParams = everyItemParam
                dateLeftTV.setTextColor(Color.BLACK)
                dateLeftTV.text = UMCalendarUtil.getPrettyDateFromLong(everyItem.dateLeft, "")

                activeStatusTV = TextView(applicationContext)
                activeStatusTV.layoutParams = everyItemParam
                activeStatusTV.setTextColor(Color.BLACK)
                activeStatusTV.text = if (everyItem.isClazzMemberActive)
                    getText(R.string.yes_literal)
                else
                    getText(R.string.no_literal)

                genderTV = TextView(applicationContext)
                genderTV.layoutParams = everyItemParam
                genderTV.setTextColor(Color.BLACK)
                var theGender = ""
                theGender = getText(R.string.not_set) as String
                when (everyItem.gender) {
                    Person.GENDER_FEMALE -> theGender = getText(R.string.female) as String
                    Person.GENDER_MALE -> theGender = getText(R.string.male) as String
                    Person.GENDER_OTHER -> theGender = getText(R.string.other_not_set) as String
                    Person.GENDER_UNSET -> {
                    }
                    else -> theGender = getText(R.string.not_set) as String
                }
                genderTV.text = theGender

                dobTV = TextView(applicationContext)
                dobTV.layoutParams = everyItemParam
                dobTV.setTextColor(Color.BLACK)

                dobTV.text = UMCalendarUtil.getPrettyDateFromLong(everyItem.dateOfBirth, "")

                iRow.addView(classNameTV)
                iRow.addView(firstNameTV)
                iRow.addView(lastNameTV)
                iRow.addView(studentIDTV)
                iRow.addView(daysPresentTV)
                iRow.addView(daysAbsentTV)
                iRow.addView(daysPartialTV)
                iRow.addView(totalClazzDaysTV)
                iRow.addView(dateLeftTV)
                iRow.addView(activeStatusTV)
                iRow.addView(genderTV)
                iRow.addView(dobTV)

                //BUILD TABLE TEXT DATA
                val rowItems = arrayOfNulls<String>(iRow.childCount)
                for (i in 0 until iRow.childCount) {
                    rowItems[i] = (iRow.getChildAt(i) as TextView).text.toString()
                }
                tableTextData.add(rowItems)

                tableLayout!!.addView(iRow)
                tableLayout!!.addView(horizontalLine)

            }

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
