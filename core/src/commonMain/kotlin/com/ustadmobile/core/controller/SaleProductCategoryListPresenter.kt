package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SaleProductDao
import com.ustadmobile.core.db.dao.SaleProductParentJoinDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView
import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView.Companion.ARG_ADD_TO_CATEGORY_TYPE_CATEGORY
import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView.Companion.ARG_ADD_TO_CATEGORY_TYPE_ITEM
import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView.Companion.ARG_SALE_PRODUCT_CATEGORY_TO_ASSIGN_TO_UID
import com.ustadmobile.core.view.SaleItemDetailView
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_PRODUCT_UID
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_UID
import com.ustadmobile.core.view.SaleProductCategoryListView
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_MORE_CATEGORY
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_MORE_RECENT
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_PASS_PRODUCER_UID
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_PASS_SALE_ITEM_UID
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_SALEPRODUCT_UID
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_SELECT_PRODUCT
import com.ustadmobile.core.view.SaleProductDetailView
import com.ustadmobile.core.view.SaleProductDetailView.Companion.ARG_SALE_PRODUCT_UID
import com.ustadmobile.core.view.SelectProducerView.Companion.ARG_PRODUCER_UID
import com.ustadmobile.lib.db.entities.SaleProduct
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * Presenter for SaleProductCategoryList view
 */
class SaleProductCategoryListPresenter(context: Any,
                                       arguments: Map<String, String>?,
                                       view: SaleProductCategoryListView)
    : UstadBaseController<SaleProductCategoryListView>(context, arguments!!, view) {

    private lateinit var itemProvider: DataSource.Factory<Int, SaleProduct>
    private lateinit var categoryProvider: DataSource.Factory<Int, SaleProduct>
    internal var repository: UmAppDatabase
    private val productDao: SaleProductDao
    private var currentSaleProductCategory: SaleProduct? = null
    private val productParentJoinDao: SaleProductParentJoinDao
    private val impl: UstadMobileSystemImpl

    private var selectProductMode = false
    private var producerUid: String? = null
    private var saleItemUid: String? = null

    private var moreRecent: Boolean = false
    private var moreCategory: Boolean = false


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        productDao = repository.saleProductDao
        productParentJoinDao = repository.saleProductParentJoinDao

        impl = UstadMobileSystemImpl.instance
        //Populate itemProvider and categoryProvider

        if (arguments!!.containsKey(ARG_SELECT_PRODUCT)) {
            if (arguments!!.get(ARG_SELECT_PRODUCT) == "true") {
                selectProductMode = true
            }
        }
        if (arguments.containsKey(ARG_PASS_PRODUCER_UID)) {
            producerUid = arguments.get(ARG_PASS_PRODUCER_UID).toString()
        }
        if (arguments.containsKey(ARG_PASS_SALE_ITEM_UID)) {
            saleItemUid = arguments.get(ARG_PASS_SALE_ITEM_UID).toString()
        }
        if (arguments.containsKey(ARG_MORE_RECENT)) {
            moreRecent = true
        }
        if (arguments.containsKey(ARG_MORE_CATEGORY)) {
            moreCategory = true
        }

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        val thisP = this
        if (arguments.containsKey(ARG_SALEPRODUCT_UID)) {
            GlobalScope.launch {
                try{
                    val categoryLive = productDao.findByUidLive(arguments[ARG_SALEPRODUCT_UID]!!.toLong())
                    view.runOnUiThread(Runnable {
                        categoryLive.observe(thisP, thisP::setCategoryLiveOnView)
                    })

                }catch(e:Exception){
                    println(e.message)
                }
            }

        } else {
            currentSaleProductCategory = SaleProduct("", "",
                    true, false)

            setCategoryOnView(moreRecent, moreCategory)
        }

        if (selectProductMode) {
            view.hideFAB(true)
        }
    }

    private fun setCategoryLiveOnView(saleProductCategory:SaleProduct?){

        if (saleProductCategory != null) {
            currentSaleProductCategory = saleProductCategory
        } else {
            currentSaleProductCategory = SaleProduct("", "",
                    true, false)
        }
        setCategoryOnView(true, true)
    }

    private fun setCategoryOnView(recent: Boolean, category: Boolean) {
        //Update on view
        view.initFromSaleCategory(currentSaleProductCategory!!)

        //Get category and item providers

        //Get SaleNameWithImage for all items
        var allMode : Boolean = false
        if (currentSaleProductCategory!!.saleProductUid != 0L) {
            view.hideEditMenu(false)
            itemProvider = productParentJoinDao.findAllItemsInACategory(
                    currentSaleProductCategory!!.saleProductUid)
            categoryProvider = productParentJoinDao.findAllCategoriesInACategory(
                    currentSaleProductCategory!!.saleProductUid)
            allMode = false
        } else {
            allMode = true
            view.hideEditMenu(true)
            itemProvider = productDao.findAllActiveSNWIProvider()
            categoryProvider = productDao.findActiveCategoriesProvider()
        }

        if (recent)
            view.runOnUiThread(Runnable{ view.setListProvider(itemProvider, allMode) })
        if (category)
            view.runOnUiThread(Runnable{ view.setCategoriesListProvider(categoryProvider, allMode) })

    }

    //If you want to edit the category itself.
    fun handleClickEditThisCategory() {
        val categoryUid = currentSaleProductCategory!!.saleProductUid
        val args = HashMap<String, String>()
        args.put(ARG_SALE_PRODUCT_UID, categoryUid.toString())
        impl.go(SaleProductDetailView.VIEW_NAME, args, context)
    }

    fun handleClickEditCategory(saleProductUid: Long) {
        val args = HashMap<String, String>()
        args.put(ARG_SALE_PRODUCT_UID, saleProductUid.toString())
        impl.go(SaleProductDetailView.VIEW_NAME, args, context)
    }

    fun handleDeleteCategory(saleProductUid: Long) {
        //TODO: this
    }

    /**
     * Go to item/category detail page.
     * @param productUid
     */
    fun handleClickProduct(productUid: Long, isCategory: Boolean) {
        val args = HashMap<String, String>()
        if (!isCategory) {
            if (selectProductMode) {
                //Go to the sale item
                args.put(ARG_SALE_ITEM_PRODUCT_UID, productUid.toString())
                args.put(ARG_PRODUCER_UID, producerUid.toString())
                args.put(ARG_SALE_ITEM_UID, saleItemUid.toString())
                impl.go(SaleItemDetailView.VIEW_NAME, args, context)
                view.finish()
            } else {
                //Go to product detail.
                args.put(ARG_SALE_PRODUCT_UID, productUid.toString())
                impl.go(SaleProductDetailView.VIEW_NAME, args, context)
            }

        } else {
            //Go to category detail
            if (selectProductMode) {
                //pass it on bro
                args.put(ARG_SELECT_PRODUCT, "true")
                args.put(ARG_PASS_PRODUCER_UID, producerUid!!)
                args.put(ARG_PASS_SALE_ITEM_UID, saleItemUid!!)
            }

            args.put(ARG_SALEPRODUCT_UID, productUid.toString())
            impl.go(SaleProductCategoryListView.VIEW_NAME, args, context)
        }
    }

    fun handleClickAddItem() {
        val args = HashMap<String, String>()
        args.put(ARG_SALE_PRODUCT_CATEGORY_TO_ASSIGN_TO_UID,
                currentSaleProductCategory!!.saleProductUid.toString())
        args.put(ARG_ADD_TO_CATEGORY_TYPE_ITEM, "true")
        impl.go(AddSaleProductToSaleCategoryView.VIEW_NAME, args, context)
    }

    fun handleClickAddSubCategory() {

        val args = HashMap<String, String>()
        args.put(ARG_SALE_PRODUCT_CATEGORY_TO_ASSIGN_TO_UID,
                currentSaleProductCategory!!.saleProductUid.toString())
        args.put(ARG_ADD_TO_CATEGORY_TYPE_CATEGORY, "true")
        impl.go(AddSaleProductToSaleCategoryView.VIEW_NAME, args, context)
    }
}
