package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.db.dao.FeedEntryDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UmCallbackWithDefaultValue
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.FeedListView
import com.ustadmobile.core.view.ReportSelectionView
import com.ustadmobile.lib.db.entities.ClazzAverage
import com.ustadmobile.lib.db.entities.FeedEntry
import com.ustadmobile.lib.db.entities.Role



import com.ustadmobile.core.view.ReportEditView.Companion.ARG_REPORT_NAME

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

    private var feedEntryUmProvider: UmProvider<FeedEntry>? = null

    private var repository: UmAppDatabase? = null

    /**
     * Overridden onCreate in order:
     * 1. Gets the UmProvider types FeedEntry list and sets it as a provider to the view.
     *
     * @param savedState    THE SAVED STATE
     */
    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

        repository = UmAppDatabase.getInstance(view.getContext())

        updateFeedEntries()

        //All clazz's average live data
        val averageLiveData = repository!!.clazzDao.clazzSummaryLiveData
        averageLiveData.observe(this@FeedListPresenter,
                UmObserver<ClazzAverage> { this@FeedListPresenter.handleAveragesChanged(it) })

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
    private fun handleAveragesChanged(average: ClazzAverage) {
        val attendanceAverage = Math.round(average.attendanceAverage * 100)
        view.updateAttendancePercentage(attendanceAverage)
        view.updateNumClasses(average.numClazzes)
        view.updateNumStudents(average.numStudents)
        //TODOne: Update with attendance trend.
        //Update: Not part of new Sprint design.
        //view.updateAttendanceTrend(0, 0);

        checkPermissions()
    }

    /**
     * Checks permission and updates the view.
     */
    fun checkPermissions() {
        val clazzDao = repository!!.clazzDao
        clazzDao.personHasPermission(loggedInPersonUid, Role.PERMISSION_REPORTS_VIEW,
                UmCallbackWithDefaultValue(false, object : UmCallback<Boolean> {
                    override fun onSuccess(result: Boolean?) {
                        view.showReportOptionsOnSummaryCard(result!!)
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
        )
    }

    /**
     * Mark feed as done so it can be dismissed from the feed list.
     * @param feedUid   the feed uid
     */
    fun markFeedAsDone(feedUid: Long) {
        val feedEntryDao = repository!!.feedEntryDao
        feedEntryDao.findByUidAsync(feedUid, object : UmCallback<FeedEntry> {
            override fun onSuccess(thisFeed: FeedEntry?) {
                if (thisFeed != null) {
                    thisFeed.isFeedEntryDone = true
                    feedEntryDao.update(thisFeed)
                    updateFeedEntries()
                }
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })

    }

    /**
     * Updates the View with the feed provider set on the Presenter
     */
    private fun updateFeedProviderToView() {
        view.runOnUiThread({ view.setFeedEntryProvider(feedEntryUmProvider!!) })
    }

    /**
     * Splits the string query without host name and returns a hash map of it.
     *
     * @param query The string get query without host. eg: clazzuid=22&logdate=123456789
     * @return  A hash table all the query
     */
    private fun splitQuery(query: String): Map<String, String>? {
        val query_pairs = Hashtable<String, String>()

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
        val args = Hashtable<String, String>()
        args.put(ARG_REPORT_NAME, "Test Report")
        impl.go(ReportSelectionView.VIEW_NAME, args, view.getContext())
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
        impl.go(linkViewName, args, view.getContext())

    }

}
