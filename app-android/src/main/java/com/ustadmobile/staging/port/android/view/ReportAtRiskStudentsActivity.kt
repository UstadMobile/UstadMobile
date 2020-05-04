package com.ustadmobile.staging.port.android.view

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ReportAtRiskStudentsPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ReportAtRiskStudentsView
import com.ustadmobile.lib.db.entities.PersonWithEnrollment
import com.ustadmobile.port.android.view.UstadBaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

class ReportAtRiskStudentsActivity : UstadBaseActivity(), ReportAtRiskStudentsView, PopupMenu.OnMenuItemClickListener {

    private var toolbar: Toolbar? = null
    private val reportLinearLayout: LinearLayout? = null
    private var mPresenter: ReportAtRiskStudentsPresenter? = null
    private var mRecyclerView: RecyclerView? = null

    /**
     * Data for export report. Used to construct the export report (has line by line information)
     */
    internal lateinit var tableTextData: MutableList<Array<String>>

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Set layout:
        setContentView(R.layout.activity_report_at_risk_students)

        //Toolbar
        toolbar = findViewById(R.id.activity_report_at_risk_students_toolbar)
        toolbar!!.setTitle(R.string.at_risk_students)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        mRecyclerView = findViewById(R.id.activity_report_at_risk_students_rv)

        //reportLinearLayout = findViewById(R.id.activity_report_at_risk_students_ll);

        tableTextData = ArrayList()

        //Recycler View for Report
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        //Presenter
        mPresenter = ReportAtRiskStudentsPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_report_at_risk_students_fab)
        fab.setOnClickListener { this.showPopup(it) }
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

        val title = "report_at_risk_students_" + System.currentTimeMillis()

        val output = File(dir, "$title.xlsx")
        xlsxReportPath = output.absolutePath

        val testDir = File(dir, title)
        testDir.mkdir()
        val workingDir = testDir.absolutePath

        mPresenter!!.dataToXLSX(title, xlsxReportPath, workingDir)

    }

    override fun generateCSVReport() {
        var csvReportFilePath = ""
        //Create the file.

        val dir = filesDir
        val output = File(dir, "report_at_risk_students_" +
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


    fun generateViewForDataSet(classDataSet: List<PersonWithEnrollment>): List<View> {

        val classRiskStudentViews = ArrayList<View>()

        for (everyStudent in classDataSet) {
            val hl = LinearLayout(applicationContext)
            hl.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)

            val studentNameTV = TextView(applicationContext)
            studentNameTV.textSize = 10f
            studentNameTV.setTextColor(Color.BLACK)
            studentNameTV.text = (everyStudent.firstNames +
                    " " + everyStudent.lastName +
                    " (" + (everyStudent.attendancePercentage * 100).toString()
                    + "% " + getText(R.string.attendance).toString() + ")")

            classRiskStudentViews.add(studentNameTV)
        }



        return classRiskStudentViews
    }


    override fun updateTables(dataMaps: LinkedHashMap<String, List<PersonWithEnrollment>>) {

        tableTextData = ArrayList()

        val iterator = dataMaps.keys.iterator()
        while (iterator.hasNext()) {
            val className = iterator.next()
            val classDataSet = dataMaps[className]

            if (!classDataSet!!.isEmpty()) {
                //Add title to tableTextData
                val titleItems = arrayOf(className)
                tableTextData.add(titleItems)

                val numRiskStudents = classDataSet.size

                val addThese = generateViewForDataSet(classDataSet)

                //Heading
                val heading = TextView(applicationContext)
                heading.setTextColor(Color.BLACK)
                heading.setTypeface(null, Typeface.BOLD)
                heading.text = className + "( " + numRiskStudents + " " +
                        getText(R.string.students_literal) + ")"

                val everyClassLL = LinearLayout(applicationContext)
                everyClassLL.orientation = LinearLayout.VERTICAL

                runOnUiThread {

                    for (everyStudentRow in addThese) {
                        everyClassLL.addView(everyStudentRow)
                    }

                    reportLinearLayout!!.addView(heading)
                    reportLinearLayout.addView(everyClassLL)
                }
            }
        }

    }

    override fun setTableTextData(tableTextData: MutableList<Array<String>>) {
        this.tableTextData = tableTextData
    }

    override fun setReportProvider(factory: DataSource.Factory<Int, PersonWithEnrollment>) {
        val recyclerAdapter = PersonWithEnrollmentRecyclerAdapter(DIFF_CALLBACK2, applicationContext,
                this, mPresenter!!, true, false, true,
                true)

        recyclerAdapter.setShowAddStudent(false)
        recyclerAdapter.setShowAddTeacher(false)

        val data = LivePagedListBuilder(factory, 20).build()

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<PersonWithEnrollment>> { recyclerAdapter.submitList(it) })
        }

        mRecyclerView!!.adapter = recyclerAdapter
    }

    companion object {

        val DIFF_CALLBACK2: DiffUtil.ItemCallback<PersonWithEnrollment> = object
            : DiffUtil.ItemCallback<PersonWithEnrollment>() {
            override fun areItemsTheSame(oldItem: PersonWithEnrollment,
                                         newItem: PersonWithEnrollment): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: PersonWithEnrollment,
                                            newItem: PersonWithEnrollment): Boolean {
                return oldItem.personUid == newItem.personUid
            }
        }
    }
}
