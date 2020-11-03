package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.FragmentProductDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.InventoryTransactionDao
import com.ustadmobile.core.db.dao.ProductDao
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ProductDetailView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.lib.db.entities.InventoryTransactionDetail
import com.ustadmobile.lib.db.entities.PersonWithInventoryCount
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount
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
        PersonWithInventoryCountListener{

    private var mBinding: FragmentProductDetailBinding? = null

    private var mPresenter: ProductDetailPresenter? = null

    private var categoriesRecyclerView : RecyclerView? = null
    private var stockRecyclerView : RecyclerView? = null
    private var historyRecyclerView : RecyclerView? = null

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
            stockLiveData = value?.asRepositoryLiveData(InventoryTransactionDao)
            field = value
            stockLiveData?.observeIfFragmentViewIsReady(this, stockObserver)
        }

    override var transactionList: DataSource.Factory<Int, InventoryTransactionDetail>? = null
        set(value) {
            historyLiveData?.removeObserver(historyObserver)
            historyLiveData = value?.asRepositoryLiveData(InventoryTransactionDao)
            field = value
            historyLiveData?.observeIfFragmentViewIsReady(this, historyObserver)
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
        //TOOD
    }

    override fun handleClickRecordSale(product: ProductWithInventoryCount) {
        //TODO
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View

        categoriesRecyclerAdapter = CategoryChipRecyclerAdapter(this)
        stockRecyclerAdapter = PersonWithInventoryCountRecyclerAdapter(this)
        historyRecyclerAdapter = InventoryTransactionDetailRecyclerAdapter(this)



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

    override var entity: ProductWithInventoryCount? = null
        get() = field
        set(value) {
            field = value
            mBinding?.product = value
        }



}