package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentProductDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.InventoryItemDao
import com.ustadmobile.core.db.dao.ProductDao
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ProductDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.observeIfFragmentViewIsReady
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

interface ProductDetailFragmentEventHandler {

    fun handleClickRecordDelivery(product: ProductWithInventoryCount)
    fun handleClickRecordSale(product: ProductWithInventoryCount)
}

class ProductDetailFragment: UstadDetailFragment<ProductWithInventoryCount>(), ProductDetailView,
        ProductDetailFragmentEventHandler, CategoryChipListener, InventoryTransactionDetailListener,
        PersonWithInventoryCountListener, ProductImageListener{

    private var mBinding: FragmentProductDetailBinding? = null

    private var mPresenter: ProductDetailPresenter? = null

    private var categoriesRecyclerView : RecyclerView? = null
    private var stockRecyclerView : RecyclerView? = null
    private var historyRecyclerView : RecyclerView? = null

    //Pictures RV
    private var picturesRecyclerAdapter: ProductImageRecyclerAdapter? = null
    private var picturesLiveData: LiveData<PagedList<Product>>? = null
    private var picturesObserver = Observer<PagedList<Product>?>{
        t ->
        run {
            picturesRecyclerAdapter?.submitList(t)
        }
    }

    //Category RV
    private var categoriesRecyclerAdapter: CategoryChipRecyclerAdapter? = null
    private var categoryLiveData: LiveData<PagedList<Category>>? = null
    private val categoryObserver = Observer<PagedList<Category>?> {
        t ->
        run {
            categoriesRecyclerAdapter?.submitList(t)
        }
    }

    //Stock RV
    private var stockRecyclerAdapter: PersonWithInventoryCountRecyclerAdapter? = null
    private var stockLiveData: LiveData<PagedList<PersonWithInventoryCount>>? = null
    private val stockObserver = Observer<PagedList<PersonWithInventoryCount>?> {
        t ->
        run {
            stockRecyclerAdapter?.submitList(t)
        }
    }

    //History RV
    private var historyRecyclerAdapter: InventoryTransactionDetailRecyclerAdapter? = null
    private var historyLiveData: LiveData<PagedList<InventoryTransactionDetail>>? = null
    private val historyObserver = Observer<PagedList<InventoryTransactionDetail>?> {
        t ->
        run {
            historyRecyclerAdapter?.submitList(t)
        }
    }

    override var productCategories: DataSource.Factory<Int, Category>? = null
        set(value) {
            categoryLiveData?.removeObserver(categoryObserver)
            categoryLiveData = value?.asRepositoryLiveData(ProductDao)
            field = value
            categoryLiveData?.observeIfFragmentViewIsReady(this, categoryObserver)
        }


    override var stockList: DataSource.Factory<Int, PersonWithInventoryCount>? = null
        set(value) {
            stockLiveData?.removeObserver(stockObserver)
            stockLiveData = value?.asRepositoryLiveData(InventoryItemDao)
            field = value
            stockLiveData?.observeIfFragmentViewIsReady(this, stockObserver)
        }

    override var transactionList: DataSource.Factory<Int, InventoryTransactionDetail>? = null
        set(value) {
            historyLiveData?.removeObserver(historyObserver)
            historyLiveData = value?.asRepositoryLiveData(InventoryItemDao)
            field = value
            historyLiveData?.observeIfFragmentViewIsReady(this, historyObserver)
        }
    override var pictureList: DataSource.Factory<Int, Product>? = null
        set(value) {
            picturesLiveData?.removeObserver(picturesObserver)
            picturesLiveData = value?.asRepositoryLiveData(ProductDao)
            field = value
            picturesLiveData?.observeIfFragmentViewIsReady(this, picturesObserver)
        }

    private var repo: UmAppDatabase? = null

    override fun onClickEntry(entry: Category) {
        //TODO
    }

    override fun onClickEntry(entry: InventoryTransactionDetail) {
        //TODO
    }

    override fun onClickPerson(person: PersonWithInventoryCount) {
        //TODO
    }
    override fun handleClickRecordDelivery(product: ProductWithInventoryCount) {

        navigateToEditEntity(null, R.id.inventoryitem_edit_dest, InventoryItem::class.java,
        argBundle = bundleOf(
                UstadView.ARG_PRODUCT_UID to arguments?.get(UstadView.ARG_ENTITY_UID).toString()))

    }

    override fun handleClickRecordSale(product: ProductWithInventoryCount) {



        navigateToEditEntity(null, R.id.sale_edit_dest,
                Sale::class.java,
                argBundle = bundleOf(
                        UstadView.ARG_PRODUCT_UID to product.productUid.toString(),
                        UstadView.ARG_CREATE_SALE to "true")
        )

    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View

        categoriesRecyclerAdapter = CategoryChipRecyclerAdapter(this)
        stockRecyclerAdapter = PersonWithInventoryCountRecyclerAdapter(this)
        historyRecyclerAdapter = InventoryTransactionDetailRecyclerAdapter(this)
        picturesRecyclerAdapter = ProductImageRecyclerAdapter(this)


        mBinding = FragmentProductDetailBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
            it.fragmentProductDetailCategoryRv.apply{
                adapter = categoriesRecyclerAdapter
                val lm = LinearLayoutManager(requireContext())
                lm.orientation = LinearLayoutManager.HORIZONTAL
                layoutManager = lm
            }
            it.fragmentProductDetailStockRv.apply{
                adapter = stockRecyclerAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            it.fragmentProductDetailHistoryRv.apply{
                adapter = historyRecyclerAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            it.fragmentProductShowcasePicturesRv.apply{
                adapter = picturesRecyclerAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

        }
        mBinding?.fragmentEventHandler = this
        val accountManager: UstadAccountManager by instance()
        repo = di.direct.on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_REPO)

        mPresenter = ProductDetailPresenter(requireContext(), arguments.toStringMap(), this,
                di, this)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }




    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null

        categoriesRecyclerView?.adapter = null
        stockRecyclerView?.adapter = null
        historyRecyclerView?.adapter = null

        categoryLiveData = null
        stockLiveData = null
        historyLiveData = null
        categoriesRecyclerAdapter = null
        stockRecyclerAdapter = null
        historyRecyclerAdapter = null

    }

    override fun onResume() {
        super.onResume()


    }

    override var entity: ProductWithInventoryCount? = null
        get() = field
        set(value) {
            field = value
            mBinding?.product = value
            if(viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
                (activity as? AppCompatActivity)?.supportActionBar?.title =
                        value?.productName

        }

    override fun onClickProductPicture(product: Product) {
        //TODO this
    }


}