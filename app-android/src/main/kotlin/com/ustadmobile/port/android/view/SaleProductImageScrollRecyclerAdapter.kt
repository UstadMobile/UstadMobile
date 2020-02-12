package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleProductImageListPresenter
import com.ustadmobile.core.controller.SaleProductShowcasePresenter
import com.ustadmobile.core.db.dao.SaleProductPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.lib.db.entities.SaleProductPicture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import java.io.File

class SaleProductImageScrollRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<SaleProductPicture>,
        internal var mPresenter: SaleProductShowcasePresenter,
        private val theContext: Context)
    : PagedListAdapter<SaleProductPicture,
        SaleProductImageScrollRecyclerAdapter.SelectSaleProductPictureViewHolder>(diffCallback) {

    private lateinit var productPictureDaoRepo : SaleProductPictureDao
    private lateinit var productPictureDao : SaleProductPictureDao

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectSaleProductPictureViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_saleproductimagescroll, parent, false)
        return SelectSaleProductPictureViewHolder(list)

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

    override fun onBindViewHolder(holder: SelectSaleProductPictureViewHolder, position: Int) {

        val entity = getItem(position)
        val imageView = holder.itemView.findViewById<ImageView>(R.id.item_saleproductimagescroll)


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
                    productPictureDaoRepo.findByUidAsync(entity.saleProductPictureUid)
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


        holder.itemView.setOnClickListener { v ->
            mPresenter.openPictureDialog(imagePathLocal) }

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
