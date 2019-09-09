package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.controller.ReportOverallAttendancePresenter.Companion.convertLongArray
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.AuditLogDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AuditLogListView
import com.ustadmobile.core.view.AuditLogSelectionView.Companion.ARG_AUDITLOG_ACTOR_LIST
import com.ustadmobile.core.view.AuditLogSelectionView.Companion.ARG_AUDITLOG_CLASS_LIST
import com.ustadmobile.core.view.AuditLogSelectionView.Companion.ARG_AUDITLOG_FROM_TIME
import com.ustadmobile.core.view.AuditLogSelectionView.Companion.ARG_AUDITLOG_LOCATION_LIST
import com.ustadmobile.core.view.AuditLogSelectionView.Companion.ARG_AUDITLOG_PEOPLE_LIST
import com.ustadmobile.core.view.AuditLogSelectionView.Companion.ARG_AUDITLOG_TO_TIME
import com.ustadmobile.lib.db.entities.AuditLogWithNames
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Presenter for AuditLogList view
 */
class AuditLogListPresenter(context: Any, arguments: Map<String, String>?, view:
AuditLogListView, val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<AuditLogListView>(context, arguments!!, view) {

    private var umProvider: DataSource.Factory<Int, AuditLogWithNames>? = null
    internal var repository: UmAppDatabase
    private val providerDao: AuditLogDao
    private var fromDate: Long = 0
    private var toDate: Long = 0
    private var locationList: List<Long>? = null
    private var clazzesList: List<Long>? = null
    private var peopleList: List<Long>? = null
    private var actorList: List<Long>? = null


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        locationList = ArrayList()
        clazzesList = ArrayList()
        peopleList = ArrayList()
        actorList = ArrayList()

        //Get provider Dao
        providerDao = repository.auditLogDao

        if (arguments!!.containsKey(ARG_AUDITLOG_FROM_TIME)) {
            fromDate = arguments!!.get(ARG_AUDITLOG_FROM_TIME)!!.toLong()
        }
        if (arguments!!.containsKey(ARG_AUDITLOG_TO_TIME)) {
            toDate = arguments!!.get(ARG_AUDITLOG_TO_TIME)!!.toLong()
        }

        //TODO: Chcek if can cast to LongArray Just like that. It should be parsed BACK
        if (arguments!!.containsKey(ARG_AUDITLOG_LOCATION_LIST)) {
            val locations = arguments!!.get(ARG_AUDITLOG_LOCATION_LIST) as LongArray
            locationList = convertLongArray(locations)
        }
        if (arguments!!.containsKey(ARG_AUDITLOG_CLASS_LIST)) {
            val clazzes = arguments!!.get(ARG_AUDITLOG_CLASS_LIST) as LongArray
            clazzesList = convertLongArray(clazzes)
        }

        if (arguments!!.containsKey(ARG_AUDITLOG_PEOPLE_LIST)) {
            val people = arguments!!.get(ARG_AUDITLOG_PEOPLE_LIST) as LongArray
            peopleList = convertLongArray(people)
        }

        if (arguments!!.containsKey(ARG_AUDITLOG_ACTOR_LIST)) {
            val actors = arguments!!.get(ARG_AUDITLOG_ACTOR_LIST) as LongArray
            actorList = convertLongArray(actors)
        }

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        //Get provider
        umProvider = providerDao.findAllAuditLogsWithNameFilter(fromDate, toDate, locationList!!,
                clazzesList!!, peopleList!!, actorList!!)
        view.setListProvider(umProvider!!)

    }

    fun dataToCSV() {

        //Get all as list
        val data = ArrayList<Array<String>>()

        val changedString = impl.getString(MessageID.changed, context)
        val clazzType = impl.getString(MessageID.clazz, context)
        val personType = impl.getString(MessageID.person, context)

        GlobalScope.launch {
            val result = providerDao.findAllAuditLogsWithNameFilterList(fromDate, toDate,
                    locationList!!, clazzesList!!, peopleList!!, actorList!!)
            for (entity in result!!) {
                //"Actor changed Entity Type Entity Name at Time"
                var entityType = ""
                var entityName: String? = ""
                when (entity.auditLogTableUid) {
                    Clazz.TABLE_ID -> {
                        entityType = clazzType
                        entityName = entity.clazzName
                    }
                    Person.TABLE_ID -> {
                        entityType = personType
                        entityName = entity.personName
                    }
                    else -> {
                    }
                }
                val logString = entity.actorName + " " + changedString + " " +
                        entityType + " " + entityName
                val a = arrayOf(logString)
                data.add(a)

            }
            view.generateCSVReport(data)
        }


    }

}
