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
import com.ustadmobile.core.view.SaleProductShowcaseView.Companion.ARG_SALE_PRODUCT_SHOWCASE_SALE_PRODUCT_UID
import com.ustadmobile.core.view.SelectProducerView.Companion.ARG_PRODUCER_UID
import com.ustadmobile.core.view.SelectProducersView.Companion.ARG_SELECT_PRODUCERS_INVENTORY_ADDITION
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

    private lateinit var recentProvider: DataSource.Factory<Int, SaleProduct>
    private lateinit var categoryProvider: DataSource.Factory<Int, SaleProduct>
    private lateinit var collectionProvider: DataSource.Factory<Int, SaleProduct>

    internal var repository: UmAppDatabase = UmAccountManager.getRepositoryForActiveAccount(context)
    internal var database: UmAppDatabase = UmAccountManager.getActiveDatabase(context)

    internal var saleProductDao: SaleProductDao
    internal var personDao : PersonDao
    private var productParentJoinDao: SaleProductParentJoinDao
    internal var impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance

    private var producerUid: Long = 0
    private var saleItemUid: Long = 0
    private var saleUid: Long = 0

    var searchQuery: String = "%%"
    private var preOrder = false

    var loggedInPersonUid : Long = 0
    private var loggedInPerson : Person? = null

    fun setQuerySearch(query:String){
        searchQuery = "%$query%"
    }

    fun isLoggedInPersonAdmin(): Boolean{
        return if(loggedInPerson == null){
            false
        }else{
            loggedInPerson!!.admin
        }
    }

    init {
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
        view.setRecentProvider(recentProvider)

    }

    private fun updateCategoryProvider() {

        categoryProvider = saleProductDao.sortAndFindActiveCategoriesProvider(loggedInPersonUid,
                searchQuery,0, impl.getLocale(context))
        view.setCategoryProvider(categoryProvider)

    }

    private fun updateCollectionProvider() {
        collectionProvider = productParentJoinDao.findAllCategoriesInCollection(loggedInPersonUid,
                searchQuery)
        view.setCollectionProvider(collectionProvider)
    }

    fun handleClickProduct(productUid: Long, isCategory: Boolean){
        handleClickProductMulti(productUid, isCategory, false)
    }

    fun handleClickProductMulti(productUid: Long, isCategory: Boolean, editMode: Boolean) {

        val args = HashMap<String, String>()
        if(inventoryMode){

            if(saleUid != 0L) {
                args[ARG_SELECT_PRODUCERS_SALE_UID] = saleUid.toString()
            }

            if(arguments.containsKey(ARG_SELECT_PRODUCERS_INVENTORY_ADDITION)){
                args[ARG_SELECT_PRODUCERS_INVENTORY_ADDITION] = arguments[ARG_SELECT_PRODUCERS_INVENTORY_ADDITION]!!
            }

            if(arguments.containsKey(ARG_SELECT_PRODUCERS_INVENTORY_SELECTION)){
                args[ARG_SELECT_PRODUCERS_INVENTORY_SELECTION] = arguments.get(ARG_SELECT_PRODUCERS_INVENTORY_SELECTION)!!
            }

            if(preOrder) {
                args[ARG_SELECT_PRODUCERS_SALE_ITEM_PREORDER] = "true"
                args[ARG_SALE_ITEM_PRODUCT_UID] = productUid.toString()
                args[ARG_PRODUCER_UID] = loggedInPersonUid.toString()
                args[ARG_SALE_ITEM_DETAIL_PREORDER] = "true"
            }

            if (isCategory) {
                args[ARG_SALEPRODUCT_UID] = productUid.toString()
                args[ARG_SALEPRODUCT_CATEGORY_INVENTORY_MODE] = "true"
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
                    args[ARG_SALE_PRODUCT_UID] = productUid.toString()
                    impl.go(SaleProductDetailView.VIEW_NAME, args, context)
//                    args[ARG_SALE_PRODUCT_SHOWCASE_SALE_PRODUCT_UID] = productUid.toString()
//                    impl.go(SaleProductShowcaseView.VIEW_NAME, args, context)
                }else {
                    args[ARG_SALEPRODUCT_UID] = productUid.toString()
                    impl.go(SaleProductCategoryListView.VIEW_NAME, args, context)
                }
            } else {
                args[ARG_SALE_PRODUCT_UID] = productUid.toString()
                if(editMode) {
                    impl.go(SaleProductDetailView.VIEW_NAME, args, context)
                }else {
                    args[ARG_SALE_PRODUCT_SHOWCASE_SALE_PRODUCT_UID] = productUid.toString()
                    impl.go(SaleProductShowcaseView.VIEW_NAME, args, context)
                }
            }
        } else {
            //Need to select the product.
            if (isCategory) {
                args[ARG_SALEPRODUCT_UID] = productUid.toString()
                args[ARG_PASS_PRODUCER_UID] = producerUid.toString()
                args[ARG_PASS_SALE_ITEM_UID] = saleItemUid.toString()
                args[ARG_SELECT_PRODUCT] = "true"
                impl.go(SaleProductCategoryListView.VIEW_NAME, args, context)
            } else {
                args[ARG_SALE_ITEM_PRODUCT_UID] = productUid.toString()
                args[ARG_PRODUCER_UID] = producerUid.toString()
                args[ARG_SALE_ITEM_UID] = saleItemUid.toString()
                args[ARG_SELECT_PRODUCT] = "true"
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

    fun handleDeleteSaleProduct(productUid: Long, isCategory: Boolean) {
        GlobalScope.launch {
            try{
                val saleProduct = saleProductDao.findByUidAsync(productUid)
                //Either you are an admin or the owner of this product
                if(loggedInPersonUid == saleProduct!!.saleProductPersonAdded ||
                        loggedInPerson!!.admin){
                    if (isCategory) {
                        //Only admins can delete categories.
                        if(loggedInPerson!!.admin) {
                            saleProductDao.inactivateEntityAsync(productUid)
                            view.runOnUiThread(Runnable {
                                view.showMessage(MessageID.category_deleted) })
                        }
                    } else {
                        saleProductDao.inactivateEntityAsync(productUid)
                        view.runOnUiThread(Runnable { view.showMessage(MessageID.item_deleted) })
                    }
                }
            }catch(e:Exception){
                println(e.message)
            }
        }
    }

    fun handleClickRecentMore() {
        val args = HashMap<String, String>()
        args[ARG_MORE_RECENT] = "true"
        if (catalogMode) {
            impl.go(SaleProductCategoryListView.VIEW_NAME, args, context)

        } else {
            //Need to select the product.

            //Pass it on
            args[ARG_PASS_PRODUCER_UID] = producerUid.toString()
            args[ARG_PASS_SALE_ITEM_UID] = saleItemUid.toString()
            args[ARG_SELECT_PRODUCT] = "true"

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
