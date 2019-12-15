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
import androidx.paging.LivePagedListBuilder
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
import com.ustadmobile.lib.db.entities.SaleProduct


class CatalogListFragment : UstadBaseFragment, IOnBackPressed, SelectSaleProductView {

    override val viewContext: Any
        get() = context!!

    internal var rootContainer: View ?= null
    private var mPresenter: SelectSaleProductPresenter? = null
    private var recentRV: RecyclerView? = null
    private var categoryRV: RecyclerView? = null
    private var collectionRV: RecyclerView? = null
    private var floatingActionMenu: FloatingActionMenu? = null
    private var recentMore: TextView? = null
    private var categoryMore: TextView? = null
    private var collectionMore: TextView? = null

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun updateToolbar(title: String) {
        //TODO
    }

    override fun onBackPressed(): Boolean {
        if(floatingActionMenu!!.isOpened()){
            floatingActionMenu!!.close(true)
            return false
        }else{
            return true
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
        recentRV = rootContainer!!.findViewById(R.id.activity_select_sale_product_recent_rv)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recentRV!!.setLayoutManager(layoutManager)

        //RecyclerView - Category
        categoryRV = rootContainer!!.findViewById(R.id.activity_select_sale_product_category_rv)
        val layoutManager2 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        categoryRV!!.setLayoutManager(layoutManager2)

        //Recyclerview - Collection
        collectionRV = rootContainer!!.findViewById(R.id.activity_select_sale_product_collection_rv)
        val layoutManager3 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        collectionRV!!.setLayoutManager(layoutManager3)

        //Set other views
        recentMore = rootContainer!!.findViewById(R.id.activity_select_sale_product_recent_more)
        categoryMore = rootContainer!!.findViewById(R.id.activity_select_sale_product_category_more)
        collectionMore = rootContainer!!.findViewById(R.id.activity_select_sale_product_collections_more)

        //Call the Presenter
        mPresenter = SelectSaleProductPresenter(context!!,
                bundleToMap(arguments), this, true)
        mPresenter!!.onCreate(bundleToMap(savedInstanceState))

        //Set listeners
        floatingActionMenu = rootContainer!!.findViewById(R.id.activity_select_sale_product_fab_menu)
        rootContainer!!.findViewById<View>(R.id.activity_select_sale_product_fab_subcategory)
                .setOnClickListener { v ->
                    floatingActionMenu!!.close(true)
                    mPresenter!!.handleClickAddSubCategory()
                }

        rootContainer!!.findViewById<View>(R.id.activity_select_sale_product_fab_item)
                .setOnClickListener { v ->
                    floatingActionMenu!!.close(true)
                    mPresenter!!.handleClickAddItem()
                }

        recentMore!!.setOnClickListener { v -> mPresenter!!.handleClickRecentMore() }

        categoryMore!!.setOnClickListener { v -> mPresenter!!.handleClickCategoryMore() }

        collectionMore!!.setOnClickListener { v -> mPresenter!!.handleClickCollectionMore() }

        return rootContainer
    }



    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Hide the appbar
        rootContainer!!.findViewById<View>(
                R.id.activity_select_sale_product_appbar).visibility = View.GONE
    }

    override fun finish() { }

    override fun setRecentProvider(factory: DataSource.Factory<Int, SaleProduct>) {

        val boundaryCallback = UmAccountManager.getRepositoryForActiveAccount(context!!)
                .saleProductDaoBoundaryCallbacks.findActiveProductsProvider(factory)

        val recyclerAdapter = SelectSaleProductRecyclerAdapter(DIFF_CALLBACK, mPresenter!!,
                this,false, true, context!!)

        // get the provider, set , observe, etc.
        val data =
                LivePagedListBuilder(factory, 20)
                        .setBoundaryCallback(boundaryCallback)
                        .build()
        //Observe the data:
        data.observe(this,
                Observer<PagedList<SaleProduct>>
                { recyclerAdapter!!.submitList(it) }
                )

        //set the adapter
        recentRV!!.setAdapter(recyclerAdapter)
    }

    override fun setCategoryProvider(factory: DataSource.Factory<Int, SaleProduct>) {
        val boundaryCallback = UmAccountManager.getRepositoryForActiveAccount(context!!)
                .saleProductDaoBoundaryCallbacks.findActiveCategoriesProviderByNameAsc(factory)

        val recyclerAdapter = SelectSaleProductRecyclerAdapter(DIFF_CALLBACK, mPresenter!!,
                this, true, true, context!!)

        // get the provider, set , observe, etc.
        // A warning is expected
        val data = LivePagedListBuilder(factory, 20)
                .setBoundaryCallback(boundaryCallback)
                .build()
        //Observe the data:
        data.observe(this, Observer<PagedList<SaleProduct>>
        { recyclerAdapter!!.submitList(it) })

        //set the adapter
        categoryRV!!.setAdapter(recyclerAdapter)
    }

    fun searchCatalog(searchValue: String) {
        if(mPresenter != null) {
            mPresenter!!.setQuerySearch(searchValue)
            mPresenter!!.updateProviders()
        }
    }


    override fun setCollectionProvider(factory: DataSource.Factory<Int, SaleProduct>) {
        val recyclerAdapter = SelectSaleProductRecyclerAdapter(DIFF_CALLBACK, mPresenter!!,
                this,true, true, context!!)

        val boundaryCallback = UmAccountManager.getRepositoryForActiveAccount(context!!)
                .saleProductParentJoinDaoBoundaryCallbacks.findAllCategoriesInCollection(factory)

        // get the provider, set , observe, etc.
        // A warning is expected
        val data = LivePagedListBuilder(factory, 20)
                .setBoundaryCallback(boundaryCallback)
                .build()
        //Observe the data:
        data.observe(this, Observer<PagedList<SaleProduct>>
        { recyclerAdapter!!.submitList(it) })

        //set the adapter
        collectionRV!!.setAdapter(recyclerAdapter)
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

    constructor(args:Bundle) : this() {
        arguments = args
    }

    companion object {

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
                return oldItem.saleProductUid == newItem.saleProductUid
            }
        }
    }
}
