package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.db.dao.SaleProductDao
import com.ustadmobile.core.db.dao.SaleProductGroupDao
import com.ustadmobile.core.db.dao.SaleProductParentJoinDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
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
import com.ustadmobile.core.view.SaleProductDetailView.Companion.ARG_NEW_CATEGORY
import com.ustadmobile.core.view.SaleProductDetailView.Companion.ARG_NEW_TITLE
import com.ustadmobile.core.view.SaleProductDetailView.Companion.ARG_SALE_PRODUCT_UID
import com.ustadmobile.core.view.SelectProducerView.Companion.ARG_PRODUCER_UID
import com.ustadmobile.core.view.SelectSaleProductView
import com.ustadmobile.lib.db.entities.SaleDescWithSaleProductPicture
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


/**
 * Presenter for SelectSaleProduct view
 */
class SelectSaleProductPresenter(context: Any,
                                 arguments: Map<String, String>?,
                                 view: SelectSaleProductView,
                                 private val catalogMode: Boolean)
    : UstadBaseController<SelectSaleProductView>(context, arguments!!, view) {

    private var recentProvider: DataSource.Factory<Int, SaleDescWithSaleProductPicture>? = null
    private var categoryProvider: DataSource.Factory<Int, SaleDescWithSaleProductPicture>? = null
    private var collectionProvider: DataSource.Factory<Int, SaleDescWithSaleProductPicture>? = null

    internal var repository: UmAppDatabase

    internal var saleProductDao: SaleProductDao
    internal var saleProductGroupDao: SaleProductGroupDao
    internal var productParentJoinDao: SaleProductParentJoinDao
    internal var impl: UstadMobileSystemImpl

    private var producerUid: Long = 0
    private var saleItemUid: Long = 0


    init {

        impl = UstadMobileSystemImpl.instance

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        saleProductDao = repository.saleProductDao
        saleProductGroupDao = repository.saleProductGroupDao
        productParentJoinDao = repository.saleProductParentJoinDao

        if (arguments!!.containsKey(ARG_PRODUCER_UID)) {
            producerUid = (arguments.get(ARG_PRODUCER_UID)!!.toLong())
        }
        if (arguments.containsKey(ARG_SALE_ITEM_UID)) {
            saleItemUid = (arguments.get(ARG_SALE_ITEM_UID)!!.toLong())
        }

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        updateRecentProvider()
        updateCategoryProvider()
        updateCollectionProvider()
    }

    private fun updateRecentProvider() {

        recentProvider = saleProductDao.findAllActiveProductsSNWIProviderWithPP()
        view.setRecentProvider(recentProvider!!)

    }

    private fun updateCategoryProvider() {

        categoryProvider = saleProductDao.findAllActiveCategoriesSNWIProviderWithPP()
        view.setCategoryProvider(categoryProvider!!)

    }

    private fun updateCollectionProvider() {
        collectionProvider = productParentJoinDao.findAllCategoriesInCollectionWithPP()
        view.setCollectionProvider(collectionProvider!!)
    }

    fun handleClickProduct(productUid: Long, isCategory: Boolean){
        handleClickProductMulti(productUid, isCategory, false)
    }

    fun handleClickProductMulti(productUid: Long, isCategory: Boolean, editMode: Boolean) {

        val args = HashMap<String, String>()
        if (catalogMode) {

            if (isCategory) {
                if(editMode){
                    args.put(ARG_SALE_PRODUCT_UID, productUid.toString())
                    impl.go(SaleProductDetailView.VIEW_NAME, args, context)
                }else {
                    args.put(ARG_SALEPRODUCT_UID, productUid.toString())
                    impl.go(SaleProductCategoryListView.VIEW_NAME, args, context)
                }
            } else {
                args.put(ARG_SALE_PRODUCT_UID, productUid.toString())
                impl.go(SaleProductDetailView.VIEW_NAME, args, context)
            }
        } else {
            //Need to select the product.
            if (isCategory) {
                args.put(ARG_SALEPRODUCT_UID, productUid.toString())
                args.put(ARG_PASS_PRODUCER_UID, producerUid.toString())
                args.put(ARG_PASS_SALE_ITEM_UID, saleItemUid.toString())
                args.put(ARG_SELECT_PRODUCT, "true")
                impl.go(SaleProductCategoryListView.VIEW_NAME, args, context)
            } else {
                args.put(ARG_SALE_ITEM_PRODUCT_UID, productUid.toString())
                args.put(ARG_PRODUCER_UID, producerUid.toString())
                args.put(ARG_SALE_ITEM_UID, saleItemUid.toString())
                args.put(ARG_SELECT_PRODUCT, "true")
                impl.go(SaleItemDetailView.VIEW_NAME, args, context)
            }
            view.finish()
        }
    }

    fun handleClickAddItem() {
        val args = HashMap<String, String>()
        args.put(ARG_NEW_TITLE, "true")
        impl.go(SaleProductDetailView.VIEW_NAME, args, context)

    }

    fun handleClickAddSubCategory() {
        val args = HashMap<String, String>()
        args.put(ARG_NEW_CATEGORY, "true")
        impl.go(SaleProductDetailView.VIEW_NAME, args, context)
    }

    fun handleDelteSaleProduct(productUid: Long, isCategory: Boolean) {
        GlobalScope.launch {
            try{
                val result = saleProductDao.inactivateEntityAsync(productUid)
                //Send message to view
                if (isCategory) {
                    view.runOnUiThread(Runnable { view.showMessage(MessageID.category_deleted) })
                } else {
                    view.runOnUiThread(Runnable { view.showMessage(MessageID.item_deleted) })
                }
            }catch(e:Exception){
                println(e.message)
            }
        }
    }

    fun handleClickRecentMore() {
        val args = HashMap<String, String>()
        args.put(ARG_MORE_RECENT, "true")
        if (catalogMode) {
            impl.go(SaleProductCategoryListView.VIEW_NAME, args, context)

        } else {
            //Need to select the product.

            //Pass it on
            args.put(ARG_PASS_PRODUCER_UID, producerUid.toString())
            args.put(ARG_PASS_SALE_ITEM_UID, saleItemUid.toString())
            args.put(ARG_SELECT_PRODUCT, "true")

            impl.go(SaleProductCategoryListView.VIEW_NAME, args, context)
        }
    }

    fun handleClickCategoryMore() {
        val args = HashMap<String, String>()
        args.put(ARG_MORE_CATEGORY, "true")
        if (catalogMode) {
            impl.go(SaleProductCategoryListView.VIEW_NAME, args, context)

        } else {
            //Need to select the product.

            //Pass it on
            args.put(ARG_PASS_PRODUCER_UID, producerUid.toString())
            args.put(ARG_PASS_SALE_ITEM_UID, saleItemUid.toString())
            args.put(ARG_SELECT_PRODUCT, "true")

            impl.go(SaleProductCategoryListView.VIEW_NAME, args, context)
        }
    }

    fun handleClickCollectionMore() {
        GlobalScope.launch {
            try{
                val result = saleProductDao.findByNameAsync("Collection")
                handleClickProduct(result!!.saleProductUid, true)

            }catch(e:Exception){
                println(e.message)
            }
        }
    }
}
