package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.SaleProductDao
import com.ustadmobile.core.db.dao.SaleProductParentJoinDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_DETAIL_PREORDER
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_PRODUCT_UID
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_UID
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_MORE_CATEGORY
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_MORE_RECENT
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_PASS_PRODUCER_UID
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_PASS_SALE_ITEM_UID
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_SALEPRODUCT_CATEGORY_INVENTORY_MODE
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_SALEPRODUCT_UID
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_SELECT_PRODUCT
import com.ustadmobile.core.view.SaleProductDetailView.Companion.ARG_NEW_CATEGORY
import com.ustadmobile.core.view.SaleProductDetailView.Companion.ARG_NEW_TITLE
import com.ustadmobile.core.view.SaleProductDetailView.Companion.ARG_SALE_PRODUCT_UID
import com.ustadmobile.core.view.SelectProducerView.Companion.ARG_PRODUCER_UID
import com.ustadmobile.core.view.SelectProducersView.Companion.ARG_SELECT_PRODUCERS_INVENTORY_ADDITION
import com.ustadmobile.core.view.SelectProducersView.Companion.ARG_SELECT_PRODUCERS_INVENTORY_MODE
import com.ustadmobile.core.view.SelectProducersView.Companion.ARG_SELECT_PRODUCERS_INVENTORY_SELECTION
import com.ustadmobile.core.view.SelectProducersView.Companion.ARG_SELECT_PRODUCERS_SALE_ITEM_PREORDER
import com.ustadmobile.core.view.SelectProducersView.Companion.ARG_SELECT_PRODUCERS_SALE_UID
import com.ustadmobile.core.view.SelectSaleProductView.Companion.ARG_INVENTORY_MODE
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.SaleProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


/**
 * Presenter for SelectSaleProduct view
 */
class SelectSaleProductPresenter(context: Any,
                                 arguments: Map<String, String>?,
                                 view: SelectSaleProductView,
                                 private val catalogMode: Boolean,
                                 private var inventoryMode:Boolean = false)
    : UstadBaseController<SelectSaleProductView>(context, arguments!!, view) {

    private var recentProvider: DataSource.Factory<Int, SaleProduct>? = null
    private var categoryProvider: DataSource.Factory<Int, SaleProduct>? = null
    private var collectionProvider: DataSource.Factory<Int, SaleProduct>? = null

    internal var repository: UmAppDatabase
    internal var database: UmAppDatabase

    internal var saleProductDao: SaleProductDao
    internal var personDao : PersonDao
    internal var productParentJoinDao: SaleProductParentJoinDao
    internal var impl: UstadMobileSystemImpl

    private var producerUid: Long = 0
    private var saleItemUid: Long = 0
    private var saleUid: Long = 0

    var searchQuery: String = "%%"
    var preOrder = false

    var loggedInPersonUid : Long = 0
    var loggedInPerson : Person? = null

    fun setQuerySearch(query:String){
        searchQuery = "%$query%"
    }

    fun isLoggedInPersonAdmin(): Boolean{
        if(loggedInPerson == null){
            return false
        }else{
            return loggedInPerson!!.admin
        }
    }

    init {

        impl = UstadMobileSystemImpl.instance

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        database = UmAccountManager.getActiveDatabase(context)
        loggedInPersonUid = UmAccountManager.getActivePersonUid(context)

        saleProductDao = repository.saleProductDao
        productParentJoinDao = repository.saleProductParentJoinDao
        personDao = repository.personDao

        if (arguments!!.containsKey(ARG_PRODUCER_UID)) {
            producerUid = (arguments.get(ARG_PRODUCER_UID)!!.toLong())
        }
        if (arguments.containsKey(ARG_SELECT_PRODUCERS_SALE_UID)) {
            saleUid = (arguments.get(ARG_SELECT_PRODUCERS_SALE_UID)!!.toLong())
        }
        if (arguments.containsKey(ARG_SALE_ITEM_UID)) {
            saleItemUid = (arguments.get(ARG_SALE_ITEM_UID)!!.toLong())
        }

        if(arguments.containsKey(ARG_INVENTORY_MODE)){
            inventoryMode = true
        }

        if(arguments.containsKey(ARG_SELECT_PRODUCERS_SALE_ITEM_PREORDER)){
            val pos = arguments[ARG_SELECT_PRODUCERS_SALE_ITEM_PREORDER].toString().toLowerCase()
            if(pos.equals("true")){
                preOrder = true
            }
        }

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        GlobalScope.launch(Dispatchers.Main) {
            loggedInPerson = personDao.findByUidAsync(loggedInPersonUid)
            //view.showAddButton(loggedInPerson!!.admin)
            updateProviders()
        }

        if(inventoryMode){
            val selectTypeTitle = impl.getString(MessageID.select_type, context)
            view.updateToolbar(selectTypeTitle)
        }
    }

    fun updateProviders(){
        updateRecentProvider()
        updateCategoryProvider()
        updateCollectionProvider()
    }

    private fun updateRecentProvider() {

        recentProvider = saleProductDao.findActiveProductsProvider(loggedInPersonUid, searchQuery)
        view.setRecentProvider(recentProvider!!)

    }

    private fun updateCategoryProvider() {

        categoryProvider = saleProductDao.sortAndFindActiveCategoriesProvider(loggedInPersonUid, searchQuery,0)
        view.setCategoryProvider(categoryProvider!!)

    }

    private fun updateCollectionProvider() {
        collectionProvider = productParentJoinDao.findAllCategoriesInCollection(loggedInPersonUid, searchQuery)
        view.setCollectionProvider(collectionProvider!!)
    }

    fun handleClickProduct(productUid: Long, isCategory: Boolean){
        handleClickProductMulti(productUid, isCategory, false)
    }

    fun handleClickProductMulti(productUid: Long, isCategory: Boolean, editMode: Boolean) {

        val args = HashMap<String, String>()
        if(inventoryMode){

            if(saleUid != 0L) {
                args.put(ARG_SELECT_PRODUCERS_SALE_UID, saleUid.toString())
            }

            if(arguments.containsKey(ARG_SELECT_PRODUCERS_INVENTORY_ADDITION)){
                args.put(ARG_SELECT_PRODUCERS_INVENTORY_ADDITION,
                        arguments!!.get(ARG_SELECT_PRODUCERS_INVENTORY_ADDITION)!!)
            }

            if(arguments.containsKey(ARG_SELECT_PRODUCERS_INVENTORY_SELECTION)){
                args.put(ARG_SELECT_PRODUCERS_INVENTORY_SELECTION,
                        arguments!!.get(ARG_SELECT_PRODUCERS_INVENTORY_SELECTION)!!)
            }

            if(preOrder) {
                args[ARG_SELECT_PRODUCERS_SALE_ITEM_PREORDER] = "true"
                args[ARG_SALE_ITEM_PRODUCT_UID] = productUid.toString()
                args[ARG_PRODUCER_UID] = loggedInPersonUid.toString()
                args[ARG_SALE_ITEM_DETAIL_PREORDER] = "true"
            }

            if (isCategory) {
                args.put(ARG_SALEPRODUCT_UID, productUid.toString())
                args.put(ARG_SALEPRODUCT_CATEGORY_INVENTORY_MODE, "true")
                view.finish()
                impl.go(SaleProductCategoryListView.VIEW_NAME, args, context)
            } else {
                args.put(SelectProducersView.ARG_SELECT_PRODUCERS_SALE_PRODUCT_UID,
                        productUid.toString())
                //Go to SelectProducers
                view.finish()
                if(preOrder){
                    impl.go(SaleItemDetailView.VIEW_NAME, args, context)
                }else {
                    impl.go(SelectProducersView.VIEW_NAME, args, context)
                }
            }
        }
        else if (catalogMode) {

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
                val result = saleProductDao.findByNameAsync("Collections")
                handleClickProduct(result!!.saleProductUid, true)

            }catch(e:Exception){
                println(e.message)
            }
        }
    }
}
