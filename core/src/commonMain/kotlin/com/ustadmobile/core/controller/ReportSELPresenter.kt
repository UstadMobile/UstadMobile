package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzMemberDao
import com.ustadmobile.core.db.dao.SelQuestionResponseNominationDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.view.ReportSELView
import com.ustadmobile.core.xlsx.UmSheet
import com.ustadmobile.core.xlsx.UmXLSX
import com.ustadmobile.core.xlsx.ZipUtil
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson
import com.ustadmobile.lib.db.entities.SELNominationItem

import com.ustadmobile.core.controller.ReportOverallAttendancePresenter.Companion.convertLongArray
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_CLAZZ_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_FROM_DATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_TO_DATE

class ReportSELPresenter(context: Any, arguments: Map<String, String>?, view: ReportSELView) :
        UstadBaseController<ReportSELView>(context, arguments!!, view) {

    private var fromDate: Long = 0
    private var toDate: Long = 0
    private var clazzList: List<Long>? = null
    internal var repository: UmAppDatabase
    internal var clazzMap: LinkedHashMap<String, LinkedHashMap<String, Map<Long, List<Long>>>>
    internal var clazzToStudents: HashMap<String, List<ClazzMemberWithPerson>>
    internal var clazzSheetTemplate: HashMap<String, UmSheet>
    internal var nominatorToIdMap: HashMap<Long, Int>
    internal var nomineeToIdMap: HashMap<Long, Int>


    init {
        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        clazzList = ArrayList()

        if (arguments!!.containsKey(ARG_FROM_DATE)) {
            fromDate = arguments!!.get(ARG_FROM_DATE)
        }
        if (arguments!!.containsKey(ARG_TO_DATE)) {
            toDate = arguments!!.get(ARG_TO_DATE)
        }
        if (arguments!!.containsKey(ARG_CLAZZ_LIST)) {
            val clazzes = arguments!!.get(ARG_CLAZZ_LIST) as LongArray
            clazzList = convertLongArray(clazzes)
        }

    }

    fun onCreate(savedState: Map<String, String>?) {
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

        val nominationDao = repository.getSocialNominationQuestionResponseNominationDao()

        //Get all nominations
        nominationDao.getAllNominationReportAsync(fromDate, toDate, clazzList,
                object : UmCallback<List<SELNominationItem>> {
                    override fun onSuccess(allNominations: List<SELNominationItem>?) {

                        //TODO: Handle QuestionSet grouping ? (Not implemented in Prototypes)
                        var index = 0
                        for (everyNomination in allNominations!!) { //For every nomination / all
                            index++
                            val thisClazzUid = everyNomination.clazzUid
                            val thisClazzName = everyNomination.clazzName
                            val thisQuestionTitle = everyNomination.questionText
                            val nomineeUid = everyNomination.nomineeUid
                            val nominatorUid = everyNomination.nominatorUid

                            val questionMap: LinkedHashMap<String, Map<Long, List<Long>>>?
                            val nominationMap: MutableMap<Long, List<Long>>?
                            val nominations: MutableList<Long>?

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
                            questionMap[thisQuestionTitle] = nominationMap

                            clazzMap[thisClazzName] = questionMap

                            //Build students map - Add students
                            if (!clazzToStudents.containsKey(thisClazzName)) {

                                val finalIndex = index
                                clazzMemberDao.findClazzMemberWithPersonByRoleForClazzUid(thisClazzUid,
                                        ClazzMember.ROLE_STUDENT, object : UmCallback<List<ClazzMemberWithPerson>> {
                                    override fun onSuccess(result: List<ClazzMemberWithPerson>?) {
                                        clazzToStudents[thisClazzName] = result

                                        //If end of the loop
                                        if (finalIndex >= allNominations.size) {
                                            createTablesOnView()
                                            createClassSheetTemplates()
                                        }
                                    }

                                    override fun onFailure(exception: Throwable?) {
                                        print(exception!!.message)
                                    }
                                })
                            } else {
                                //If end of the loop
                                if (index >= allNominations.size) {
                                    createTablesOnView()
                                    createClassSheetTemplates()
                                }
                            }
                        }
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
    }

    /**
     * Send sel raw data to view for table updating.
     */
    private fun createTablesOnView() {
        view.runOnUiThread({ view.createTables(clazzMap, clazzToStudents) })
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

                val studentName = everyStudent.person!!.firstNames + " " +
                        everyStudent.person!!.lastName

                clazzSheet.addValueToSheet(0, c, studentName)
                nomineeToIdMap[everyStudent.clazzMemberUid] = c

                c++

            }

            //Every Nominator names
            r = 1
            for (es in students) {
                nominatorToIdMap[es.clazzMemberUid] = r
                val nominatorName = es.person!!.firstNames + " " +
                        es.person!!.lastName

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

    /**
     * Generates the excel file with th ecurrently set data.
     * @param title             The title of the excel file
     * @param xlsxReportPath    The .xlsx file path (to be created)
     * @param theWorkingPath    The working directory where the xlsx file will be worked on.
     */
    fun dataToXLSX(title: String, xlsxReportPath: String, theWorkingPath: String) {

        try {
            ZipUtil.createEmptyZipFile(xlsxReportPath)

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

                    val newMap = LinkedHashMap(clazzSheet!!.getSheetMap())
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
            e.printStackTrace()
        }

    }

    companion object {

        val TICK_UNICODE = "\u2713"
        val CROSS_UNICODE = "\u2718"
    }
}
