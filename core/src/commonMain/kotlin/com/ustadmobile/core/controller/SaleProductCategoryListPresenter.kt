package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.db.dao.SaleProductDao
import com.ustadmobile.core.db.dao.SaleProductParentJoinDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView.Companion.ARG_ADD_TO_CATEGORY_TYPE_CATEGORY
import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView.Companion.ARG_ADD_TO_CATEGORY_TYPE_ITEM
import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView.Companion.ARG_SALE_PRODUCT_CATEGORY_TO_ASSIGN_TO_UID
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_PRODUCT_UID
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_UID
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_MORE_CATEGORY
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_MORE_RECENT
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_PASS_PRODUCER_UID
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_PASS_SALE_ITEM_UID
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_SALEPRODUCT_UID
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_SELECT_INVENTORY
import com.ustadmobile.core.view.SaleProductCategoryListView.Companion.ARG_SELECT_PRODUCT
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

    private var idToOrderInteger: MutableMap<Long, Int>? = null
    private var currentSortOrder = 0

    private var selectInventoryMode = false

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
        if (arguments!!.containsKey(ARG_SELECT_INVENTORY)) {
            if (arguments!!.get(ARG_SELECT_INVENTORY) == "true") {
                selectInventoryMode = true
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

        if (selectProductMode || selectInventoryMode) {
            view.hideFAB(true)
        }

        updateSortSpinnerPreset()
    }

    private fun setCategoryLiveOnView(saleProductCategory:SaleProduct?){

        if (saleProductCategory != null) {
            currentSaleProductCategory = saleProductCategory
        } else {
            currentSaleProductCategory = SaleProduct("", "",
                    true, false)
        }
        //Show more recent and show category ?
        setCategoryOnView(true, true)
    }

    /**
     * Updates the sort by drop down (spinner) on the Class list. For now the sort options are
     * defined within this method and will automatically update the sort options without any
     * database call.
     */
    private fun updateSortSpinnerPreset() {
        val presetAL = ArrayList<String>()

        idToOrderInteger = HashMap<Long, Int>()

        presetAL.add(impl.getString(MessageID.sort_by_name_asc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SaleDao.SORT_ORDER_NAME_ASC)
        presetAL.add(impl.getString(MessageID.sort_by_name_desc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SaleDao.SORT_ORDER_NAME_DESC)

        val sortPresets = SaleListPresenter.arrayListToStringArray(presetAL)

        view.updateSortPresets(sortPresets)
    }

    fun handleChangeSortOrder(order: Long) {
        var orderI = order + 1

        if (idToOrderInteger!!.containsKey(orderI)) {
            currentSortOrder = idToOrderInteger!![orderI]!!
            getAndSetProvider(currentSortOrder)
        }
    }

    private fun getAndSetProvider(sortCode: Int) {

        var allMode : Boolean = false
        if(currentSaleProductCategory != null) {
            if (currentSaleProductCategory!!.saleProductUid != 0L) {
                itemProvider = productParentJoinDao.sortAndFindAllItemsInACategory(sortCode,
                        currentSaleProductCategory!!.saleProductUid)
                categoryProvider = productParentJoinDao.sortAndFindAllCategoriesInACategory(sortCode,
                        currentSaleProductCategory!!.saleProductUid)
                allMode = false
            } else {
                itemProvider = productDao.sortAndFindAllActiveSNWIProvider(sortCode)
                categoryProvider = productDao.sortAndFindActiveCategoriesProvider("", sortCode)
                allMode = true
            }
            view.setListProvider(itemProvider, allMode)
            view.setCategoriesListProvider(categoryProvider, allMode)
        }

        if (moreRecent)
            view.runOnUiThread(Runnable{
                view.updateToolbar(impl.getString(MessageID.most_recent, context))
            })
        if (moreCategory)
            view.runOnUiThread(Runnable{
                view.updateToolbar(impl.getString(MessageID.categories, context))
            })




    }

    private fun setCategoryOnView(showRecent: Boolean, showCategory: Boolean) {
        //Update on view
        view.initFromSaleCategory(currentSaleProductCategory!!)

        //Get showCategory and item providers

        //Get SaleNameWithImage for all items
        var allMode : Boolean = false
        if (currentSaleProductCategory!!.saleProductUid != 0L) {
            view.hideEditMenu(false)
            itemProvider = productParentJoinDao.sortAndFindAllItemsInACategory(0,
                    currentSaleProductCategory!!.saleProductUid)
            categoryProvider = productParentJoinDao.sortAndFindAllCategoriesInACategory(0,
                    currentSaleProductCategory!!.saleProductUid)
            allMode = false
        } else {
            allMode = true
            view.hideEditMenu(true)
            itemProvider = productDao.sortAndFindAllActiveSNWIProvider(0)
            categoryProvider = productDao.sortAndFindActiveCategoriesProvider("" , 0)
        }

        if (showRecent)
            view.runOnUiThread(Runnable{
                //view.updateToolbar(impl.getString(MessageID.most_recent, context))
                view.setListProvider(itemProvider, allMode)
            })
        if (showCategory)
            view.runOnUiThread(Runnable{
                //view.updateToolbar(impl.getString(MessageID.categories, context))
                view.setCategoriesListProvider(categoryProvider, allMode)
            })

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
            } else if (selectInventoryMode){
                //Go to SelectProducersPresenter

                if(arguments.containsKey(SelectProducersView.ARG_SELECT_PRODUCERS_INVENTORY_ADDITION)){
                    args.put(SelectProducersView.ARG_SELECT_PRODUCERS_INVENTORY_ADDITION,
                            arguments!!.get(SelectProducersView.ARG_SELECT_PRODUCERS_INVENTORY_ADDITION)!!)
                }

                if(arguments.containsKey(SelectProducersView.ARG_SELECT_PRODUCERS_INVENTORY_SELECTION)){
                    args.put(SelectProducersView.ARG_SELECT_PRODUCERS_INVENTORY_SELECTION,
                            arguments!!.get(SelectProducersView.ARG_SELECT_PRODUCERS_INVENTORY_SELECTION)!!)
                }

                view.finish()
                args.put(SelectProducersView.ARG_SELECT_PRODUCERS_SALE_PRODUCT_UID, productUid.toString())
                impl.go(SelectProducersView.VIEW_NAME, args, context)


            } else{
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
