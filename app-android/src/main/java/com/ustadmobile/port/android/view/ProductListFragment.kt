package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ProductListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ProductListView
import com.ustadmobile.core.view.SaleItemEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount
import com.ustadmobile.lib.db.entities.SaleItemWithProduct
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter

class ProductListFragment(): UstadListViewFragment<Product, ProductWithInventoryCount>(),
        ProductListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ProductListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in ProductWithInventoryCount>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = ProductListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = ProductListRecyclerAdapter(mPresenter, requireContext())
        val createNewText = requireContext().getString(R.string.create_product)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this, createNewText)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = true
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.product)
    }

    override fun goToSaleItem(product: Product) {
        navigateToEditEntity(null, R.id.saleitem_edit_dest,
                SaleItemWithProduct::class.java, argBundle = bundleOf(
                        UstadView.ARG_PRODUCT_UID to product.productUid.toString()))
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout) {
            navigateToEditEntity(null, R.id.product_edit_dest, Product::class.java)
        }else{
            super.onClick(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.productDao


}