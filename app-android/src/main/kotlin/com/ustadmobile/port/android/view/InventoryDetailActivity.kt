package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.toughra.ustadmobile.R

import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.core.controller.InventoryDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.InventoryDetailView
import com.ustadmobile.lib.db.entities.InventoryTransactionDetail


class InventoryDetailActivity : UstadBaseActivity(), InventoryDetailView {

    private var toolbar: Toolbar? = null
    private var mPresenter: InventoryDetailPresenter? = null

    // If you have a recycler view 
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

    override fun updateTotalInventoryCount(count: Int){

    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_inventory_detail)

        //Toolbar:
        toolbar = findViewById(R.id.activity_inventory_detail_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_inventory_detail_rv)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        //Call the Presenter
        mPresenter = InventoryDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))


    }

    override fun setListProvider(factory: DataSource.Factory<Int, InventoryTransactionDetail>) {
        val recyclerAdapter = InventoryDetailRecyclerAdapter(DIFF_CALLBACK, mPresenter!!, this,
                applicationContext)

        // get the provider, set , observe, etc.
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        data.observe(this, Observer<PagedList<InventoryTransactionDetail>> {
            recyclerAdapter.submitList(it) })

        //set the adapter
        mRecyclerView!!.adapter = recyclerAdapter
    }


    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<InventoryTransactionDetail> =
                object : DiffUtil.ItemCallback<InventoryTransactionDetail>() {
            override fun areItemsTheSame(oldItem: InventoryTransactionDetail,
                                         newItem: InventoryTransactionDetail): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: InventoryTransactionDetail,
                                            newItem: InventoryTransactionDetail): Boolean {
                return oldItem == newItem
            }
        }

    }
}
