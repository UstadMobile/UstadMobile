package com.ustadmobile.port.android.view


import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ReportSELPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ReportSELView
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson
import com.ustadmobile.port.android.view.ClazzEditActivity.Companion.dpToPx
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.io.File
import java.io.FileWriter


class ReportSELActivity : UstadBaseActivity(), ReportSELView, PopupMenu.OnMenuItemClickListener {

    private var reportLinearLayout: LinearLayout? = null

    //For export line by line data.
    internal lateinit var tableTextData: MutableList<Array<String>>

    //Presenter
    internal lateinit var mPresenter: ReportSELPresenter

    internal var imageLP = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)

    //The clazz sel report data
    internal lateinit var clazzMap: LinkedHashMap<String,
            LinkedHashMap<String, HashMap<Long, ArrayList<Long>>>>

    /**
     * Creates a new Vertical line for a table's row
     * @return  The vertical line view.
     */
    //Vertical line
    val verticalLine: View
        get() {
            val vLineParams = TableRow.LayoutParams(1, TableRow.LayoutParams.MATCH_PARENT)
            val vla = View(this)
            vla.setBackgroundColor(Color.GRAY)
            vla.layoutParams = vLineParams
            return vla
        }

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

    /**
     * Creates and returns a new tick Image View.
     * @return  The imageview view
     */
    //tickIV.setLayoutParams(imageLP);
    val tick: View
        get() {
            val tickIV = AppCompatImageView(this)
            tickIV.setImageResource(R.drawable.ic_check_black_24dp)
            tickIV.setColorFilter(Color.GRAY)
            return tickIV
        }

    /**
     * Creates and returns a new cross/cancel/remove Image View
     * @return  The imageview view
     */
    //crossIV.setLayoutParams(imageLP);
    val cross: View
        get() {
            imageLP.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            val crossIV = AppCompatImageView(this)
            crossIV.setImageResource(R.drawable.ic_clear_black_24dp)
            crossIV.setColorFilter(Color.GRAY)
            return crossIV
        }

    /**
     * Create and returns a new - (dash) ImageView
     * @return  The imageview view
     */
    //naIV.setLayoutParams(imageLP);
    val na: View
        get() {
            val naIV = AppCompatImageView(this)
            naIV.setImageResource(R.drawable.ic_remove_black_24dp)
            naIV.setColorFilter(Color.GRAY)
            return naIV
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Set layout
        setContentView(R.layout.activity_report_sel)

        //Toolbar:
        val toolbar = findViewById<Toolbar>(R.id.activity_report_sel_toolbar)
        toolbar.setTitle(R.string.sel_report)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        reportLinearLayout = findViewById(R.id.activity_report_sel_ll)

        //Call the Presenter
        mPresenter = ReportSELPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_report_sel_fab)
        fab.setOnClickListener { this.showPopup(it) }

        tableTextData = ArrayList()
    }

    fun showPopup(v: View) {
        val popup = PopupMenu(this, v)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.menu_export, popup.menu)
        popup.setOnMenuItemClickListener(this)
        popup.show()
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

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == R.id.menu_export_csv) {
            generateCSVReport()
            return true
        } else if (i == R.id.menu_export_xls) {
            try {
                startXLSXReportGeneration()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return true

        } else {
            return false
        }
    }

    /**
     * Starts the xlsx report process. Here it crates hte xlsx file.
     */
    private fun startXLSXReportGeneration() {

        val dir = filesDir
        val xlsxReportPath: String

        val title = "report_sel_" + System.currentTimeMillis()

        val output = File(dir, "$title.xlsx")
        xlsxReportPath = output.getAbsolutePath()

        val testDir = File(dir, title)
        testDir.mkdir()
        val workingDir = testDir.getAbsolutePath()

        mPresenter.dataToXLSX(title, xlsxReportPath, workingDir)

    }

    override fun generateCSVReport() {

        val csvReportFilePath: String
        //Create the file.

        val dir = filesDir
        val output = File(dir, "report_sel_" +
                System.currentTimeMillis() + ".csv")
        csvReportFilePath = output.getAbsolutePath()

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

    override fun generateXLSReport(xlsxReportPath: String) {

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
     *
     * Updates the raw data given and starts to construct the tables on the SEL report.
     *
     * @param clazzMap          The raw sel report data in a map grouped by clazz, further grouped
     * by questions and further by nominator -> nominee list
     * @param clazzToStudents   A map of every clazz and its clazz members for the view to construct
     */

    override fun createTables(clazzMap: LinkedHashMap<String,
            LinkedHashMap<String, HashMap<Long, ArrayList<Long>>>>, clazzToStudents: HashMap<String,
            List<ClazzMemberWithPerson>>) {


        //Build a string array of the data
        tableTextData = arrayListOf()

        this.clazzMap = clazzMap

        //Work with: reportLinearLayout linear layout
        val headingParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
        headingParams.setMargins(dpToPx(8), dpToPx(32), dpToPx(8), dpToPx(16))

        val everyItemParam = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
        everyItemParam.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
        everyItemParam.gravity = Gravity.CENTER_VERTICAL

        //For every clazz:
        for (currentClazzName in clazzMap.keys) {
            val clazzMembers = clazzToStudents[currentClazzName]

            //Class Name heading
            val clazzHeading = TextView(applicationContext)
            clazzHeading.layoutParams = headingParams
            clazzHeading.setTextColor(Color.BLACK)
            clazzHeading.setTypeface(null, Typeface.BOLD)
            clazzHeading.setText(currentClazzName)

            //table text data for reports
            val titleItems = arrayOf<String>(currentClazzName)
            tableTextData!!.add(titleItems)


            val clazzNominationData = clazzMap[currentClazzName]

            //Create a tableLayout for this clazz for all the Students.
            val tableLayout = generateTableLayoutForClazz(clazzMembers!!)

            //Default look up first question when constructing the SEL report tables.
            assert(clazzNominationData != null)
            val firstQuestionTitle = clazzNominationData!!.keys.iterator().next()
            updateTableBasedOnQuestionSelected(currentClazzName, firstQuestionTitle, tableLayout)

            val horizontalScrollView = HorizontalScrollView(this)
            horizontalScrollView.addView(tableLayout)

            reportLinearLayout!!.addView(clazzHeading)
            reportLinearLayout!!.addView(horizontalScrollView)

            val hLL = LinearLayout(this)
            val hllP = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            hLL.layoutParams = hllP
            hLL.orientation = LinearLayout.HORIZONTAL

            val questions = clazzNominationData!!.keys
            val allButtons = arrayListOf<View>()

            val buttonParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            buttonParams.weight = 1f

            for (everyQuestion in questions) {

                val questionButton = Button(this)
                questionButton.layoutParams = buttonParams
                questionButton.textSize = 12f
                val shape = GradientDrawable()
                shape.cornerRadius = SEL_REPORT_QUESTION_BUTTON_RADIUS.toFloat()

                if (firstQuestionTitle.toString() == everyQuestion.toString()) {
                    shape.setColorFilter(resources.getColor(R.color.primary),
                            PorterDuff.Mode.ADD)
                } else {
                    shape.setColorFilter(Color.GRAY, PorterDuff.Mode.ADD)
                }
                questionButton.background = shape
                questionButton.setText(everyQuestion as String)
                questionButton.setPadding(12, 20, 12, 8)
                questionButton.gravity = 1

                allButtons.add(questionButton)
                questionButton.setOnClickListener { v ->
                    updateTableBasedOnQuestionSelected(currentClazzName, everyQuestion,
                            tableLayout)


                    //Gray out other buttons.
                    for (everyButton in allButtons) {
                        val shapeb = GradientDrawable()
                        shapeb.cornerRadius = SEL_REPORT_QUESTION_BUTTON_RADIUS.toFloat()
                        shapeb.setColorFilter(Color.GRAY, PorterDuff.Mode.ADD)
                        everyButton.setBackground(shapeb)
                    }
                    val shapea = GradientDrawable()
                    shapea.cornerRadius = SEL_REPORT_QUESTION_BUTTON_RADIUS.toFloat()
                    shapea.setColorFilter(resources.getColor(R.color.primary),
                            PorterDuff.Mode.ADD)
                    v.background = shapea

                }

                hLL.addView(questionButton)
            }
            reportLinearLayout!!.addView(hLL)


        }
    }

    /**
     * Updates the current Clazz SEL table and updates its sel result nomination markings based
     * on the question uid selected from the raw rel report data
     * @param clazzName         The clazzName of the table we want to update (used to get raw data)
     * @param questionTitle     The question title we want the table to reflect
     * @param tableLayout       The table it self that needs updating.
     */
    private fun updateTableBasedOnQuestionSelected(clazzName: String, questionTitle: String,
                                                   tableLayout: TableLayout) {

        val clazzNominationData = clazzMap[clazzName]!!
        val testFirstQuestionData = clazzNominationData.get(questionTitle)!!
        //For every nominations in that question ..
        for (nominatorUid in testFirstQuestionData.keys) {
            val nomineeList = testFirstQuestionData.get(nominatorUid)
            // ..update the markings on the TableLayout
            processTable(nominatorUid, nomineeList!!, tableLayout)
        }
    }

    /**
     * Update the sel nomination ticks to the table provided for the given nominator.
     *
     * @param nominatorUid  The Nominator's ClazzMember Uid
     * @param nomineeList   The Nominations (nominee List) nominated by the nominatorUid
     * in a list of ClazzMember Uids
     * @param tableLayout   The table to update the ticks/crosses for the given Nominator and
     * its nominations.
     */
    private fun processTable(nominatorUid: Long?, nomineeList: List<Long>, tableLayout: TableLayout) {

        var nominatorRow: TableRow? = null
        //Find the table Row that is marked with the Nominator Uid we want.
        for (i in 0 until tableLayout.childCount) {
            val child = tableLayout.getChildAt(i)
            if (child is TableRow) {
                if(child.getTag(TAG_NOMINATOR_CLAZZMEMBER_UID) != null) {
                    val childNominatorUid = child.getTag(TAG_NOMINATOR_CLAZZMEMBER_UID) as Long
                    if (childNominatorUid == nominatorUid!!.toLong()) {
                        //Save it for the next loop
                        nominatorRow = child
                        break
                    }
                }
            }
        }

        if (nominatorRow != null) {
            //Find all views within that nominator Row that have an id in the NomineeList
            for (j in 0 until nominatorRow.childCount) {
                val rowChild = nominatorRow.getChildAt(j)
                if (rowChild is AppCompatImageView) {
                    if(rowChild.getTag(TAG_NOMINEE_CLAZZMEMBER_UID) != null) {
                        val rowChildNomineeUid = rowChild.getTag(TAG_NOMINEE_CLAZZMEMBER_UID) as Long
                        if (nomineeList.contains(rowChildNomineeUid)) {
                            //If this cell is in the nominee list , change the view to be a tick!
                            val nomineeImageView = rowChild as AppCompatImageView
                            nomineeImageView.setImageResource(R.drawable.ic_check_black_24dp)
                        } else {
                            //if not, its a cross
                            val nomineImageView = rowChild as AppCompatImageView
                            nomineImageView.setImageResource(R.drawable.ic_clear_black_24dp)
                        }
                    }
                }
            }
        }

    }

    /**
     * Generates an SEL table layout view for the given Clazz Members.
     * ____________________________________________
     * |SEL Heading Row ... |         |         |   |
     * |  Nominating:       |Student 1|Student 2|...|
     * |____________________|_________|_________|___|
     * |Nomination Row 1..  |         |         |   |
     * |  Student 1         |   -     |    ✓    |   |
     * |____________________|_________|_________|___|
     * |Nomination Row 2..  |         |         |   |
     * |  Student 2         |   ✖     |   -     |   |
     * |____________________|_________|_________|___|
     * |  ...               |         |         |   |
     * |____________________|_________|_________|___|
     *
     *
     * @param clazzMembers  A list of type ClazzMemberWithPerson of all the clazz members
     *
     * @return  The TabLayout View with the SEL report.
     */
    fun generateTableLayoutForClazz(clazzMembers: List<ClazzMemberWithPerson>): TableLayout {

        //LAYOUT Parameters
        val rowParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)

        val everyItemParam = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
        everyItemParam.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
        everyItemParam.gravity = Gravity.CENTER_VERTICAL

        val tableParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT)
        tableParams.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))

        //Create a new table layout
        val selTableLayout = TableLayout(applicationContext)
        selTableLayout.layoutParams = tableParams

        //SEL Table's heading row
        val selTableTopRow = TableRow(applicationContext)
        selTableTopRow.layoutParams = rowParams

        //SEL Table's nomination rows
        val nominationRows = arrayListOf<View>()

        //SEL Table's heading's 1st item: Nominating.
        selTableTopRow.addView(verticalLine)
        val nominatingHeading = TextView(applicationContext)
        nominatingHeading.layoutParams = everyItemParam
        nominatingHeading.textSize = 12f
        nominatingHeading.setTextColor(Color.BLACK)
        val nominatingString = getText(R.string.nominating).toString() + ":"
        nominatingHeading.text = nominatingString

        selTableTopRow.addView(nominatingHeading)
        //Add vertical line after every item in the rows
        selTableTopRow.addView(verticalLine)

        // Loop through every student in this Clazz and add to Nominee names in the heading row.
        for (everyClazzMember in clazzMembers) {

            //Add this textview to the heading row (nominees)
            val aStudentTopRowTV = TextView(applicationContext)
            aStudentTopRowTV.layoutParams = everyItemParam
            aStudentTopRowTV.textSize = 12f
            aStudentTopRowTV.setTextColor(Color.BLACK)
            val personName = everyClazzMember.person!!.firstNames + " " +
                    everyClazzMember.person!!.lastName
            aStudentTopRowTV.text = personName

            selTableTopRow.addView(aStudentTopRowTV)
            //Add vertical line every time in the rows
            selTableTopRow.addView(verticalLine)


            //Create Nomination Rows for every clazz member (students here)
            val nominationRow = TableRow(applicationContext)
            nominationRow.layoutParams = rowParams

            //Add this textView as the first cell in the nomination rows
            val nominationRowStudentTV = TextView(this)
            nominationRowStudentTV.layoutParams = everyItemParam
            nominationRowStudentTV.textSize = 12f
            nominationRowStudentTV.setTextColor(Color.BLACK)
            nominationRowStudentTV.text = personName
            nominationRowStudentTV.setTag(TAG_NOMINATOR_CLAZZMEMBER_UID,
                    everyClazzMember.clazzMemberUid)

            //Add vertical line between cells in the row:
            nominationRow.addView(verticalLine)
            //Add the nominator name to the nominator Row.
            nominationRow.addView(nominationRowStudentTV)

            //Set tags on the rows so we can find these rows when populating the sel results
            nominationRow.setTag(TAG_NOMINATOR_CLAZZMEMBER_UID, everyClazzMember.clazzMemberUid)
            nominationRow.setTag(TAG_NOMINATOR_NAME, everyClazzMember.person!!.firstNames
                    + " " + everyClazzMember.person!!.lastName)
            //vertical line after nominator name
            nominationRow.addView(verticalLine)
            //Loop through All Students again to addd the default tick/cross/dash image views and
            // assign them nominee and nominator tags (so we can alter then later)
            for (againClazzMember in clazzMembers) {
                val crossView = cross
                crossView.setTag(TAG_NOMINATOR_CLAZZMEMBER_UID, everyClazzMember.clazzMemberUid)
                crossView.setTag(TAG_NOMINEE_CLAZZMEMBER_UID, againClazzMember.clazzMemberUid)
                if (everyClazzMember.clazzMemberUid == againClazzMember.clazzMemberUid) {
                    nominationRow.addView(na)
                } else {
                    nominationRow.addView(crossView)
                }
                //need that vertical line
                nominationRow.addView(verticalLine)
            }
            //All all Nominee imageview and vertical line views to the nomination rows
            nominationRows.add(nominationRow)
        }

        //Table layout top horizontal line
        selTableLayout.addView(horizontalLine)
        //Table layout top row
        selTableLayout.addView(selTableTopRow)
        //another horizontal line
        selTableLayout.addView(horizontalLine)

        //Get every nomination rows
        for (everyRow in nominationRows) {
            //..and add it to the table layout
            selTableLayout.addView(everyRow)
            //can't forget the horizontal line
            selTableLayout.addView(horizontalLine)
        }

        //Make it scrollable
        selTableLayout.isScrollContainer = true
        return selTableLayout
    }

    companion object {

        val TAG_NOMINEE_CLAZZMEMBER_UID = R.string.nomination
        val TAG_NOMINATOR_CLAZZMEMBER_UID = R.string.nominating
        val TAG_NOMINATOR_NAME = R.string.name
        val SEL_REPORT_QUESTION_BUTTON_RADIUS = 50
    }

}
