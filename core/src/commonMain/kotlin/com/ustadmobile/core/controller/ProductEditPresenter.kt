package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.ProductDetailView
import com.ustadmobile.core.view.ProductEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.lib.db.entities.ProductPicture
import com.ustadmobile.lib.db.entities.ProductCategoryJoin
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.kodein.di.DI
import com.ustadmobile.door.ext.onDbThenRepoWithTimeout
import kotlinx.serialization.builtins.ListSerializer


class ProductEditPresenter(context: Any,
                           arguments: Map<String, String>, view: ProductEditView, di: DI,
                           lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<ProductEditView, Product>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private val categoryEditHelper =
            DefaultOneToManyJoinEditHelper(Category::categoryUid,
            "state_Category_list", ListSerializer(Category.serializer()),
                ListSerializer(Category.serializer()), this, Category::class) { categoryUid = it }

    fun handleAddOrEditCategory(category: Category) {
        categoryEditHelper.onEditResult(category)
    }

    fun handleRemoveCategory(category: Category) {
        categoryEditHelper.onDeactivateEntity(category)
    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.categories = categoryEditHelper.liveList
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Product? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val product = withTimeout(2000){
            db.productDao.findByUidAsync(entityUid)
        }?: Product()

        view.productPicture = db.onDbThenRepoWithTimeout(2000) {dbToUse, _ ->
            dbToUse.takeIf { entityUid != 0L }?.productPictureDao?.findByProductUidAsync(entityUid)
        } ?: ProductPicture()


        val categoryList = withTimeout(2000){
            db.productDao.findAllCategoriesOfProductUidAsync(entityUid)
        }
        categoryEditHelper.liveList.sendValue(categoryList)

        return product

    }

    override fun onLoadFromJson(bundle: Map<String, String>): Product? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: Product? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, Product.serializer(), entityJsonStr)
        }else {
            editEntity = Product()
        }

        view.categories = categoryEditHelper.liveList

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: Product) {


        GlobalScope.launch(doorMainDispatcher()) {
            val loggedInPersonUid = accountManager.activeAccount.personUid
            entity.productPersonAdded = loggedInPersonUid
            if(entity.productUid == 0L) {
                entity.productUid = repo.productDao.insertAsync(entity)
            }else {
                repo.productDao.updateAsync(entity)
            }


            val categoriesToInsert = categoryEditHelper.entitiesToInsert
            val categoriesToDelete = categoryEditHelper.primaryKeysToDeactivate

            repo.productCategoryJoinDao.insertListAsync(categoriesToInsert.map {
                ProductCategoryJoin().apply {
                    productCategoryJoinCategoryUid = it.categoryUid
                    productCategoryJoinProductUid = entity.productUid ?: 0L
                    productCategoryJoinDateCreated = systemTimeInMillis()
                }
            })

            repo.productCategoryJoinDao.deactivateByCategoryAndProductUid(entity.productUid,
                    categoriesToDelete)

            val productPicture = view.productPicture
            if(productPicture != null) {
                productPicture.productPictureProductUid = entity.productUid

                if(productPicture.productPictureUid == 0L) {
                    repo.productPictureDao.insertAsync(productPicture)
                }else {
                    repo.productPictureDao.updateAsync(productPicture)
                }
            }

            onFinish(ProductDetailView.VIEW_NAME, entity.productUid, entity)
        }
    }


}