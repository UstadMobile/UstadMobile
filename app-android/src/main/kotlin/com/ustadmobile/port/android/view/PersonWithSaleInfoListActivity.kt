package com.ustadmobile.port.android.view

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.PersonWithSaleInfoListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonWithSaleInfoListView
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PersonWithSaleInfoListActivity : UstadBaseActivity(), PersonWithSaleInfoListView{


    private lateinit var toolbar: Toolbar
    private lateinit var mPresenter: PersonWithSaleInfoListPresenter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var sortSpinner: AppCompatSpinner
    private lateinit var searchView:SearchView


    internal lateinit var sortSpinnerPresets: Array<String?>

    override fun setWEListFactory(factory: DataSource.Factory<Int, PersonWithSaleInfo>) {
        val recyclerAdapter = PersonWithSaleInfoRecyclerAdapter(DIFF_CALLBACK, mPresenter!!,
                false, false,this, this)
        val data = LivePagedListBuilder(factory, 20).build()
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP, Observer<PagedList<PersonWithSaleInfo>>
                { recyclerAdapter.submitList(it) })
        }

        mRecyclerView.adapter = recyclerAdapter
    }

    override fun updateSortSpinner(presets: Array<String?>) {
        this.sortSpinnerPresets = presets
        val adapter = ArrayAdapter(this,
                R.layout.item_simple_spinner_gray, sortSpinnerPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.action_search).actionView as SearchView

        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(componentName))
        searchView.queryHint = getText(R.string.name)

        searchView.maxWidth = Integer.MAX_VALUE

        // listening to search query text change
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {

                // filter recycler view when query submitted
                mPresenter.handleSearchQuery(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                // filter recycler view when query submitted
                mPresenter.handleSearchQuery(query)
                return false
            }
        })

        searchView.setOnCloseListener {
            val query=""
            // filter recycler view when query submitted
            mPresenter.handleSearchQuery(query)
            false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_person_with_sale_info_list)

        toolbar = findViewById(R.id.activity_person_with_saleinfo_list_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_person_with_saleinfo_list_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = mRecyclerLayoutManager

        sortSpinner = findViewById(R.id.activity_person_with_saleinfo_list_spinner)

        //Call the presenter
        val impl = UstadMobileSystemImpl.instance
        mPresenter = PersonWithSaleInfoListPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this, impl)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Listeners
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter.handleSortChanged(position.toLong())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


    }

    companion object{
        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithSaleInfo> = object
            : DiffUtil.ItemCallback<PersonWithSaleInfo>() {
            override fun areItemsTheSame(oldItem: PersonWithSaleInfo,
                                         newItem: PersonWithSaleInfo): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: PersonWithSaleInfo,
                                            newItem: PersonWithSaleInfo): Boolean {
                return oldItem.personUid == newItem.personUid
            }
        }
    }
}