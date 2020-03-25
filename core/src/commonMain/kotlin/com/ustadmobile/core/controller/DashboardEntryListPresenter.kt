package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.DashboardEntryDao
import com.ustadmobile.core.db.dao.DashboardTagDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.DashboardEntryListView
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.ReportOptionsDetailView
import com.ustadmobile.core.view.ReportOptionsDetailView.Companion.ARG_DASHBOARD_ENTRY_UID
import com.ustadmobile.core.view.ReportOptionsDetailView.Companion.ARG_REPORT_OPTIONS
import com.ustadmobile.core.view.ReportOptionsDetailView.Companion.ARG_REPORT_TYPE
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.DashboardEntry
import com.ustadmobile.lib.db.entities.DashboardEntry.Companion.REPORT_TYPE_SALES_LOG
import com.ustadmobile.lib.db.entities.DashboardEntry.Companion.REPORT_TYPE_SALES_PERFORMANCE
import com.ustadmobile.lib.db.entities.DashboardEntry.Companion.REPORT_TYPE_TOP_LES
import com.ustadmobile.lib.db.entities.DashboardTag
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * Presenter for DashboardEntryList view
 */
class DashboardEntryListPresenter(context: Any, arguments: Map<String, String?>,
                                  view: DashboardEntryListView)
    : UstadBaseController<DashboardEntryListView>(context, arguments!!, view) {

    private var entryProvider: DataSource.Factory<Int, DashboardEntry>? = null
    private var tagProvider: DataSource.Factory<Int, DashboardTag>? = null
    internal var repository: UmAppDatabase
    private val dashboardEntryDao: DashboardEntryDao
    private val tagDao: DashboardTagDao
    private var loggedInPerson: Person? = null
    private var loggedInPersonUid = 0L
    private val personDao: PersonDao
    private lateinit var tagLiveData: DoorLiveData<List<DashboardTag>>

    private var tagToPosition: HashMap<Long, Int>? = null
    private var positionToTag: HashMap<Int, Long>? = null

    private val saleDao: SaleDao

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        dashboardEntryDao = repository.dashboardEntryDao
        tagDao = repository.dashboardTagDao
        personDao = repository.personDao
        saleDao = repository.saleDao

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        val activeAccount = UmAccountManager.getActiveAccount(context)

        if (activeAccount != null) {
            loggedInPersonUid = activeAccount.personUid
            //Get provider
            GlobalScope.launch {
                entryProvider = dashboardEntryDao.findByPersonAndActiveProvider(loggedInPersonUid)
                view.runOnUiThread(Runnable {
                    view.setDashboardEntryProvider(entryProvider!!)
                })

                loggedInPerson = personDao.findByUid(loggedInPersonUid)
                if(loggedInPerson != null){
                    if(loggedInPerson!!.admin){
                        view.runOnUiThread(Runnable {
                            view.showSalesLogOption(true)
                            view.showTopLEsOption(true)
                            view.showSalesPerformanceOption(true)
                        })
                    }else{
                        view.runOnUiThread(Runnable {
                            view.showSalesLogOption(false)
                            view.showTopLEsOption(false)
                            view.showSalesPerformanceOption(true)
                        })
                    }
                }
            }

            GlobalScope.launch {
                tagProvider = tagDao.findAllActiveProvider()
                view.runOnUiThread(Runnable {
                    view.setDashboardTagProvider(tagProvider!!)
                })
            }

            val thisP = this
            //Update location spinner
            tagLiveData = tagDao.findAllActiveLive()
            GlobalScope.launch(Dispatchers.Main){
                tagLiveData.observeWithPresenter(thisP, thisP::handleTagsChanged)
            }
        }
    }

    private fun handleTagsChanged(tags: List<DashboardTag>?) {

        tagToPosition = HashMap()
        positionToTag = HashMap()

        val tagList = ArrayList<String>()
        var pos = 0
        for (el in tags!!) {
            tagList.add(el.dashboardTagTitle!!)
            tagToPosition!![el.dashboardTagUid] = pos
            positionToTag!![pos] = el.dashboardTagUid
            pos++
        }
        var tagPresets = tagList.toTypedArray()
        view.runOnUiThread(Runnable {
            view.loadChips(tagPresets)
        })
    }

    fun handleClickSearch() {
        //TODO
    }

    fun handleClickNewSalePerformanceReport() {
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(ARG_REPORT_TYPE, REPORT_TYPE_SALES_PERFORMANCE.toString())
        impl.go(ReportOptionsDetailView.VIEW_NAME, args, context)

    }

    fun handleClickNewSalesLogReport() {
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(ARG_REPORT_TYPE, REPORT_TYPE_SALES_LOG.toString())
        impl.go(ReportOptionsDetailView.VIEW_NAME, args, context)

    }

    fun handleClickTopLEsReport() {
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(ARG_REPORT_TYPE, REPORT_TYPE_TOP_LES.toString())
        impl.go(ReportOptionsDetailView.VIEW_NAME, args, context)

    }

    /**
     * Primary action on item.
     */
    fun handleAddTag(entryUid: Long, tagUid: Long) {
        //TODO
    }

    /**
     * Secondary action on item.
     */
    fun handleSetTitle(entryUid: Long, title: String) {

        GlobalScope.launch {
            dashboardEntryDao.updateTitle(entryUid, title)
            val a=0
        }
    }

    fun handleDeleteEntry(entryUid: Long) {
        GlobalScope.launch {
            dashboardEntryDao.deleteEntry(entryUid)
        }
    }

    fun handleClickReport(entryUid: Long, reportOptions: String, reportType: Int) {
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(ARG_DASHBOARD_ENTRY_UID, entryUid.toString())
        args.put(ARG_REPORT_OPTIONS, reportOptions)
        args.put(ARG_REPORT_TYPE, reportType.toString())

        impl.go(ReportDetailView.VIEW_NAME, args, context)

    }

    fun handleEditEntry(entryUid: Long) {
        //Go to Report Options with the data here.
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(ARG_DASHBOARD_ENTRY_UID, entryUid.toString())
        impl.go(ReportOptionsDetailView.VIEW_NAME, args, context)
    }

    fun handleChangeTitle(entryUid: Long, existingTitle: String) {
        view.runOnUiThread(Runnable {
            view.showSetTitle(existingTitle, entryUid)
        })
    }

    fun handlePinEntry(entryUid: Long, pinned: Boolean) {
        if (pinned) {
            GlobalScope.launch {
                dashboardEntryDao.unpinEntry(entryUid)
            }
        } else {
            GlobalScope.launch {
                dashboardEntryDao.pinEntry(entryUid)
            }
        }
    }
}
