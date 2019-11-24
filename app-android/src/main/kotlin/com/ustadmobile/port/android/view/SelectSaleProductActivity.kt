package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.clans.fab.FloatingActionMenu
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectSaleProductPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SelectSaleProductView
import com.ustadmobile.lib.db.entities.SaleProduct

class SelectSaleProductActivity : UstadBaseActivity(), SelectSaleProductView {

    private var toolbar: Toolbar? = null
    private var mPresenter: SelectSaleProductPresenter? = null
    private var recentRV: RecyclerView? = null
    private var categoryRV: RecyclerView? = null
    private var collectionRV: RecyclerView? = null

    private var fam: FloatingActionMenu? = null

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
        setContentView(R.layout.activity_select_sale_product)

        //Toolbar:
        toolbar = findViewById(R.id.activity_select_sale_product_toolbar)
        toolbar!!.title = getText(R.string.add_item)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //RecyclerView - Recent
        recentRV = findViewById(
                R.id.activity_select_sale_product_recent_rv)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recentRV!!.layoutManager = layoutManager

        //RecyclerView - Category
        categoryRV = findViewById(
                R.id.activity_select_sale_product_category_rv)
        val layoutManager2 = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoryRV!!.layoutManager = layoutManager2

        //Recyclerview - Collection
        collectionRV = findViewById(
                R.id.activity_select_sale_product_collection_rv)
        val layoutManager3 = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        collectionRV!!.layoutManager = layoutManager3

        //Call the Presenter
        mPresenter = SelectSaleProductPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this, false)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        fam = findViewById(R.id.activity_select_sale_product_fab_menu)
        fam!!.visibility = View.GONE
    }

    override fun setRecentProvider(factory: DataSource.Factory<Int, SaleProduct>) {
        val recyclerAdapter = SelectSaleProductRecyclerAdapter(DIFF_CALLBACK, mPresenter!!, this,
                false, false,
                applicationContext)

        val boundaryCallback = UmAccountManager.getRepositoryForActiveAccount(applicationContext)
                .saleProductDaoBoundaryCallbacks.findActiveProductsProvider(factory)

        // get the provider, set , observe, etc.
        val data = LivePagedListBuilder(factory, 20)
                .setBoundaryCallback(boundaryCallback)
                .build()
        //Observe the data:
        data.observe(this, Observer<PagedList<SaleProduct>> { recyclerAdapter.submitList(it) })

        //set the adapter
        recentRV!!.adapter = recyclerAdapter
    }

    override fun setCategoryProvider(factory: DataSource.Factory<Int, SaleProduct>) {
        val boundaryCallback = UmAccountManager.getRepositoryForActiveAccount(applicationContext)
                .saleProductDaoBoundaryCallbacks.findActiveCategoriesProviderByNameAsc(factory)

        val recyclerAdapter = SelectSaleProductRecyclerAdapter(DIFF_CALLBACK, mPresenter!!, this,
                true, false,
                applicationContext)

        // get the provider, set , observe, etc.

        val data = LivePagedListBuilder(factory, 20)
                .setBoundaryCallback(boundaryCallback)
                .build()
        //Observe the data:
        data.observe(this, Observer<PagedList<SaleProduct>> { recyclerAdapter.submitList(it) })

        //set the adapter
        categoryRV!!.adapter = recyclerAdapter
    }

    override fun setCollectionProvider(factory: DataSource.Factory<Int, SaleProduct>) {
        val recyclerAdapter = SelectSaleProductRecyclerAdapter(DIFF_CALLBACK, mPresenter!!, this,
                true, false,
                applicationContext)

        val boundaryCallback = UmAccountManager.getRepositoryForActiveAccount(applicationContext)
                .saleProductParentJoinDaoBoundaryCallbacks.findAllCategoriesInCollection(factory)

        // get the provider, set , observe, etc.
        val data = LivePagedListBuilder(factory, 20)
                .setBoundaryCallback(boundaryCallback)
                .build()
        //Observe the data:
        data.observe(this, Observer<PagedList<SaleProduct>> { recyclerAdapter.submitList(it) })

        //set the adapter
        collectionRV!!.adapter = recyclerAdapter
    }

    override fun showMessage(messageId: Int) {
        val impl = UstadMobileSystemImpl.instance
        val toast = impl.getString(messageId, this)
        runOnUiThread {
            Toast.makeText(
                    this,
                    toast,
                    Toast.LENGTH_SHORT
            ).show()
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
