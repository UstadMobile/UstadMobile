package com.ustadmobile.port.android.view


import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.LocationListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.LocationListView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.LocationWithSubLocationCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.util.*

class LocationListActivity : UstadBaseActivity(), LocationListView {

    private var toolbar: Toolbar? = null
    private var mPresenter: LocationListPresenter? = null
    private var mRecyclerView: RecyclerView? = null


    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_location_list)

        //Toolbar:
        toolbar = findViewById(R.id.activity_location_list_toolbar)
        toolbar!!.setTitle(getText(R.string.locations))
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //RecyclerView
        mRecyclerView = findViewById(R.id.activity_location_list_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.setLayoutManager(mRecyclerLayoutManager)

        //Call the Presenter
        mPresenter = LocationListPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_location_list_fab)

        fab.setOnClickListener { v -> mPresenter!!.handleClickPrimaryActionButton() }


    }

    override fun setListProvider(factory : DataSource.Factory<Int, LocationWithSubLocationCount>) {


        val recyclerAdapter = LocationListRecyclerAdapter(DIFF_CALLBACK, mPresenter!!, this,
                applicationContext)

        // get the provider, set , observe, etc.
        // A warning is expected
        val data = factory.asRepositoryLiveData(UmAccountManager.getRepositoryForActiveAccount(applicationContext!!).locationDao)

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<LocationWithSubLocationCount>> { recyclerAdapter.submitList(it) })
        }
        //set the adapter
        mRecyclerView!!.setAdapter(recyclerAdapter)
    }

    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<LocationWithSubLocationCount> = object
            : DiffUtil.ItemCallback<LocationWithSubLocationCount>() {
            override fun areItemsTheSame(oldItem: LocationWithSubLocationCount,
                                         newItem: LocationWithSubLocationCount): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: LocationWithSubLocationCount,
                                            newItem: LocationWithSubLocationCount): Boolean {
                return oldItem.locationUid == newItem.locationUid
            }
        }
    }
}
