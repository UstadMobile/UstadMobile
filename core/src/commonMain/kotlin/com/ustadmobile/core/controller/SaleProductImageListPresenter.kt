package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SaleProductPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonPictureDialogView
import com.ustadmobile.core.view.SaleProductImageListView
import com.ustadmobile.core.view.SaleProductImageListView.Companion.ARG_MANAGE_IMAGES_SALE_PRODUCT_UID
import com.ustadmobile.lib.db.entities.SaleProductPicture
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 *  Presenter for SaleProductImageList View
 **/
class SaleProductImageListPresenter(context: Any,
                                    arguments: Map<String, String>?,
                                    view: SaleProductImageListView,
                                    val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                                    private val repository: UmAppDatabase =
                                            UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<SaleProductImageListView>(context, arguments!!, view) {


    private var productPictureDao: SaleProductPictureDao = repository.saleProductPictureDao

    private lateinit var factory: DataSource.Factory<Int, SaleProductPicture>

    var productUid : Long = 0

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (arguments.containsKey(ARG_MANAGE_IMAGES_SALE_PRODUCT_UID)) {
            productUid = arguments[ARG_MANAGE_IMAGES_SALE_PRODUCT_UID]!!.toLong()
        }

        getAndSetProvider()
    }

    private fun getAndSetProvider() {
        factory = productPictureDao.findAllByProductByIndex(productUid)
        view.setListProvider(factory)
    }

    fun handleClickDone(){
        view.finish()
    }

    fun handleClickAddPicture(){
        view.showGetImageAlertDialog()
    }

    fun handleNewCompressedImage(imageFilePath: String) {
        //Create picture entry

        GlobalScope.launch {

            val newProductPicture = SaleProductPicture()
            newProductPicture.saleProductPictureSaleProductUid = productUid
            newProductPicture.saleProductPictureTimestamp = DateTime.nowUnixLong()
            val maxIndex = productPictureDao.findMaxIndexForSaleProductPicture(productUid)
            newProductPicture.saleProductPictureIndex = maxIndex + 1
            val productPictureUid = productPictureDao.insertAsync(newProductPicture)
            newProductPicture.saleProductPictureUid = productPictureUid

            productPictureDao.setAttachment(newProductPicture, imageFilePath)
        }
    }

    fun openPictureDialog(imagePath: String) {
        val args = HashMap<String, String>()
        args.put(PersonPictureDialogView.ARG_PERSON_IMAGE_PATH, imagePath)
        impl.go(PersonPictureDialogView.VIEW_NAME, args, context)
    }

    fun editProductPicture(saleProductPictureUid: Long){

    }

    fun deleteProductPicture(saleProductPictureUid: Long){
        GlobalScope.launch {
            val pp = productPictureDao.findByUidAsync(saleProductPictureUid)
            if(pp != null){
                pp.saleProductPictureSaleProductUid = 0
                productPictureDao.update(pp)
            }
        }
    }
}
