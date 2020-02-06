package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.clans.fab.FloatingActionMenu
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectSaleProductPresenter
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SelectSaleProductView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.SaleProduct
import com.ustadmobile.staging.core.view.SearchableListener

class CatalogListFragment : UstadBaseFragment, IOnBackPressed, SelectSaleProductView, SearchableListener {

    override val viewContext: Any
        get() = context!!

    internal lateinit var rootContainer: View
    private lateinit var mPresenter: SelectSaleProductPresenter
    private lateinit var recentRV: RecyclerView
    private lateinit var categoryRV: RecyclerView
    private lateinit var collectionRV: RecyclerView
    private lateinit var floatingActionMenu: FloatingActionMenu
    private lateinit var recentMore: TextView
    private lateinit var categoryMore: TextView
    private lateinit var collectionMore: TextView

    override fun updateToolbar(title: String) {
        //TODO
    }

    override fun onSearchButtonClick() {}

    override fun onSearchQueryUpdated(query: String) {
        searchCatalog(query)
    }

    override fun onBackPressed(): Boolean {
        return if(floatingActionMenu.isOpened){
            floatingActionMenu.close(true)
            false
        }else{
            true
        }
    }

    override fun showAddButton(show: Boolean) {
        if(show) {
            floatingActionMenu.visibility = View.VISIBLE
        }else{
            floatingActionMenu.visibility = View.GONE
        }
    }

    @Nullable
    override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?,
                              @Nullable savedInstanceState: Bundle?): View? {

        //Inflate view
        rootContainer = inflater.inflate(R.layout.activity_select_sale_product, container, false)
        setHasOptionsMenu(true)

        //Set recycler views
        //RecyclerView - Recent
        recentRV = rootContainer.findViewById(R.id.activity_select_sale_product_recent_rv)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recentRV.layoutManager = layoutManager

        //RecyclerView - Category
        categoryRV = rootContainer.findViewById(R.id.activity_select_sale_product_category_rv)
        val layoutManager2 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        categoryRV.layoutManager = layoutManager2

        //Recyclerview - Collection
        collectionRV = rootContainer.findViewById(R.id.activity_select_sale_product_collection_rv)
        val layoutManager3 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        collectionRV.layoutManager = layoutManager3

        //Set other views
        recentMore = rootContainer.findViewById(R.id.activity_select_sale_product_recent_more)
        categoryMore = rootContainer.findViewById(R.id.activity_select_sale_product_category_more)
        collectionMore = rootContainer.findViewById(R.id.activity_select_sale_product_collections_more)

        floatingActionMenu = rootContainer.findViewById(R.id.activity_select_sale_product_fab_menu)

        //Call the Presenter
        mPresenter = SelectSaleProductPresenter(context!!,
                bundleToMap(arguments), this, true)
        mPresenter.onCreate(bundleToMap(savedInstanceState))

        //Set listeners
        rootContainer.findViewById<View>(R.id.activity_select_sale_product_fab_subcategory)
                .setOnClickListener {
                    floatingActionMenu.close(true)
                    mPresenter.handleClickAddSubCategory()
                }

        rootContainer.findViewById<View>(R.id.activity_select_sale_product_fab_item)
                .setOnClickListener {
                    floatingActionMenu.close(true)
                    mPresenter.handleClickAddItem()
                }

        recentMore.setOnClickListener { mPresenter.handleClickRecentMore() }

        categoryMore.setOnClickListener { mPresenter.handleClickCategoryMore() }

        collectionMore.setOnClickListener { mPresenter.handleClickCollectionMore() }

        return rootContainer
    }



    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Hide the appbar
        rootContainer.findViewById<View>(
                R.id.activity_select_sale_product_appbar).visibility = View.GONE
    }

    override fun finish() { }

    override fun setRecentProvider(recentProvider: DataSource.Factory<Int, SaleProduct>) {

        if(context == null){
            return
        }

        val recyclerAdapter = SelectSaleProductRecyclerAdapter(DIFF_CALLBACK,
                mPresenter,
                this, isCategory = false, catalog = true,
                context = context!!)

        val data = recentProvider.asRepositoryLiveData(
                UmAccountManager.getRepositoryForActiveAccount(context!!).saleProductDao)

        //Observe the data:
        data.observe(this,
                Observer<PagedList<SaleProduct>>
                { recyclerAdapter.submitList(it) }
                )

        //set the adapter
        recentRV.setAdapter(recyclerAdapter)
    }

    override fun setCategoryProvider(categoryProvider: DataSource.Factory<Int, SaleProduct>) {

        if(context == null){
            return
        }
        
        val recyclerAdapter = SelectSaleProductRecyclerAdapter(DIFF_CALLBACK,
                mPresenter,
                this, isCategory = true, catalog = true,
                context = context!!)

        // get the provider, set , observe, etc.
        val data = categoryProvider.asRepositoryLiveData(
                UmAccountManager.getRepositoryForActiveAccount(context!!).saleProductDao)

        //Observe the data:
        data.observe(this, Observer<PagedList<SaleProduct>>
        { recyclerAdapter.submitList(it) })

        //set the adapter
        categoryRV.setAdapter(recyclerAdapter)
    }

    fun searchCatalog(searchValue: String) {
        mPresenter.setQuerySearch(searchValue)
        mPresenter.updateProviders()
    }


    override fun setCollectionProvider(collectionProvider: DataSource.Factory<Int, SaleProduct>) {

        if(context == null){
            return
        }
        
        val recyclerAdapter = SelectSaleProductRecyclerAdapter(DIFF_CALLBACK,
                mPresenter,
                this, isCategory = true, catalog = true,
                context = context!!)

        // get the provider, set , observe, etc.
        // A warning is expected
        val data = collectionProvider.asRepositoryLiveData(
                UmAccountManager.getRepositoryForActiveAccount(context!!).saleProductParentJoinDao)

        //Observe the data:
        data.observe(this, Observer<PagedList<SaleProduct>>
        { recyclerAdapter.submitList(it) })

        //set the adapter
        collectionRV.adapter = recyclerAdapter
    }

    override fun showMessage(messageId: Int) {
        val impl = UstadMobileSystemImpl.instance
        val toast = impl.getString(messageId, context!!)
        runOnUiThread(Runnable {
            Toast.makeText(
                    context,
                    toast,
                    Toast.LENGTH_SHORT
            ).show()
        })
    }

    constructor()  {
        val args = Bundle()
        arguments = args
        icon = R.drawable.ic_list_black_24dp
        title = R.string.catalog
    }

    companion object {

        val icon = R.drawable.ic_list_black_24dp

        fun newInstance(): CatalogListFragment {
            val fragment = CatalogListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SaleProduct> = object :
                DiffUtil.ItemCallback<SaleProduct>() {
            override fun areItemsTheSame(oldItem: SaleProduct,
                                         newItem: SaleProduct): Boolean {
                return oldItem == newItem
            }
            override fun areContentsTheSame(oldItem: SaleProduct,
                                            newItem: SaleProduct): Boolean {
                return oldItem == newItem
            }
        }
    }
}
