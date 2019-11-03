package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectProducerPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SelectProducerView
import com.ustadmobile.lib.db.entities.Person

class SelectProducerActivity : UstadBaseActivity(), SelectProducerView {

    private var toolbar: Toolbar? = null
    private var mPresenter: SelectProducerPresenter? = null
    private var mRecyclerView: RecyclerView? = null

    private var sortSpinner: Spinner? = null
    internal lateinit var sortSpinnerPresets: Array<String?>


    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)

        return true
    }

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
        setContentView(R.layout.activity_select_producer)

        //Toolbar:
        toolbar = findViewById(R.id.activity_select_producer_toolbar)
        toolbar!!.title = getText(R.string.select_producer)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        sortSpinner = findViewById(R.id.activity_select_producer_spinner)

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_select_producer_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        //Call the Presenter
        mPresenter = SelectProducerPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Sort spinner handler
        sortSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter!!.handleChangeSortOrder(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

    }

    override fun setListProvider(factory: DataSource.Factory<Int, Person>) {
        val recyclerAdapter = SelectProducerRecyclerAdapter(DIFF_CALLBACK, mPresenter!!, this,
                applicationContext)

        // get the provider, set , observe, etc.
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        data.observe(this, Observer<PagedList<Person>> { recyclerAdapter.submitList(it) })

        //set the adapter
        mRecyclerView!!.adapter = recyclerAdapter
    }

    override fun updateSpinner(presets: Array<String?>) {
        this.sortSpinnerPresets = presets
        val adapter = ArrayAdapter(this,
                R.layout.item_simple_spinner_gray, sortSpinnerPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner!!.adapter = adapter
    }

    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Person> = object : DiffUtil.ItemCallback<Person>() {
            override fun areItemsTheSame(oldItem: Person,
                                         newItem: Person): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Person,
                                            newItem: Person): Boolean {
                return oldItem == newItem
            }
        }
    }
}
