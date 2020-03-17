package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzLogDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ClassLogDetailView
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson
import com.ustadmobile.port.android.view.UstadBaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.util.*

/**
 * The ClassLogDetail activity.
 *
 * This Activity extends UstadBaseActivity and implements ClassLogDetailView
 */
class ClazzLogDetailActivity : UstadBaseActivity(), ClassLogDetailView {

    private var toolbar: Toolbar? = null

    private var mRecyclerView: RecyclerView? = null

    private var mPresenter: ClazzLogDetailPresenter? = null

    private var dateHeading: TextView? = null

    private var markAllPresent: Button? = null
    private var markAllAbsent: Button? = null

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * onCreate of the Activity does the following:
     *
     * 1. Gets arguments for clazz log uid, clazz uid, logdate
     * 2. sets the recycler view
     * 3. adds handlers to all buttons on the view
     *
     * @param savedInstanceState    The bundle saved state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Setting layout:
        setContentView(R.layout.activity_clazz_log_detail)

        //Toolbar
        toolbar = findViewById(R.id.class_log_detail_toolbar)
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar)!!.setDisplayHomeAsUpEnabled(true)

        mRecyclerView = findViewById(R.id.class_log_detail_container_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.setLayoutManager(mRecyclerLayoutManager)

        val backDate = findViewById<AppCompatImageButton>(R.id
                .activity_class_log_detail_date_go_back)
        val forwardDate = findViewById<AppCompatImageButton>(R.id
                .activity_class_log_detail_date_go_forward)

        markAllPresent = findViewById(R.id.activity_class_log_detail_mark_all_present_text)
        markAllAbsent = findViewById(R.id.activity_class_log_detail_mark_all_absent_text)

        val fab = findViewById<FloatingTextButton>(R.id.class_log_detail__done_fab)

        //Date heading
        dateHeading = findViewById(R.id.activity_class_log_detail_date_heading)

        mPresenter = ClazzLogDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))


        //Change icon based on rtl in current language (eg: arabic)
        val isLeftToRight = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())
        when (isLeftToRight) {
            ViewCompat.LAYOUT_DIRECTION_RTL -> {
                backDate.setImageDrawable(AppCompatResources.getDrawable(
                        applicationContext, R.drawable.ic_chevron_right_black_24dp))
                forwardDate.setImageDrawable(AppCompatResources.getDrawable(application,
                        R.drawable.ic_chevron_left_black_24dp))
            }
        }

        //FAB
        fab.setOnClickListener { v -> mPresenter!!.handleClickDone() }

        //Mark all present
        markAllPresent!!.setOnClickListener { view -> mPresenter!!.handleMarkAll(ClazzLogAttendanceRecord.STATUS_ATTENDED) }

        //Mark all absent
        markAllAbsent!!.setOnClickListener { view -> mPresenter!!.handleMarkAll(ClazzLogAttendanceRecord.STATUS_ABSENT) }

        backDate.setOnClickListener { v -> mPresenter!!.handleClickGoBackDate() }

        forwardDate.setOnClickListener { v -> mPresenter!!.handleClickGoForwardDate() }

    }

    override fun setClazzLogAttendanceRecordProvider(
            factory: DataSource.Factory<Int, ClazzLogAttendanceRecordWithPerson>) {

        val recyclerAdapter = ClazzLogDetailRecyclerAdapter(DIFF_CALLBACK, applicationContext,
                this, mPresenter!!)

        val data = LivePagedListBuilder(factory, 20).build()

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<ClazzLogAttendanceRecordWithPerson>> { recyclerAdapter.submitList(it) })
        }
        mRecyclerView!!.setAdapter(recyclerAdapter)
    }

    override fun updateToolbarTitle(title: String) {
        runOnUiThread {
            toolbar!!.setTitle(title)
            setSupportActionBar(toolbar)
            Objects.requireNonNull(supportActionBar)!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    /**
     * Sets the dateString to the View
     *
     * @param dateString    The date in readable format that will be set to the ClazzLogDetail view
     */
    override fun updateDateHeading(dateString: String) {
        //Since its called from the presenter, need to run on ui thread.
        runOnUiThread { dateHeading!!.text = dateString }
    }

    override fun showMarkAllButtons(show: Boolean) {
        if (show) {
            markAllAbsent!!.visibility = View.VISIBLE
            markAllPresent!!.visibility = View.VISIBLE
        } else {
            markAllAbsent!!.visibility = View.INVISIBLE
            markAllPresent!!.visibility = View.INVISIBLE
        }
    }

    companion object {

        // Diff callback.
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzLogAttendanceRecordWithPerson> = object
            : DiffUtil.ItemCallback<ClazzLogAttendanceRecordWithPerson>() {
            override fun areItemsTheSame(oldItem: ClazzLogAttendanceRecordWithPerson,
                                         newItem: ClazzLogAttendanceRecordWithPerson): Boolean {
                return oldItem.clazzLogAttendanceRecordUid == newItem.clazzLogAttendanceRecordUid
            }

            override fun areContentsTheSame(oldItem: ClazzLogAttendanceRecordWithPerson,
                                            newItem: ClazzLogAttendanceRecordWithPerson): Boolean {
                return oldItem.attendanceStatus == newItem.attendanceStatus
            }
        }
    }


}
