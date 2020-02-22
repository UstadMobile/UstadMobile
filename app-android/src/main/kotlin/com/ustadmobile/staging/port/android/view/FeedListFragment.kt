package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.*
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.FeedListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.FeedListView
import com.ustadmobile.core.view.FeedListView.Companion.FEED_LIST_ATTENDANCE_TREND_DOWN
import com.ustadmobile.core.view.FeedListView.Companion.FEED_LIST_ATTENDANCE_TREND_FLAT
import com.ustadmobile.core.view.FeedListView.Companion.FEED_LIST_ATTENDANCE_TREND_UP
import com.ustadmobile.lib.db.entities.FeedEntry
import com.ustadmobile.port.android.view.UstadBaseFragment
import java.util.*

/**
 * FeedListFragment Android fragment extends UstadBaseFragment - fragment responsible for displaying
 * the feed page and actions on them depending on the feed.
 */
class FeedListFragment : UstadBaseFragment(), FeedListView,
        RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    override val viewContext: Any
        get() = requireContext()

    internal var rootContainer: View? = null
    private var mRecyclerView: RecyclerView? = null
    private var numClassesView: TextView? = null
    private var numStudentsView: TextView? = null
    private var attendancePercentageView: TextView? = null
    private var mPresenter: FeedListPresenter? = null
    private var summaryCard: CardView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        rootContainer = inflater.inflate(R.layout.fragment_feed_list, container, false)
        setHasOptionsMenu(true)

        //Set Recycler view
        mRecyclerView = rootContainer?.findViewById(R.id.fragment_feed_list_recyclerview)

        //Use Linear Layout Manager : Set layout Manager
        val mRecyclerLayoutManager = LinearLayoutManager(context)
        mRecyclerView?.layoutManager = mRecyclerLayoutManager

        //Swipe trial:
        mRecyclerView?.itemAnimator = DefaultItemAnimator()
        mRecyclerView?.addItemDecoration(DividerItemDecoration(Objects.requireNonNull(context),
                DividerItemDecoration.VERTICAL))

        val itemTouchHelperCallback = RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView)
        //end of Swipe

        numClassesView = rootContainer?.findViewById(R.id.fragment_feed_list_report_card_num_classes)
        numStudentsView = rootContainer?.findViewById(R.id.fragment_feed_list_report_card_num_students)
        attendancePercentageView = rootContainer?.findViewById(R.id.fragment_feed_list_report_card_attendance_percentage)

        summaryCard = rootContainer?.findViewById(R.id.fragment_feed_list_report_card)


        //Create presenter and call its onCreate()
        mPresenter = FeedListPresenter(requireContext(), UMAndroidUtil.bundleToMap(
                arguments), this, this)
        mPresenter?.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))



        return rootContainer
    }

    override fun onDestroyView() {
        super.onDestroyView()

        rootContainer = null
        mRecyclerView?.adapter = null
        mRecyclerView  = null
        numClassesView = null
        numStudentsView = null
        attendancePercentageView = null
        mPresenter = null
        summaryCard = null
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        val adapter = mRecyclerView?.adapter as FeedListRecyclerAdapter?
        val feedUid = adapter!!.positionToFeedUid[position]!!
        if (feedUid != 0L) {
            mPresenter?.handleMarkFeedDone(feedUid)
        }
    }

    override fun setFeedEntryProvider(feedEntryUmProvider: DataSource.Factory<Int, FeedEntry>) {
        val presenterVal = mPresenter ?: return

        val recyclerAdapter = FeedListRecyclerAdapter( DIFF_CALLBACK, presenterVal)

        val data = LivePagedListBuilder(feedEntryUmProvider, 20).build()

        data.observe(this, Observer<PagedList<FeedEntry>> {
            recyclerAdapter.submitList(it)
        })


        mRecyclerView?.setAdapter(recyclerAdapter)
    }

    /**
     * Updates the number of classes in the Summary card. View is updated on UI Thread since it is
     * being called from the presenter (probably on another async thread)
     *
     * @param num   The total number of classes
     */
    override fun updateNumClasses(num: Int) {
        runOnUiThread (Runnable{
            val numClassesText = Integer.toString(num)
            numClassesView?.text = numClassesText
        })
    }

    /**
     * Update the number of students on the Summary card. View is updated on UI Thread since it is
     * being called from the presenter (probably on another async thread)
     *
     * @param num   The total number of students
     */
    override fun updateNumStudents(num: Int) {
        runOnUiThread (Runnable{
            val numStudentsText = Integer.toString(num)
            numStudentsView?.text = numStudentsText
        })
    }

    /**
     * Update the number of attendance percentage of the Summary card. View is updated on UI Thread
     * since it is bring called from the presenter (probably on another async thread)
     *
     * @param per   The percentage value in double - triple digits (ie: 42 or 100)
     */
    override fun updateAttendancePercentage(per: Int) {
        val concatString = "$per%"
        runOnUiThread (Runnable{ attendancePercentageView?.text = concatString })
    }

    override fun updateAttendanceTrend(trend: Int, per: Int) {
        when (trend) {
            FEED_LIST_ATTENDANCE_TREND_UP -> {
            }
            FEED_LIST_ATTENDANCE_TREND_DOWN -> {
            }
            FEED_LIST_ATTENDANCE_TREND_FLAT -> {
            }
            else -> {
            }
        }
    }

    override fun showReportOptionsOnSummaryCard(visible: Boolean) {}

    override fun showSummaryCard(visible: Boolean) {
        runOnUiThread (Runnable{
            summaryCard?.visibility = if (visible) View.VISIBLE else View.GONE
            summaryCard?.isEnabled = visible
        })
    }

    companion object {

        /**
         * The Diff callback
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<FeedEntry> = object : DiffUtil.ItemCallback<FeedEntry>() {
            override fun areItemsTheSame(oldItem: FeedEntry,
                                         newItem: FeedEntry): Boolean {
                return oldItem.feedEntryHash == newItem.feedEntryHash
            }

            override fun areContentsTheSame(oldItem: FeedEntry,
                                            newItem: FeedEntry): Boolean {
                return oldItem.feedEntryUid == newItem.feedEntryUid
            }
        }

        val icon = R.drawable.ic_today_black_48dp

        val title = R.string.bottomnav_feed_title
    }
}
