package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.FeedListView
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_REPORT_NAME
import com.ustadmobile.core.view.ReportSelectionView
import com.ustadmobile.lib.db.entities.ClazzAverage
import com.ustadmobile.lib.db.entities.FeedEntry
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * The FeedList's Presenter - responsible for the logic to display all feeds and action on opening
 * them to the right place.
 * This presenter is also responsible for generating required feeds when required.
 *
 */
class FeedListPresenter(context: Any, arguments: Map<String, String>?, view: FeedListView,
                        val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<FeedListView>(context, arguments!!, view) {

    private var loggedInPersonUid = 0L

    private var feedEntryUmProvider: DataSource.Factory<Int, FeedEntry>? = null

    private var repository: UmAppDatabase? = null

    /**
     * Overridden onCreate in order:
     * 1. Gets the UmProvider types FeedEntry list and sets it as a provider to the view.
     *
     * @param savedState    THE SAVED STATE
     */
    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

        repository = UmAppDatabase.getInstance(view.viewContext)

        updateFeedEntries()

        //All clazz's average live data
        val averageLiveData = repository!!.clazzDao.getClazzSummaryLiveData()
        averageLiveData.observe(this, this::handleAveragesChanged)

        //Check permissions
        checkPermissions()
    }

    private fun updateFeedEntries() {
        feedEntryUmProvider = repository!!.feedEntryDao.findByPersonUid(loggedInPersonUid)
        updateFeedProviderToView()
    }

    /**
     * Handles what happens when averages changes. Usually called on live data's onObserve.
     * @param average   The ClazzAverage POJO that changed.
     */
    private fun handleAveragesChanged(average: ClazzAverage?) {
        val attendanceAverage = kotlin.math.round(average!!.attendanceAverage * 100)
        view.updateAttendancePercentage(attendanceAverage.toInt())
        view.updateNumClasses(average.numClazzes)
        view.updateNumStudents(average.numStudents)

    }

    /**
     * Checks permission and updates the view.
     */
    private fun checkPermissions() {
        val clazzDao = repository!!.clazzDao
        GlobalScope.launch {
            if(loggedInPersonUid != 0L) {
                val result = clazzDao.personHasPermission(loggedInPersonUid, Role.PERMISSION_REPORTS_VIEW)
                if (result != null) {
                    view.showReportOptionsOnSummaryCard(result!!)
                }
            }
        }

    }

    /**
     * Mark feed as done so it can be dismissed from the feed list.
     * @param feedUid   the feed uid
     */
    fun markFeedAsDone(feedUid: Long) {
        val feedEntryDao = repository!!.feedEntryDao
        GlobalScope.launch {
            val thisFeed = feedEntryDao.findByUidAsync(feedUid)
            if (thisFeed != null) {
                thisFeed.feedEntryDone = true
                feedEntryDao.update(thisFeed)
                updateFeedEntries()
            }
        }

    }

    /**
     * Updates the View with the feed provider set on the Presenter
     */
    private fun updateFeedProviderToView() {
        view.runOnUiThread(Runnable{ view.setFeedEntryProvider(feedEntryUmProvider!!) })
    }

    /**
     * Splits the string query without host name and returns a hash map of it.
     *
     * @param query The string get query without host. eg: clazzuid=22&logdate=123456789
     * @return  A hash table all the query
     */
    private fun splitQuery(query: String): Map<String, String>? {
        val query_pairs = HashMap<String, String>()

        val pairs = query.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            query_pairs.put(pair.substring(0, idx), pair.substring(idx + 1))
        }
        return query_pairs
    }


    /**
     * Goes to Report selection
     */
    fun handleClickViewReports() {
        val args = HashMap<String, String>()
        args.put(ARG_REPORT_NAME, "Test Report")
        impl.go(ReportSelectionView.VIEW_NAME, args, view.viewContext)
    }

    /**
     * Takes action on a feed. This splits the feed's link and builds its destination and arguments
     * for it to go to.
     *
     * @param feedEntry The FeedEntry object that was clicked.
     */
    fun handleClickFeedEntry(feedEntry: FeedEntry) {
        val feedLink = feedEntry.link
        val linkViewName = feedLink!!.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        val args = splitQuery(feedLink.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
        impl.go(linkViewName, args!!, view.viewContext)

    }

}
