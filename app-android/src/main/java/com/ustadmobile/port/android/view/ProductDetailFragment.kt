package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.FragmentProductDetailBinding
import com.ustadmobile.core.controller.ProductDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.dao.ClazzWorkDao
import com.ustadmobile.core.db.dao.InventoryTransactionDao
import com.ustadmobile.core.db.dao.ProductDao
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.core.view.ProductDetailView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.ext.observeIfFragmentViewIsReady
import kotlinx.android.synthetic.main.fragment_person_detail.*

interface ProductDetailFragmentEventHandler {

}

class ProductDetailFragment: UstadDetailFragment<ProductWithInventoryCount>(), ProductDetailView,
        ProductDetailFragmentEventHandler {

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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentProductDetailBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
        }

        mPresenter = ProductDetailPresenter(requireContext(), arguments.toStringMap(), this,
                di, this)

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


    override var editButtonMode: EditButtonMode = EditButtonMode.GONE
        get() = field
        set(value) {
            mBinding?.editButtonMode = value
            field = value
        }

}