package com.ustadmobile.port.android.view

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CommonInventorySelectionPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.PersonWithInventory
import com.ustadmobile.lib.db.entities.SaleItemListDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File

/**
 * Created by mike on 9/22/17.
 */
class SaleDeliverySelectionView : ConstraintLayout {

    private lateinit var titleTV : TextView
    private lateinit var descTV : TextView
    private lateinit var saleIV : ImageView
    private lateinit var toggleIV : ImageView
    private lateinit var producersLL : LinearLayout
    private var mPresenter : CommonInventorySelectionPresenter<*> ? = null
    private var producers: List<PersonWithInventory> ? = null
    private var showAll = true
    private var item: SaleItemListDetail? = null
    private val impl = UstadMobileSystemImpl.instance

    var newDelivery = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, allProducers: List<PersonWithInventory>,
                saleItemWithExtra : SaleItemListDetail,
                presenter: CommonInventorySelectionPresenter<*>) : super(context) {
        producers = allProducers
        item = saleItemWithExtra
        mPresenter = presenter
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.view_sale_delivery_selection, this)
        titleTV = findViewById(R.id.view_sale_delivery_selection_product_name)
        descTV = findViewById(R.id.view_sale_delivery_selection_desc)
        saleIV = findViewById(R.id.view_sale_delivery_selection_product_image)
        toggleIV = findViewById(R.id.sale_delivery_selection_toggle_imageview)
        producersLL = findViewById(R.id.sale_delivery_selection_procuders_ll)

        if(item != null){

            val saleProductName = item!!.saleItemSaleProduct!!.getNameLocale(impl.getLocale(context))
            titleTV.setText(saleProductName)

            val totalItems = item!!.saleItemQuantity
            val remaining = totalItems - item!!.deliveredCount
            val descText = item!!.deliveredCount.toString() + " " +
                    context.getText(R.string.already_delivered) + ", " +
                    remaining + " " + context.getText(R.string.remaining)
            descTV.setText(descText)

            updateSaleProductPictureOnView()

        }

        if(producers != null) {

            //Also limit selection to remaining
            val remaining = item!!.saleItemQuantity - item!!.deliveredCount

            for (producer in producers!!) {
                val personWithInventorySelection = PersonWithInventorySelectionView(context,
                        producer, mPresenter!!,item!!.saleItemUid, remaining)
                producersLL.addView(personWithInventorySelection)
            }
        }


        //Toggle
        toggleIV.setOnClickListener{
            if(showAll){
                producersLL.visibility = View.GONE
                toggleIV.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
                showAll = false

            }else{
                showAll = true
                producersLL.visibility = View.VISIBLE
                toggleIV.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
            }
        }

    }

    fun updateSaleProductPictureOnView(){
        var imagePathLocal = ""
        var imagePathServer = ""
        val productPictureDaoRepo  =
                UmAccountManager.getRepositoryForActiveAccount(context).saleProductPictureDao
        val productPictureDao = UmAppDatabase.getInstance(context).saleProductPictureDao
        GlobalScope.async(Dispatchers.Main) {

            //Load the local image first
            val saleProductPictureLocal = productPictureDao.findBySaleProductUidAsync2(
                    item!!.saleItemProductUid)
            imagePathLocal = productPictureDaoRepo.getAttachmentPath(saleProductPictureLocal!!)!!;

            if (imagePathLocal.isNotEmpty())
                setPictureOnView(imagePathLocal, saleIV)
            else
                saleIV.setImageResource(R.drawable.ic_card_giftcard_black_24dp)

            //Get the server image
            val saleProductPictureServer =
                    productPictureDaoRepo.findBySaleProductUidAsync2(item!!.saleItemProductUid)
            imagePathServer =
                    productPictureDaoRepo.getAttachmentPath(saleProductPictureServer!!)!!;

            //If local is not server (suggesting picture/entity update)
            if(saleProductPictureLocal != saleProductPictureServer) {

                if (imagePathServer.isNotEmpty())
                    setPictureOnView(imagePathServer, saleIV)
                else
                    saleIV.setImageResource(R.drawable.ic_card_giftcard_black_24dp)
            }else{
                //Do nothing
            }
        }
    }

    private fun setPictureOnView(imagePath: String, theImage: ImageView) {

        val imageUri = Uri.fromFile(File(imagePath))

        Picasso
                .get()
                .load(imageUri)
                .resize(0, dpToPxImagePerson())
                .noFade()
                .into(theImage)
    }

    private fun dpToPxImagePerson(): Int {
        return (IMAGE_PERSON_THUMBNAIL_WIDTH *
                Resources.getSystem().displayMetrics.density).toInt()
    }

    companion object {
        private val IMAGE_PERSON_THUMBNAIL_WIDTH = 48

    }

}
