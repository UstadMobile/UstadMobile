package com.ustadmobile.port.android.view

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView

import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleListSearchPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SaleListSearchView
import com.ustadmobile.lib.db.entities.SaleListDetail

import java.text.DecimalFormat
import java.util.Objects

class SaleListSearchActivity : UstadBaseActivity(), SaleListSearchView {

    private var mPresenter: SaleListSearchPresenter? = null
    private var mRecyclerView: RecyclerView? = null
    private var searchView: SearchView? = null
    private var locationSpinner: Spinner? = null

    private var apl = 0
    private var aph = 0

    private var currentValue = ""

    private var fromDate: Long = 0
    private var toDate: Long = 0
    internal lateinit var dateRangeET: EditText
    internal lateinit var valueRangeTV: TextView

    private var sortSpinner: Spinner? = null
    internal lateinit var sortSpinnerPresets: Array<String>

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        } else if (id == R.id.action_search) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        // close search view on back button pressed

        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.action_search)
                .actionView as SearchView

        searchView!!.isFocusable = true
        searchView!!.isIconified = false
        searchView!!.requestFocusFromTouch()
        searchView!!.setSearchableInfo(searchManager
                .getSearchableInfo(componentName))
        searchView!!.queryHint = getText(R.string.product_name)

        searchView!!.maxWidth = Integer.MAX_VALUE

        // listening to search query text change
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                currentValue = query
                // filter recycler view when query submitted
                mPresenter!!.updateFilter(currentValue)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                currentValue = query
                // filter recycler view when text is changed
                mPresenter!!.updateFilter(currentValue)
                return false
            }
        })

        searchView!!.setOnCloseListener {
            currentValue = ""
            mPresenter!!.updateFilter(currentValue)
            false
        }


        return true
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_sale_list_search)

        //Toolbar:
        val toolbar = findViewById<Toolbar>(R.id.activity_sale_list_search_toolbar)
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar).setDisplayHomeAsUpEnabled(true)

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_sale_list_search_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        locationSpinner = findViewById(R.id.activity_sale_list_search_location_spinner)
        dateRangeET = findViewById(R.id.activity_sale_list_search_date_range_edittext)
        dateRangeET.isFocusable = false
        val valueSeekbar = findViewById(R.id.activity_sale_list_search_value_seekbar)

        valueRangeTV = findViewById(R.id.activity_sale_list_search_value_range_textview)
        sortSpinner = findViewById(R.id.activity_sale_list_search_sort_spinner)

        //Call the Presenter
        mPresenter = SaleListSearchPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        locationSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter!!.handleLocationSelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        valueSeekbar.setMaxValue(100000)
        valueSeekbar.setMinValue(0)
        valueSeekbar.setOnRangeSeekbarChangeListener({ minValue, maxValue ->
            updateValueRangeOnView(minValue, maxValue)
            apl = minValue
            aph = maxValue

            mPresenter!!.updateFilter(apl, aph, currentValue)
        })

        updateValueRangeOnView(0, 100000)

        dateRangeET.setOnClickListener { v -> mPresenter!!.goToSelectDateRange(fromDate, toDate) }

        sortSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter!!.handleChangeSortOrder(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    private fun updateValueRangeOnView(from: Int, to: Int) {
        fromDate = from.toLong()
        toDate = to.toLong()
        val formatter = DecimalFormat("#,###")

        val toS = formatter.format(to.toLong())
        val fromS = formatter.format(from.toLong())
        val rangeText = getText(R.string.from).toString() + " " + fromS + " Afs - " + toS + " Afs"
        valueRangeTV.text = rangeText
    }

    override fun setListProvider(factory: DataSource.Factory<Int, SaleListDetail>) {

        val recyclerAdapter = SaleListRecyclerAdapter(DIFF_CALLBACK, mPresenter!!, false, false,
                this, applicationContext)
        val data = LivePagedListBuilder(factory, 20).build()
        data.observe(this, Observer<PagedList<SaleListDetail>> { recyclerAdapter.submitList(it) })

        mRecyclerView!!.adapter = recyclerAdapter
    }

    override fun updateLocationSpinner(locations: Array<String>) {
        val adapter = ArrayAdapter(applicationContext,
                R.layout.item_simple_spinner, locations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        locationSpinner!!.adapter = adapter
    }

    override fun updateDateRangeText(dateRangeText: String) {
        dateRangeET.setText(dateRangeText)
    }

    /**
     * Updates the sort spinner with string list given
     *
     * @param presets A String array String[] of the presets available.
     */
    override fun updateSortSpinner(presets: Array<String?>) {
        this.sortSpinnerPresets = presets
        val adapter = ArrayAdapter(Objects.requireNonNull(applicationContext),
                R.layout.spinner_item, sortSpinnerPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner!!.adapter = adapter
    }

    companion object {


        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SaleListDetail> = object : DiffUtil.ItemCallback<SaleListDetail>() {
            override fun areItemsTheSame(oldItem: SaleListDetail,
                                         newItem: SaleListDetail): Boolean {
                return oldItem.saleUid == newItem.saleUid
            }

            override fun areContentsTheSame(oldItem: SaleListDetail,
                                            newItem: SaleListDetail): Boolean {
                return oldItem == newItem
            }
        }
    }
}
