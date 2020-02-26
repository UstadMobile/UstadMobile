package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ArgumentUtil
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_CLAZZ_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_FROM_DATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_TO_DATE
import com.ustadmobile.core.view.ReportSELView
import com.ustadmobile.staging.core.xlsx.UmSheet
import com.ustadmobile.staging.core.xlsx.UmXLSX
import com.ustadmobile.staging.core.xlsx.ZipUtil
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.io.IOException

class ReportSELPresenter(context: Any, arguments: Map<String, String>?, view: ReportSELView) :
        UstadBaseController<ReportSELView>(context, arguments!!, view) {

    private var fromDate: Long = 0
    private var toDate: Long = 0
    private var clazzList: List<Long>? = null
    internal var repository: UmAppDatabase
    internal lateinit var clazzMap: LinkedHashMap<String, LinkedHashMap<String, HashMap<Long,
            ArrayList<Long>>>>
    internal lateinit var clazzToStudents: HashMap<String, List<ClazzMemberWithPerson>>
    internal lateinit var clazzSheetTemplate: HashMap<String, UmSheet>
    internal lateinit var nominatorToIdMap: HashMap<Long, Int>
    internal lateinit var nomineeToIdMap: HashMap<Long, Int>


    init {
        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        clazzList = ArrayList()

        if (arguments!!.containsKey(ARG_FROM_DATE)) {
            fromDate = arguments!!.get(ARG_FROM_DATE)!!.toLong()
        }
        if (arguments!!.containsKey(ARG_TO_DATE)) {
            toDate = arguments!!.get(ARG_TO_DATE)!!.toLong()
        }
        if (arguments!!.containsKey(ARG_CLAZZ_LIST)) {
            clazzList = ArgumentUtil.convertCSVStringToLongList(arguments.get(ARG_CLAZZ_LIST)!!)
        }

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        getRawData()
    }

    /**
     * Separated out method that queries the database and updates the report upon getting and
     * ordering the result.
     *
     * The SEL Raw data has the following format:
     * [SEL Raw Data] is:
     * [Clazz Name, [Question Data Map]]
     * [ClazzName, [Question Name, [Question Nominations]]]
     * [ClazzName, [Question Name, [Nominator ClazzMember Uid, List<Nominee Uids>]]]
     *
     * eg:
     * [ Class A, ["Who let the dogs out?", [Nominator 1 Uid, <Nominee1Uid></Nominee1Uid>, Nominee2Uid, ...>]]]
     * [ Class A, ["Who let the dogs out?", [Nominator 2 Uid, <Nominee3Uid></Nominee3Uid>, Nominee4Uid, ...>]]]
     * [ Class A, ["Who are your friends?", [Nominator 1 Uid, <...>]]]
     * [Class B, ["Who let the dogs out?", ...]]]
     * ...
     *
    </Nominee> */
    private fun getRawData() {
        val clazzMemberDao = repository.clazzMemberDao

        clazzToStudents = HashMap()
        clazzMap = LinkedHashMap()

        val nominationDao = repository.selQuestionResponseNominationDao

        //Get all nominations
        GlobalScope.launch {
            val allNominations = nominationDao.getAllNominationReportAsync(fromDate, toDate,
                    clazzList)

            //TODO: Handle QuestionSet grouping ? (Not implemented in Prototypes)
            var index = 0
            for (everyNomination in allNominations!!) { //For every nomination / all
                index++
                val thisClazzUid = everyNomination.clazzUid
                val thisClazzName = everyNomination.clazzName
                val thisQuestionTitle = everyNomination.questionText
                val nomineeUid = everyNomination.nomineeUid
                val nominatorUid = everyNomination.nominatorUid

                val questionMap: LinkedHashMap<String, HashMap<Long, ArrayList<Long>>>?
                val nominationMap: HashMap<Long, ArrayList<Long>>?
                val nominations: ArrayList<Long>?

                if (!clazzMap.containsKey(thisClazzName)) {
                    //New Clazz starts. Add question Map and nominations map to every Question.
                    questionMap = LinkedHashMap()
                    nominationMap = HashMap()
                    nominations = ArrayList()

                } else {
                    questionMap = clazzMap[thisClazzName]

                    if (!questionMap!!.containsKey(thisQuestionTitle)) {
                        nominationMap = HashMap()
                        nominations = ArrayList()
                    } else {
                        nominationMap = questionMap.get(thisQuestionTitle)
                        if (!nominationMap!!.containsKey(nominatorUid)) {
                            nominations = ArrayList()
                        } else {
                            nominations = nominationMap[nominatorUid]
                        }
                    }
                }

                nominations!!.add(nomineeUid)
                nominationMap[nominatorUid] = nominations
                questionMap[thisQuestionTitle!!] = nominationMap

                clazzMap[thisClazzName!!] = questionMap

                //Build students map - Add students
                if (!clazzToStudents.containsKey(thisClazzName)) {

                    val finalIndex = index
                    val result =
                            clazzMemberDao.findClazzMemberWithPersonByRoleForClazzUid(thisClazzUid,
                                    ClazzMember.ROLE_STUDENT)
                    clazzToStudents[thisClazzName] = result

                    //If end of the loop
                    if (finalIndex >= allNominations.size) {
                        createTablesOnView()
                        createClassSheetTemplates()
                    }

                } else {
                    //If end of the loop
                    if (index >= allNominations.size) {
                        createTablesOnView()
                        createClassSheetTemplates()
                    }
                }
            }
        }
    }

    /**
     * Send sel raw data to view for table updating.
     */
    private fun createTablesOnView() {
        view.runOnUiThread(Runnable{ view.createTables(clazzMap, clazzToStudents) })
    }

    private fun createClassSheetTemplates() {
        clazzSheetTemplate = HashMap<String, UmSheet>()
        nominatorToIdMap = HashMap()
        nomineeToIdMap = HashMap()
        for (clazzName in clazzToStudents.keys) {
            val students = clazzToStudents[clazzName]

            val clazzSheet = UmSheet(clazzName)
            val nominating = "Nominating"
            //1st corner is "Nominating"
            clazzSheet.addValueToSheet(0, 0, nominating)


            var r = 0
            var c = 1
            val t = students!!.size

            //Top Nominee Row ( 0th Row) and every X
            for (everyStudent in students) {

                val studentName = everyStudent.person!!.fullName(UstadMobileSystemImpl.instance.getLocale(context))

                clazzSheet.addValueToSheet(0, c, studentName)
                nomineeToIdMap[everyStudent.clazzMemberUid] = c

                c++

            }

            //Every Nominator names
            r = 1
            for (es in students) {
                nominatorToIdMap[es.clazzMemberUid] = r
                val nominatorName = es.person!!.fullName(UstadMobileSystemImpl.instance.getLocale(context))

                clazzSheet.addValueToSheet(r, 0, nominatorName)
                r++

            }

            //Every x cross
            for (j in 1..t) {
                for (k in 1..t) {
                    clazzSheet.addValueToSheet(j, k, CROSS_UNICODE)
                }
            }

            //Every -
            for (j in 1..t) {
                clazzSheet.addValueToSheet(j, j, "-")
            }

            clazzSheetTemplate[clazzName] = clazzSheet
        }
    }

    val z = ZipUtil()

    /**
     * Generates the excel file with th ecurrently set data.
     * @param title             The title of the excel file
     * @param xlsxReportPath    The .xlsx file path (to be created)
     * @param theWorkingPath    The working directory where the xlsx file will be worked on.
     */
    fun dataToXLSX(title: String, xlsxReportPath: String, theWorkingPath: String) {

        try {
            z.createEmptyZipFile(xlsxReportPath)

            val umXLSX = UmXLSX(title, xlsxReportPath, theWorkingPath)

            /*
                Sheet order
                Class A - Question 1 ]- Uses Class A template
                Class A - Question 2 ]
                Class B - Question 1 ]- Uses Class B template
                Class B - Question 2 ]

             */
            val clazzIterator = clazzMap.keys.iterator()
            while (clazzIterator.hasNext()) {

                val clazzName = clazzIterator.next()
                val clazzData = clazzMap[clazzName]
                val questionIterator = clazzData!!.keys.iterator()
                while (questionIterator.hasNext()) {
                    val question = questionIterator.next()
                    val questionData = clazzData[question]

                    val clazzSheet = clazzSheetTemplate[clazzName]
                    var sheetTitle = "$clazzName $question"
                    if (sheetTitle.length > 30) {
                        sheetTitle = sheetTitle.substring(0, 29)
                    }
                    val sheetTitleShort = sheetTitle.replace('?', ' ')

                    val newMap = LinkedHashMap(clazzSheet!!.sheetMap!!)
                    val newValues = ArrayList(clazzSheet!!.getSheetValues())

                    val clazzQuestionSheet = UmSheet(sheetTitleShort,
                            newValues, newMap)

                    val questionDataIterator = questionData!!.keys.iterator()
                    while (questionDataIterator.hasNext()) {
                        val nominatorUid = questionDataIterator.next()
                        val r = nominatorToIdMap[nominatorUid]!!

                        val nomineeList = questionData[nominatorUid]
                        for (nominee in nomineeList!!) {
                            val c = nomineeToIdMap[nominee]!!

                            //Put value
                            clazzQuestionSheet.addValueToSheet(r, c, TICK_UNICODE)
                        }
                    }

                    umXLSX.addSheet(clazzQuestionSheet)
                }
            }

            //Generate the xlsx report from the xlsx object.
            umXLSX.createXLSX()
            view.generateXLSReport(xlsxReportPath)

        } catch (e: IOException) {
            print(e.message)
        }

    }

    companion object {

        val TICK_UNICODE = "\u2713"
        val CROSS_UNICODE = "\u2718"
    }
}
