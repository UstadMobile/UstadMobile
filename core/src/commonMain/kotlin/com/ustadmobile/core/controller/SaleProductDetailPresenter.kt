package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SaleProductDao
import com.ustadmobile.core.db.dao.SaleProductParentJoinDao
import com.ustadmobile.core.db.dao.SaleProductPictureDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.SaleProductDetailView
import com.ustadmobile.core.view.SaleProductDetailView.Companion.ARG_ASSIGN_TO_CATEGORY_UID
import com.ustadmobile.core.view.SaleProductDetailView.Companion.ARG_NEW_CATEGORY
import com.ustadmobile.core.view.SaleProductDetailView.Companion.ARG_NEW_TITLE
import com.ustadmobile.core.view.SaleProductDetailView.Companion.ARG_SALE_PRODUCT_UID
import com.ustadmobile.core.view.SaleProductImageListView
import com.ustadmobile.core.view.SelectProducersView
import com.ustadmobile.core.view.SelectProducersView.Companion.ARG_SELECT_PRODUCERS_INVENTORY_ADDITION
import com.ustadmobile.core.view.SelectSaleProductView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.SaleProduct
import com.ustadmobile.lib.db.entities.SaleProductParentJoin
import com.ustadmobile.lib.db.entities.SaleProductPicture
import com.ustadmobile.lib.db.entities.SaleProductSelected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * Presenter for SaleProductDetail view
 */
class SaleProductDetailPresenter(context: Any,
                                 arguments: Map<String, String>?,
                                 view: SaleProductDetailView)
    : UstadBaseController<SaleProductDetailView>(context, arguments!!, view) {

    internal var repository: UmAppDatabase
    private val saleProductDao: SaleProductDao
    private val productParentJoinDao: SaleProductParentJoinDao
    private val impl: UstadMobileSystemImpl
    var currentSaleProduct: SaleProduct? = null
        private set
    internal var categoriesProvider: DataSource.Factory<Int, SaleProductSelected>? = null
    private var isCategory: Boolean = false

    internal var pictureDao: SaleProductPictureDao

    private val selectedToCategoriesUid: HashMap<Long, Boolean>

    private lateinit var pictureLiveData: DoorLiveData<SaleProductPicture?>

    private var productUid : Long = 0L

    private var newSaleProduct: Boolean = false

    private var loggedInPersonUid: Long = 0

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        saleProductDao = repository.saleProductDao
        productParentJoinDao = repository.saleProductParentJoinDao
        pictureDao = UmAccountManager.getRepositoryForActiveAccount(context).saleProductPictureDao

        impl = UstadMobileSystemImpl.instance

        selectedToCategoriesUid = HashMap<Long, Boolean>()
        loggedInPersonUid = UmAccountManager.getActivePersonUid(context)

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        //Update toolbar title
        var toolbarTitle = ""
        var categoryTitle = ""
        if (arguments.containsKey(ARG_NEW_TITLE)) {
            isCategory = false
            toolbarTitle = impl.getString(MessageID.create_new_item, context)
            categoryTitle = impl.getString(MessageID.category, context)
        } else if (arguments.containsKey(ARG_NEW_CATEGORY)) {
            toolbarTitle = impl.getString(MessageID.create_new_subcategory, context)
            categoryTitle = impl.getString(MessageID.subcategory, context)
            isCategory = true
        }
        view.updateToolbarTitle(toolbarTitle)
        view.updateCategoryTitle(categoryTitle)

        //Get SaleProductSelected and update the view
        GlobalScope.launch {
            if (arguments.containsKey(ARG_SALE_PRODUCT_UID)) {

                newSaleProduct = false

                productUid = arguments[ARG_SALE_PRODUCT_UID]!!.toLong()

                val product = saleProductDao.findByUidAsync(productUid)
                view.runOnUiThread(Runnable {
                    updateView(product)
                })


            } else {
                newSaleProduct = true

                currentSaleProduct = SaleProduct( "", "", isCategory, false)
                currentSaleProduct!!.saleProductDateAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                currentSaleProduct!!.saleProductPersonAdded = loggedInPersonUid

                currentSaleProduct!!.saleProductUid = saleProductDao.insertAsync(currentSaleProduct!!)
                productUid = currentSaleProduct!!.saleProductUid

                val product = saleProductDao.findByUidAsync(currentSaleProduct!!.saleProductUid)
                view.runOnUiThread(Runnable {
                    updateView(product)
                })
            }

            val imagesCount = pictureDao.findTotalNumberOfPicturesForAProduct(productUid)
            view.updateImagesCounter(imagesCount)
        }

    }

    private fun updateView(saleProduct: SaleProduct?){
        currentSaleProduct = saleProduct
        updateView()
    }

    private fun updateView() {
        val itemUid: Long
        itemUid = currentSaleProduct!!.saleProductUid

        view.runOnUiThread(Runnable {
            view.initFromSaleProduct(currentSaleProduct!!, newSaleProduct)
        })

        //Assign
        if (arguments.containsKey(ARG_ASSIGN_TO_CATEGORY_UID)) {
            val defaultJoin = SaleProductParentJoin(itemUid,
                    (arguments[ARG_ASSIGN_TO_CATEGORY_UID]!!.toLong()), true)
            productParentJoinDao.insert(defaultJoin)
        }

        //Update provider:
        categoriesProvider = productParentJoinDao.findAllSelectedCategoriesForSaleProductProvider(
                itemUid)
        GlobalScope.launch(Dispatchers.Main) {
            view.runOnUiThread(Runnable {
                if(categoriesProvider!=null)
                    view.setListProvider(categoriesProvider!!)
            })
        }

        //Update image on view
        GlobalScope.launch {
            val productPicture =
                    pictureDao.findBySaleProductUidAsync2(currentSaleProduct!!.saleProductUid)
            if (productPicture != null) {
                view.updateImageOnView(pictureDao.getAttachmentPath(productPicture)!!)
            }
        }

        //Observe the picture
        pictureLiveData = pictureDao.findByProductUidLive(currentSaleProduct!!.saleProductUid)
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            pictureLiveData.observeWithPresenter(thisP, thisP::handleProductPictureChanged)
        }

    }

    private fun handleProductPictureChanged(productPicture: SaleProductPicture?) {
        if (productPicture != null) {
            view.runOnUiThread(Runnable{
                view.updateImageOnView(
                        pictureDao.getAttachmentPath(productPicture)!!)
            })
        }
    }

    fun handleClickSave() {

        var forwardToInventory = false
        if(arguments.containsKey(SaleProductDetailView.ARG_ADD_INVENTORY_POST_SAVE)){
            val value = arguments.get(SaleProductDetailView.ARG_ADD_INVENTORY_POST_SAVE)
            if(value.equals("true")){
                forwardToInventory = true
            }
        }

        val selectedIterator = selectedToCategoriesUid.keys.iterator()
        while (selectedIterator.hasNext()) {
            val productUid = selectedIterator.next()
            val selected = selectedToCategoriesUid.get(productUid)

            //Update assignment.
            GlobalScope.launch {
                productParentJoinDao.createJoin(currentSaleProduct!!.saleProductUid,
                        productUid, selected!!)
            }

        }
        currentSaleProduct!!.saleProductActive = true
        GlobalScope.launch {
            try {
                saleProductDao.updateAsync(currentSaleProduct!!)
                if(forwardToInventory){
                    val args = HashMap<String, String>()
                    args.put(SelectProducersView.ARG_SELECT_PRODUCERS_INVENTORY_ADDITION, "true")
                    args.put(SelectProducersView.ARG_SELECT_PRODUCERS_SALE_PRODUCT_UID,
                            productUid.toString())
                    impl.go(SelectProducersView.VIEW_NAME, args, context)
                }
                view.finish()
            }catch(e:Exception){
                print(e.message)
            }
        }
    }

    fun goToManageImages(){
        val args = HashMap<String, String>()
        args.put(SaleProductImageListView.ARG_MANAGE_IMAGES_SALE_PRODUCT_UID,
                currentSaleProduct!!.saleProductUid.toString())
        impl.go(SaleProductImageListView.VIEW_NAME, args, context)
    }

    fun handleCheckboxChanged(state: Boolean, saleProductUid: Long) {
        selectedToCategoriesUid.put(saleProductUid, state)
    }

    fun updateTitleEng(title: String) {
        currentSaleProduct!!.saleProductName = title
    }

    fun updateTitleDari(title: String) {
        currentSaleProduct!!.saleProductNameDari = title
    }

    fun updateTitlePashto(title: String) {
        currentSaleProduct!!.saleProductNamePashto = title
    }

    fun updateDescEng(dec: String) {
        currentSaleProduct!!.saleProductDesc = dec
    }

    fun updateDescDari(desc: String) {
        currentSaleProduct!!.saleProductDescDari = desc
    }

    fun updateDescPashto(desc: String) {
        currentSaleProduct!!.saleProductDescPashto = desc
    }

    fun handleCompressedImage(imageFilePath: String) {

        //Create picture entry

        var productPictureUid : Long = 0L

        GlobalScope.launch {
            var existingPP = pictureDao.findBySaleProductUidAsync2(currentSaleProduct!!.saleProductUid)
            if(existingPP == null){
                existingPP = SaleProductPicture()
                existingPP.saleProductPictureSaleProductUid = currentSaleProduct!!.saleProductUid
                existingPP.saleProductPictureTimestamp = DateTime.nowUnixLong()
                productPictureUid = pictureDao.insertAsync(existingPP)
                existingPP.saleProductPictureUid = productPictureUid

            }

            if(existingPP!=null) {
                pictureDao.setAttachment(existingPP, imageFilePath)
            }

            //Update the entity as well.
            saleProductDao.updateAsync(currentSaleProduct!!)

            //Update image on view
            GlobalScope.launch {
                val productPicture =
                        pictureDao.findBySaleProductUidAsync2(currentSaleProduct!!.saleProductUid)
                if (productPicture != null) {
                    view.updateImageOnView(pictureDao.getAttachmentPath(productPicture)!!)
                }
            }

        }
    }

}
