package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.CallPersonRelatedDialogView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.ReportAtRiskStudentsView
import com.ustadmobile.core.xlsx.UmSheet
import com.ustadmobile.core.xlsx.UmXLSX
import com.ustadmobile.core.xlsx.ZipUtil
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.PersonWithEnrollment

import com.ustadmobile.core.controller.ReportOverallAttendancePresenter.Companion.convertLongArray
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_CLAZZ_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_FROM_DATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_GENDER_DISAGGREGATE
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_LOCATION_LIST
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_TO_DATE

class ReportAtRiskStudentsPresenter(context: Any, arguments: Map<String, String>?,
                                view: ReportAtRiskStudentsView,
                                val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance()) :
        CommonHandlerPresenter<ReportAtRiskStudentsView>(context, arguments!!, view) {

    private var clazzList: List<Long>? = null
    private var locationList: List<Long>? = null
    private var genderDisaggregated = true

    private var atRiskStudentsUmProvider: UmProvider<PersonWithEnrollment>? = null

    private val dataMapsMap: LinkedHashMap<String, List<PersonWithEnrollment>>

    internal var repository: UmAppDatabase

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        dataMapsMap = LinkedHashMap()
        clazzList = ArrayList()
        locationList = ArrayList()
        if (arguments!!.containsKey(ARG_FROM_DATE)) {
            val fromDate = arguments!!.get(ARG_FROM_DATE) as Long
        }
        if (arguments!!.containsKey(ARG_TO_DATE)) {
            val toDate = arguments!!.get(ARG_TO_DATE) as Long
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
        getDataAndUpdateView()
    }

    fun dataToCSV() {

        val clazzDao = repository.clazzDao
        val clazzMemberDao = repository.clazzMemberDao

        val reportData = ArrayList<Array<String>>()
        reportData.add(arrayOf("Class", "Name", "Attendance"))

        //1. Build a list of all classes for this report. This comes from both the location
        // and the specified clazz list.
        clazzDao.findAllClazzesByLocationAndUidList(locationList!!, clazzList!!,
                object : UmCallback<List<Clazz>> {
                    override fun onSuccess(clazzList: List<Clazz>?) {

                        //build a long list of classes.
                        val clazzUidList = ArrayList<Long>()
                        for (everyClazz in clazzList!!) {
                            clazzUidList.add(everyClazz.clazzUid)
                        }

                        val clazzDone = ArrayList<String>()
                        //Run Live Data Query
                        clazzMemberDao.findAllStudentsAtRiskForClazzList(clazzUidList,
                                RISK_THRESHOLD, object : UmCallback<List<PersonWithEnrollment>> {
                            override fun onSuccess(result: List<PersonWithEnrollment>?) {
                                for (pwe in result!!) {
                                    if (!clazzDone.contains(pwe.clazzName)) {
                                        clazzDone.add(pwe.clazzName)
                                        reportData.add(arrayOf(pwe.clazzName))
                                    }
                                    val name = pwe.firstNames + " " + pwe.lastName
                                    val attendance = pwe.attendancePercentage.toString()
                                    reportData.add(arrayOf("", name, attendance))
                                }

                                view.setTableTextData(reportData)
                                view.generateCSVReport()
                            }

                            override fun onFailure(exception: Throwable?) {
                                print(exception!!.message)
                            }
                        })
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
    }

    /**
     * Queries database, gets raw report data, updates view. The Guts of the logic
     */
    private fun getDataAndUpdateView() {

        val clazzDao = repository.clazzDao
        val clazzMemberDao = repository.clazzMemberDao

        //1. Build a list of all classes for this report. This comes from both the location
        // and the specified clazz list.
        clazzDao.findAllClazzesByLocationAndUidList(locationList!!, clazzList!!,
                object : UmCallback<List<Clazz>> {
                    override fun onSuccess(clazzList: List<Clazz>?) {

                        //build a long list of classes.
                        val clazzUidList = ArrayList<Long>()
                        for (everyClazz in clazzList!!) {
                            clazzUidList.add(everyClazz.clazzUid)
                        }

                        //Run Live Data Query
                        atRiskStudentsUmProvider = clazzMemberDao.findAllStudentsAtRiskForClazzList(clazzUidList,
                                RISK_THRESHOLD)
                        updateProviderToView()
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
    }

    /**
     * Sets provider to the view
     */
    private fun updateProviderToView() {
        view.setReportProvider(atRiskStudentsUmProvider!!)
    }

    override fun handleCommonPressed(arg: Any) {
        val args = Hashtable<String, Any>()
        args.put(ARG_PERSON_UID, arg as Long)
        impl.go(PersonDetailView.VIEW_NAME, args, view.getContext())

    }

    override fun handleSecondaryPressed(arg: Any) {
        val personWithEnrollment = arg as PersonWithEnrollment
        val args = HashMap<String, String>()
        args.put(ARG_PERSON_UID, personWithEnrollment.personUid)
        args.put(ARG_CLAZZ_UID, personWithEnrollment.clazzUid)
        impl.go(CallPersonRelatedDialogView.VIEW_NAME, args, view.getContext())
    }

    fun dataToXLSX(title: String, xlsxReportPath: String, workingDir: String) {

        val clazzDao = repository.clazzDao
        val clazzMemberDao = repository.clazzMemberDao

        val tableTextData = ArrayList<Array<String>>()

        try {
            ZipUtil.createEmptyZipFile(xlsxReportPath)

            val umXLSX = UmXLSX(title, xlsxReportPath, workingDir)

            val reportSheet = UmSheet("Report")

            reportSheet.addValueToSheet(0, 0, "Class")
            reportSheet.addValueToSheet(0, 1, "Name")
            reportSheet.addValueToSheet(0, 2, "Attendance")

            //Loop over tableTextData
            val r = intArrayOf(1)

            //1. Build a list of all classes for this report. This comes from both the location
            // and the specified clazz list.
            clazzDao.findAllClazzesByLocationAndUidList(locationList!!, clazzList!!,
                    object : UmCallback<List<Clazz>> {
                        override fun onSuccess(clazzList: List<Clazz>?) {

                            //build a long list of classes.
                            val clazzUidList = ArrayList<Long>()
                            for (everyClazz in clazzList!!) {
                                clazzUidList.add(everyClazz.clazzUid)
                            }

                            val clazzDone = ArrayList<String>()
                            //Run Live Data Query
                            clazzMemberDao.findAllStudentsAtRiskForClazzList(clazzUidList,
                                    RISK_THRESHOLD, object : UmCallback<List<PersonWithEnrollment>> {
                                override fun onSuccess(result: List<PersonWithEnrollment>?) {
                                    for (pwe in result!!) {
                                        if (!clazzDone.contains(pwe.clazzName)) {
                                            clazzDone.add(pwe.clazzName)
                                            tableTextData.add(arrayOf(pwe.clazzName))
                                        }
                                        val name = pwe.firstNames + " " + pwe.lastName
                                        val attendance = pwe.attendancePercentage.toString()
                                        tableTextData.add(arrayOf("", name, attendance))
                                    }

                                    for (tableTextDatum in tableTextData) {
                                        var c = 0
                                        for (i in tableTextDatum.indices) {
                                            val value = tableTextDatum[i]
                                            reportSheet.addValueToSheet(r[0], c, value)
                                            c++
                                        }
                                        r[0]++
                                    }
                                    umXLSX.addSheet(reportSheet)

                                    //Generate the xlsx report from the xlsx object.
                                    umXLSX.createXLSX()
                                    view.generateXLSXReport(xlsxReportPath)

                                }

                                override fun onFailure(exception: Throwable?) {
                                    print(exception!!.message)
                                }
                            })
                        }

                        override fun onFailure(exception: Throwable?) {
                            print(exception!!.message)
                        }
                    })

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object {
        private val RISK_THRESHOLD = 0.4f
    }
}
