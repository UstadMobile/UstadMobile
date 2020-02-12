package com.ustadmobile.port.android.view

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleProductCategoryListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SaleProductCategoryListView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.SaleProduct
import java.security.AccessController.getContext

class SaleProductCategoryListActivity : UstadBaseActivity(), SaleProductCategoryListView {

    private lateinit var toolbar: Toolbar
    private lateinit var mPresenter: SaleProductCategoryListPresenter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var cRecyclerView: RecyclerView

    private lateinit var itemActionButton: FloatingActionButton
    private lateinit var subCategoryActionButton: FloatingActionButton
    private lateinit var floatingActionMenu: FloatingActionMenu

    private var menu: Menu? = null
    private var hideEdit = false

    private lateinit var sortSpinner: Spinner
    internal lateinit var sortSpinnerPresets: Array<String?>
    private lateinit var searchView:SearchView

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search_edit, menu)

        menu.findItem(R.id.action_search).isVisible = true

        //menu.findItem(R.id.action_edit).isVisible = !hideEdit

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

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_search -> //TODO: Handle search
                true
            R.id.action_edit -> {
                mPresenter.handleClickEditThisCategory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_sale_product_category_list)

        //Toolbar:
        toolbar = findViewById(R.id.activity_sale_product_category_list_toolbar)
        toolbar.title = getText(R.string.category)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        floatingActionMenu = findViewById(R.id.activity_sale_product_category_list_fab_menu)
        itemActionButton = findViewById(R.id.activity_sale_product_category_list_fab_item)
        subCategoryActionButton = findViewById(R.id.activity_sale_product_category_list_fab_subcategory)

        //RecyclerView
        mRecyclerView = findViewById(R.id.activity_sale_product_category_list_items_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView.layoutManager = mRecyclerLayoutManager

        //categories RecyclerView
        cRecyclerView = findViewById(R.id.activity_sale_product_category_list_categories_recyclerview)
        val cRecyclerLayoutManager = LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false)
        cRecyclerView.layoutManager = cRecyclerLayoutManager

        sortSpinner = findViewById(R.id.activity_sale_product_category_list_sort_by_spinner)

        //Call the Presenter
        mPresenter = SaleProductCategoryListPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Listeners
        itemActionButton.setOnClickListener {
            floatingActionMenu.close(true)
            mPresenter.handleClickAddItem()
        }

        subCategoryActionButton.setOnClickListener {
            floatingActionMenu.close(true)
            mPresenter.handleClickAddSubCategory()
        }

        subCategoryActionButton.isVisible = mPresenter.isLoggedInPersonAdmin()

        //Sort handler
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mPresenter.handleChangeSortOrder(id)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun setListProvider(listProvider: DataSource.Factory<Int, SaleProduct>,
                                 allMode: Boolean) {

        val recyclerAdapter = SelectSaleProductWithDescRecyclerAdapter(DIFF_CALLBACK, mPresenter,
                this, false, applicationContext)

        if(allMode){
            val data = listProvider.asRepositoryLiveData(
                    UmAccountManager.getRepositoryForActiveAccount(applicationContext!!).saleProductDao)
            //Observe the data:
            data.observe(this,
                    Observer<PagedList<SaleProduct>> { recyclerAdapter.submitList(it) })

        }else{
            val data = listProvider.asRepositoryLiveData(
                    UmAccountManager.getRepositoryForActiveAccount(applicationContext!!).saleProductParentJoinDao)
            //Observe the data:
            data.observe(this,
                    Observer<PagedList<SaleProduct>> { recyclerAdapter.submitList(it) })

        }

        //set the adapter
        mRecyclerView.adapter = recyclerAdapter
    }

    override fun setCategoriesListProvider(listProvider: DataSource.Factory<Int, SaleProduct>,
                                           allMode: Boolean) {

        val recyclerAdapter = SelectSaleCategoryRecyclerAdapter(DIFF_CALLBACK, mPresenter,
                this, showContextMenu = false,
                listCategory = true, theContext = applicationContext)

        if(allMode){
            val data = listProvider.asRepositoryLiveData(
                    UmAccountManager.getRepositoryForActiveAccount(applicationContext!!).saleProductDao)
            //Observe the data:
            data.observe(this,
                    Observer<PagedList<SaleProduct>> { recyclerAdapter.submitList(it) })
        }else{
            val data = listProvider.asRepositoryLiveData(
                    UmAccountManager.getRepositoryForActiveAccount(applicationContext!!).saleProductParentJoinDao)
            //Observe the data:
            data.observe(this,
                    Observer<PagedList<SaleProduct>> { recyclerAdapter.submitList(it) })
        }
        //set the adapter
        cRecyclerView.adapter = recyclerAdapter
    }

    override fun setMessageOnView(messageCode: Int) {
        val impl = UstadMobileSystemImpl.instance
        val message = impl.getString(messageCode, getContext())

        runOnUiThread {
            Toast.makeText(
                    applicationContext,
                    message,
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun updateToolbar(title: String?) {
        if(title != null) {
            toolbar.title = title
        }
    }

    override fun initFromSaleCategory(saleProductCategory: SaleProduct) {
        runOnUiThread { updateToolbar(saleProductCategory.saleProductName) }
    }

    override fun updateSortPresets(presets: Array<String?>) {
        this.sortSpinnerPresets = presets
        val adapter = ArrayAdapter(this,
                R.layout.item_simple_spinner_gray, sortSpinnerPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = adapter
    }

    override fun hideFAB(hide: Boolean) {
        runOnUiThread { floatingActionMenu.visibility = if (hide) View.GONE else View.VISIBLE }
    }

    override fun hideEditMenu(hide: Boolean) {
        hideEdit = hide
            if(menu != null) {
                menu!!.findItem(R.id.action_edit).isVisible = !hideEdit
            }
    }

    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SaleProduct> = object : DiffUtil.ItemCallback<SaleProduct>() {
            override fun areItemsTheSame(oldItem: SaleProduct,
                                         newItem: SaleProduct): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: SaleProduct,
                                            newItem: SaleProduct): Boolean {
                return oldItem.saleProductUid == newItem.saleProductUid
            }
        }
    }
}
