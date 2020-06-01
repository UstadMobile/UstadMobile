package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.FeedListView
import com.ustadmobile.door.DoorLifecycleOwner
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
class FeedListPresenter(context: Any, arguments: Map<String, String>, view: FeedListView,
                        private val lifecycleOwner: DoorLifecycleOwner,
                        val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<FeedListView>(context, arguments, view) {

    private var loggedInPersonUid = 0L

    private var feedEntryUmProvider: DataSource.Factory<Int, FeedEntry>? = null

    private lateinit var repository: UmAppDatabase

    private lateinit var database: UmAppDatabase

    /**
     * Overridden onCreate in order:
     * 1. Gets the UmProvider types FeedEntry list and sets it as a provider to the view.
     *
     * @param savedState    THE SAVED STATE
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)?.personUid ?: 0L


        database = UmAccountManager.getActiveDatabase(context)
        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        updateFeedEntries()

        //All clazz's average live data
//        val averageLiveData = repository.clazzDao.getClazzSummaryLiveData()
//        averageLiveData.observeWithLifecycleOwner(lifecycleOwner, this::handleAveragesChanged)

        //Check permissions
        checkPermissions()
    }

    private fun updateFeedEntries() {
        feedEntryUmProvider = repository.feedEntryDao.findByPersonUid(loggedInPersonUid).apply {
            view.setFeedEntryProvider(this)
        }
    }

    /**
     * Handles what happens when averages changes. Usually called on live data's onObserve.
     * @param average   The ClazzAverage POJO that changed.
     */
    private fun handleAveragesChanged(average: ClazzAverage?) {
        if(average == null)
            return

        val attendanceAverage = kotlin.math.round(average.attendanceAverage * 100)
        view.updateAttendancePercentage(attendanceAverage.toInt())
        view.updateNumClasses(average.numClazzes)
        view.updateNumStudents(average.numStudents)
    }

    /**
     * Checks permission and updates the view.
     */
    private fun checkPermissions() {
        GlobalScope.launch {
            val hasPermission = repository.clazzDao.personHasPermission(loggedInPersonUid,
                    Role.PERMISSION_REPORTS_VIEW)
            view.runOnUiThread(Runnable {
                view.showReportOptionsOnSummaryCard(hasPermission)
            })
        }

    }

    /**
     * Mark feed as done so it can be dismissed from the feed list.
     * @param feedUid   the feed uid
     */
    fun handleMarkFeedDone(feedUid: Long) {
        GlobalScope.launch {
            repository.feedEntryDao.markEntryAsDoneByFeedEntryUid(feedUid, true)
        }
    }

    /**
     * Takes action on a feed. This splits the feed's link and builds its destination and arguments
     * for it to go to.
     *
     * @param feedEntry The FeedEntry object that was clicked.
     */
    fun handleClickFeedEntry(feedEntry: FeedEntry) {
        val feedLink = feedEntry.link ?: return
        impl.go(feedLink, context)
    }

}
