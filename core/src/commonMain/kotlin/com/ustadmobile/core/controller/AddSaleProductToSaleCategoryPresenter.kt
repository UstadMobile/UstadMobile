package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SaleProductDao
import com.ustadmobile.core.db.dao.SaleProductParentJoinDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView
import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView.Companion.ARG_ADD_TO_CATEGORY_TYPE_CATEGORY
import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView.Companion.ARG_ADD_TO_CATEGORY_TYPE_ITEM
import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView.Companion.ARG_SALE_PRODUCT_CATEGORY_TO_ASSIGN_TO_UID
import com.ustadmobile.core.view.SaleProductDetailView
import com.ustadmobile.core.view.SaleProductDetailView.Companion.ARG_ASSIGN_TO_CATEGORY_UID
import com.ustadmobile.core.view.SaleProductDetailView.Companion.ARG_NEW_CATEGORY
import com.ustadmobile.core.view.SaleProductDetailView.Companion.ARG_NEW_TITLE
import com.ustadmobile.lib.db.entities.SaleProduct
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Presenter for AddSaleProductToSaleCategory view
 */
class AddSaleProductToSaleCategoryPresenter(context: Any, arguments: Map<String, String?>,
                                            view: AddSaleProductToSaleCategoryView)
    : UstadBaseController<AddSaleProductToSaleCategoryView>(context, arguments, view) {

    private var umProvider: DataSource.Factory<Int, SaleProduct>? = null
    internal var repository: UmAppDatabase
    private val providerDao: SaleProductDao
    private val productParentJoinDao: SaleProductParentJoinDao
    private var assignToThisSaleProductCategoryUid: Long = 0
    private val impl: UstadMobileSystemImpl
    private var isCategory = false


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        providerDao = repository.saleProductDao
        productParentJoinDao = repository.saleProductParentJoinDao

        impl = UstadMobileSystemImpl.Companion.instance

        if (arguments.containsKey(ARG_SALE_PRODUCT_CATEGORY_TO_ASSIGN_TO_UID)) {
            assignToThisSaleProductCategoryUid =
                    (arguments.get(ARG_SALE_PRODUCT_CATEGORY_TO_ASSIGN_TO_UID)!!.toLong())
        }


    }

    fun handleAddNewItem() {
        val args = HashMap<String, String>()
        if (isCategory) {
            args.put(ARG_NEW_CATEGORY, "true")
            args.put(ARG_NEW_CATEGORY, "true")
        } else {
            args.put(ARG_NEW_TITLE, "true")
        }
        args.put(ARG_ASSIGN_TO_CATEGORY_UID,
                assignToThisSaleProductCategoryUid.toString())
        impl.go(SaleProductDetailView.VIEW_NAME, args, context)
    }

    fun handleClickProduct(productUid: Long) {
        GlobalScope.launch{
        productParentJoinDao.createJoin(productUid, assignToThisSaleProductCategoryUid, true)
        }
        view.finish()

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        var addTitle = ""
        var toolbarTitle = ""
        if (arguments.containsKey(ARG_ADD_TO_CATEGORY_TYPE_ITEM)) {
            addTitle = impl.getString(MessageID.create_new_item, context)
            toolbarTitle = impl.getString(MessageID.add_item, context)
            //Get provider
            umProvider = providerDao.findAllActiveProductsNotInCategorySNWIProvider(
                    assignToThisSaleProductCategoryUid)
            view.setListProvider(umProvider!!)
            isCategory = false
        } else if (arguments.containsKey(ARG_ADD_TO_CATEGORY_TYPE_CATEGORY)) {
            addTitle = impl.getString(MessageID.create_new_subcategory, context)
            toolbarTitle = impl.getString(MessageID.add_subcategory, context)
            isCategory = true
            //Get provider
            umProvider = providerDao.findAllActiveCategoriesNotInCategorySNWIProvider(
                    assignToThisSaleProductCategoryUid)
            view.setListProvider(umProvider!!)
        }
        view.setAddtitle(addTitle)
        view.setToolbarTitle(toolbarTitle)

    }


}
