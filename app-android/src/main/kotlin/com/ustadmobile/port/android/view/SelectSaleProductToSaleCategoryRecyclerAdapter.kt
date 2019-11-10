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
import com.ustadmobile.core.controller.AddSaleProductToSaleCategoryPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SaleProductPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.lib.db.entities.SaleProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async

import java.io.File

class SelectSaleProductToSaleCategoryRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<SaleProduct>,
        internal var mPresenter: AddSaleProductToSaleCategoryPresenter,
        internal var theActivity: Activity,
        private val listCategory: Boolean,
        internal var theContext: Context)
    : PagedListAdapter<SaleProduct, SelectSaleProductToSaleCategoryRecyclerAdapter.SelectSaleProductViewHolder>(diffCallback) {

    private var productPictureDaoRepo : SaleProductPictureDao? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectSaleProductViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sale_product, parent, false)
        return SelectSaleProductViewHolder(list)

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

    override fun onBindViewHolder(holder: SelectSaleProductViewHolder, position: Int) {

        val entity = getItem(position)
        val imageView = holder.itemView.findViewById<ImageView>(R.id.item_sale_item_image)
        val name = holder.itemView.findViewById<TextView>(R.id.item_sale_product_title)
        val desc = holder.itemView.findViewById<TextView>(R.id.item_sale_product_desc)
        desc.visibility = View.GONE



        var imagePath = ""

        holder.imageLoadJob?.cancel()

        holder.imageLoadJob = GlobalScope.async(Dispatchers.Main) {

            productPictureDaoRepo  = UmAccountManager.getRepositoryForActiveAccount(theContext).saleProductPictureDao
            val productPictureDao = UmAppDatabase.getInstance(theContext).saleProductPictureDao


            val saleProductPictureLocal = productPictureDao!!.findBySaleProductUidAsync2(entity!!.saleProductUid)
            imagePath = productPictureDaoRepo!!.getAttachmentPath(saleProductPictureLocal!!)!!;

            if (!imagePath.isEmpty())
                setPictureOnView(imagePath, imageView)
            else
                imageView.setImageResource(R.drawable.ic_card_giftcard_black_24dp)


            val saleProductPicture = productPictureDaoRepo!!.findBySaleProductUidAsync2(entity!!.saleProductUid)
            imagePath = productPictureDaoRepo!!.getAttachmentPath(saleProductPicture!!)!!;

            if(saleProductPictureLocal != saleProductPicture) {

                if (!imagePath.isEmpty())
                    setPictureOnView(imagePath, imageView)
                else
                    imageView.setImageResource(R.drawable.ic_card_giftcard_black_24dp)
            }
        }



        name.text = entity!!.saleProductName
        desc.text = entity!!.saleProductDesc

        holder.itemView.setOnClickListener { v -> mPresenter.handleClickProduct(entity!!.saleProductUid) }

    }

    inner class SelectSaleProductViewHolder(itemView: View, var imageLoadJob: Job? = null) : RecyclerView.ViewHolder(itemView)

    companion object {

        private val IMAGE_WITH = 100

        private fun dpToPxImagePerson(): Int {
            return (IMAGE_WITH * Resources.getSystem().displayMetrics.density).toInt()
        }
    }


}
