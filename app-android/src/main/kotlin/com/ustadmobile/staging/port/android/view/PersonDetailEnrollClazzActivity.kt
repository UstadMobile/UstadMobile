package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.PersonDetailEnrollClazzPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.PersonDetailEnrollClazzView
import com.ustadmobile.lib.db.entities.ClazzWithEnrollment
import com.ustadmobile.port.android.view.UstadBaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.util.*

/**
 * This activity opens the page that enrolls a class to the person. Usually triggered from the
 * Person edit page.
 */
class PersonDetailEnrollClazzActivity : UstadBaseActivity(), PersonDetailEnrollClazzView {

    private var mRecyclerView: RecyclerView? = null

    private var mPresenter: PersonDetailEnrollClazzPresenter? = null


    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == android.R.id.home) {
            onBackPressed()
            return true

        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * In Order:
     * 1. Sets up recycler view of the class list.
     * 2. Calls the presenter's onCreate
     * 3. Sets the floating action button's onClick listener to handle click done.
     *
     * @param savedInstanceState    the Android bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Set layout
        setContentView(R.layout.activity_clazz_list_enroll_person)

        //Toolbar
        val toolbar = findViewById<Toolbar>(R.id.activity_clazz_list_enroll_person_toolbar)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        mRecyclerView = findViewById(R.id.activity_clazz_list_enroll_person_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.setLayoutManager(mRecyclerLayoutManager)

        //Call the presenter
        mPresenter = PersonDetailEnrollClazzPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB
        findViewById<FloatingTextButton>(R.id.activity_clazz_list_enroll_person_fab).setOnClickListener(
                { v ->
                    mPresenter!!.handleClickDone()
                })
    }


    override fun setClazzListProvider(factory: DataSource.Factory<Int, ClazzWithEnrollment>) {

        val recyclerAdapter = ClazzListEnrollPersonRecyclerAdapter(DIFF_CALLBACK, applicationContext,
                this, mPresenter!!)

        //A warning is expected
        val data = LivePagedListBuilder(factory, 20).build()

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<ClazzWithEnrollment>> { recyclerAdapter.submitList(it) })
        }


        mRecyclerView!!.setAdapter(recyclerAdapter)
    }

    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzWithEnrollment> = object : DiffUtil.ItemCallback<ClazzWithEnrollment>() {
            override fun areItemsTheSame(oldItem: ClazzWithEnrollment,
                                         newItem: ClazzWithEnrollment): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }

            override fun areContentsTheSame(oldItem: ClazzWithEnrollment,
                                            newItem: ClazzWithEnrollment): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }
        }
    }

}
