package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.InventoryListPresenter
import com.ustadmobile.core.db.dao.SaleProductPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.SaleProductWithInventoryCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import java.io.File

class InventoryListRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<SaleProductWithInventoryCount>,
        internal var mPresenter: InventoryListPresenter,
        internal var theActivity: Activity,
        internal var theContext: Context)
    : PagedListAdapter<SaleProductWithInventoryCount,
        InventoryListRecyclerAdapter.InventoryListViewHolder>(diffCallback) {

    private var productPictureDaoRepo : SaleProductPictureDao?= null
    private var productPictureDao : SaleProductPictureDao? = null
    private var impl = UstadMobileSystemImpl.instance

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryListViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sale_product, parent, false)
        return InventoryListViewHolder(list)

    }

    private fun setPictureOnView(imagePath: String, theImage: ImageView) {

        val imageUri = Uri.fromFile(File(imagePath))

        Picasso
                .get()
                .load(imageUri)
                //.resize(0, dpToPxImagePerson())
                .fit()
                .centerCrop()
                .noFade()
                .into(theImage)
    }

    override fun onBindViewHolder(holder: InventoryListViewHolder, position: Int) {

        val entity = getItem(position)
        val imageView = holder.itemView.findViewById<ImageView>(R.id.item_sale_item_image)
        val name = holder.itemView.findViewById<TextView>(R.id.item_sale_product_title)
        val desc = holder.itemView.findViewById<TextView>(R.id.item_sale_product_desc)

        var imagePathLocal = ""
        var imagePathServer = ""

        productPictureDaoRepo  =
                UmAccountManager.getRepositoryForActiveAccount(theContext).saleProductPictureDao
        productPictureDao = UmAccountManager.getActiveDatabase(theContext).saleProductPictureDao

        holder.imageLoadJob?.cancel()

        holder.imageLoadJob = GlobalScope.async(Dispatchers.Main) {

            //Load the local image first
            val saleProductPictureLocal = productPictureDao!!.findBySaleProductUidAsync2(
                    entity!!.saleProductUid)
            imagePathLocal =
                    productPictureDaoRepo!!.getAttachmentPath(saleProductPictureLocal!!)!!;

            if (imagePathLocal.isNotEmpty())
                setPictureOnView(imagePathLocal, imageView)
            else
                imageView.setImageResource(R.drawable.ic_card_giftcard_black_24dp)

            //Get the server image
            val saleProductPictureServer =
                    productPictureDaoRepo!!.findBySaleProductUidAsync2(entity.saleProductUid)
            imagePathServer =
                    productPictureDaoRepo!!.getAttachmentPath(saleProductPictureServer!!)!!;

            //If local is not server (suggesting picture/entity update)
            if(saleProductPictureLocal != saleProductPictureServer) {

                if (imagePathServer.isNotEmpty())
                    setPictureOnView(imagePathServer, imageView)
                else
                    imageView.setImageResource(R.drawable.ic_card_giftcard_black_24dp)
            }else{
                //Do nothing
            }
        }

        val currentLocale = impl.getLocale(theContext)
        name.text = entity!!.getNameLocale(currentLocale)

        var descWithCount = ""

        if(entity.stock > 1) {
            descWithCount = entity.stock.toString() + " " +
                    theActivity.getText(R.string.items_in_stock)
        }else {
            descWithCount = entity.stock.toString() + " " +
                    theActivity.getText(R.string.item_in_stock)
        }

        desc.text = descWithCount

        holder.itemView.setOnClickListener {
            v -> mPresenter.handleClickSaleProductInventory(entity.saleProductUid)
        }

    }

    inner class InventoryListViewHolder(itemView: View, var imageLoadJob: Job? = null)
        : RecyclerView.ViewHolder(itemView)

    companion object {

        private val IMAGE_WITH = 100

        private fun dpToPxImagePerson(): Int {
            return (IMAGE_WITH * Resources.getSystem().displayMetrics.density).toInt()
        }
    }


}
