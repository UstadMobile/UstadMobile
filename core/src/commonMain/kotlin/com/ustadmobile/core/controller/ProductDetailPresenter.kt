package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.view.ProductDetailView
import com.ustadmobile.core.view.ProductEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.withTimeout
import org.kodein.di.DI


class ProductDetailPresenter(context: Any,
                             arguments: Map<String, String>, view: ProductDetailView,
                             di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadDetailPresenter<ProductDetailView, ProductWithInventoryCount>(context, arguments, view,
        di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        //TODO: this
        return true
    }


    override fun handleClickEdit() {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        systemImpl.go(ProductEditView.VIEW_NAME , mapOf(ARG_ENTITY_UID to entityUid.toString()), context)
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ProductWithInventoryCount? {
        val productUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val loggedInPersonUid = accountManager.activeAccount.personUid

        val productWithCount = withTimeout(2000){
            db.productDao.findProductWithInventoryCountAsync(productUid)
        }?: ProductWithInventoryCount()

        //1. Get categories
        view.productCategories = repo.productDao.findAllCategoriesOfProductUid(productUid)

        //2. Stock list
        view.stockList = repo.inventoryTransactionDao.getStockListByProduct(productUid, loggedInPersonUid)

        //3.TransactionList
        view.transactionList = repo.inventoryTransactionDao.getProductTransactionDetail(productUid)

        //4. Pictures TODO
        view.pictureList = repo.productDao.findAllProductPictures(productUid)

        return productWithCount
    }



}