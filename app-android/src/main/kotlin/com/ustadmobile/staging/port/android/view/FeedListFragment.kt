package com.ustadmobile.staging.port.android.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.FeedListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.FeedListView
import com.ustadmobile.core.view.FeedListView.Companion.FEED_LIST_ATTENDANCE_TREND_DOWN
import com.ustadmobile.core.view.FeedListView.Companion.FEED_LIST_ATTENDANCE_TREND_FLAT
import com.ustadmobile.core.view.FeedListView.Companion.FEED_LIST_ATTENDANCE_TREND_UP
import com.ustadmobile.lib.db.entities.FeedEntry
import com.ustadmobile.port.android.view.UstadBaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * FeedListFragment Android fragment extends UstadBaseFragment - fragment responsible for displaying
 * the feed page and actions on them depending on the feed.
 */
class FeedListFragment : UstadBaseFragment, FeedListView,
        RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    override val viewContext: Any
        get() = context!!

    internal lateinit var rootContainer: View
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var numClassesView: TextView
    private lateinit var numStudentsView: TextView
    private lateinit var attendancePercentageView: TextView
    private lateinit var mPresenter: FeedListPresenter
    private lateinit var summaryCard: CardView
    private lateinit var pullToRefresh: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        rootContainer = inflater.inflate(R.layout.fragment_feed_list, container, false)
        setHasOptionsMenu(true)

        //Set Recycler view
        mRecyclerView = rootContainer.findViewById(R.id.fragment_feed_list_recyclerview)

        //Use Linear Layout Manager : Set layout Manager
        val mRecyclerLayoutManager = LinearLayoutManager(context)
        mRecyclerView.layoutManager = mRecyclerLayoutManager

        //Swipe trial:
        mRecyclerView.itemAnimator = DefaultItemAnimator()
        mRecyclerView.addItemDecoration(DividerItemDecoration(Objects.requireNonNull(context),
                DividerItemDecoration.VERTICAL))

        val itemTouchHelperCallback = RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView)
        //end of Swipe

        numClassesView = rootContainer.findViewById(R.id.fragment_feed_list_report_card_num_classes)
        numStudentsView = rootContainer.findViewById(R.id.fragment_feed_list_report_card_num_students)
        attendancePercentageView = rootContainer.findViewById(R.id.fragment_feed_list_report_card_attendance_percentage)

        summaryCard = rootContainer.findViewById(R.id.fragment_feed_list_report_card)

        pullToRefresh = rootContainer.findViewById(R.id.fragment_feed_list_swiperefreshlayout)

        //Create presenter and call its onCreate()
        mPresenter = FeedListPresenter(context!!, UMAndroidUtil.bundleToMap(
                arguments), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        pullToRefresh.setOnRefreshListener {
            try {
                Thread.sleep(300)
                //TODO: Replace with repo random access sync when it is ready.
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            pullToRefresh.isRefreshing = false
        }


        return rootContainer
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        println("hi")
        val adapter = mRecyclerView!!.adapter as FeedListRecyclerAdapter?
        val feedUid = adapter!!.positionToFeedUid[position]!!
        if (feedUid != 0L) {
            mPresenter!!.markFeedAsDone(feedUid)
        }
        //adapter.notifyItemRemoved(position);


    }

    override fun setFeedEntryProvider(factory: DataSource.Factory<Int, FeedEntry>) {
        val recyclerAdapter = FeedListRecyclerAdapter(this, DIFF_CALLBACK, context!!,
                mPresenter)

        val data = LivePagedListBuilder(factory, 20).build()

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<FeedEntry>> { recyclerAdapter.submitList(it) })
        }

        mRecyclerView.setAdapter(recyclerAdapter)
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
            numClassesView.text = numClassesText
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
            numStudentsView!!.text = numStudentsText
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
        runOnUiThread (Runnable{ attendancePercentageView.text = concatString })
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
            summaryCard.visibility = if (visible) View.VISIBLE else View.GONE
            summaryCard.isEnabled = visible
        })
    }

    override fun onResume() {
        super.onResume()
        //updateTitle(getText(R.string.feed).toString())
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        //updateTitle(getText(R.string.feed).toString())
    }

    // This event is triggered soon after onCreateView().
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Setup any handles to view objects here
        //updateTitle(getText(R.string.feed).toString())

    }

    constructor()  {
        val args = Bundle()
        arguments = args
        icon = R.drawable.ic_today_black_48dp
        title = R.string.bottomnav_feed_title
    }

    constructor(args:Bundle) : this() {
        arguments = args
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

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment ContainerPageFragment.
         */
        fun newInstance(): FeedListFragment {

            val fragment = FeedListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
