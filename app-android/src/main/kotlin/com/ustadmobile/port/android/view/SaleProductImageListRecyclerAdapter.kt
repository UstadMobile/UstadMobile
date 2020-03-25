package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleProductImageListPresenter
import com.ustadmobile.core.db.dao.SaleProductPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.lib.db.entities.SaleProductPicture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import java.io.File

class SaleProductImageListRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<SaleProductPicture>,
        internal var mPresenter: SaleProductImageListPresenter,
        private val theActivity: Activity?,
        private val theContext: Context)
    : PagedListAdapter<SaleProductPicture,
        SaleProductImageListRecyclerAdapter.SelectSaleProductPictureViewHolder>(diffCallback) {

    private lateinit var productPictureDaoRepo : SaleProductPictureDao
    private lateinit var productPictureDao : SaleProductPictureDao

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectSaleProductPictureViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sale_product_picture, parent, false)
        return SelectSaleProductPictureViewHolder(list)

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

    override fun onBindViewHolder(holder: SelectSaleProductPictureViewHolder, position: Int) {

        val entity = getItem(position)
        val imageView = holder.itemView.findViewById<ImageView>(R.id.item_sale_product_picture_image)
        val dots = holder.itemView.findViewById<ImageView>(R.id.item_sale_product_picture_dots)
        val hamburger = holder.itemView.findViewById<ImageView>(R.id.item_sale_product_picture_hamburger)

        assert(entity != null)


        var imagePathLocal = ""
        var imagePathServer = ""

        productPictureDaoRepo  =
                UmAccountManager.getRepositoryForActiveAccount(theContext).saleProductPictureDao
        productPictureDao = UmAccountManager.getActiveDatabase(theContext).saleProductPictureDao

        holder.imageLoadJob?.cancel()

        imageView.setImageResource(R.drawable.ic_card_giftcard_black_24dp)
        holder.imageLoadJob = GlobalScope.async(Dispatchers.Main) {

            //Load the local image first
            val saleProductPictureLocal = productPictureDao.findByUidAsync(entity!!.saleProductPictureUid)
            imagePathLocal = productPictureDaoRepo.getAttachmentPath(saleProductPictureLocal!!)!!;

            imageView.setImageResource(R.drawable.ic_card_giftcard_black_24dp)
            if (imagePathLocal.isNotEmpty())
                setPictureOnView(imagePathLocal, imageView)
            else
                imageView.setImageResource(R.drawable.ic_card_giftcard_black_24dp)

            //Get the server image
            val saleProductPictureServer =
                    productPictureDaoRepo.findByUidAsync(entity!!.saleProductPictureUid)
            imagePathServer =
                    productPictureDaoRepo.getAttachmentPath(saleProductPictureServer!!)!!;

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

        dots.setOnClickListener { v: View ->
            if (theActivity != null) {
                //creating a popup menu
                val popup = PopupMenu(theActivity.applicationContext, v)
                popup.setOnMenuItemClickListener { item ->
                    val i = item.itemId
                    if (i == R.id.edit) {
                        mPresenter.editProductPicture(entity!!.saleProductPictureUid)
                        true
                    } else if (i == R.id.delete) {
                        mPresenter.deleteProductPicture(entity!!.saleProductPictureUid)
                        true
                    } else {
                        false
                    }
                }

                //inflating menu from xml resource
                popup.inflate(R.menu.menu_edit_delete)
                popup.menu.findItem(R.id.edit).isVisible = false
                //displaying the popup
                popup.show()
            }

        }

        holder.itemView.setOnClickListener { v ->
            mPresenter.openPictureDialog(imagePathLocal) }

    }

    fun moveItem(from: Int, to: Int) {
        println("debudebu: move from: $from to $to")
    }

    fun movedItem(from: Int, to: Int) {
        println("debudebu: Moved from: $from to $to")
    }

    inner class SelectSaleProductPictureViewHolder(itemView: View, var imageLoadJob: Job? = null)
        : RecyclerView.ViewHolder(itemView)

    companion object {

        private const val IMAGE_WITH = 100

        private fun dpToPxImagePerson(): Int {
            return (IMAGE_WITH * Resources.getSystem().displayMetrics.density).toInt()
        }
    }

}
