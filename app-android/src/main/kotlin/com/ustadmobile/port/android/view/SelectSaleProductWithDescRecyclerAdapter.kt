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
import com.ustadmobile.core.controller.SaleProductCategoryListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.SaleNameWithImage

import java.io.File

class SelectSaleProductWithDescRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<SaleNameWithImage>,
        internal var mPresenter: SaleProductCategoryListPresenter,
        internal var theActivity: Activity,
        private val listCategory: Boolean,
        internal var theContext: Context)
    : PagedListAdapter<SaleNameWithImage, SelectSaleProductWithDescRecyclerAdapter.SelectSaleProductViewHolder>(diffCallback) {

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
                .resize(dpToPxImagePerson(), dpToPxImagePerson())
                .noFade()
                .into(theImage)
    }

    override fun onBindViewHolder(holder: SelectSaleProductViewHolder, position: Int) {

        val entity = getItem(position)
        val imageView = holder.itemView.findViewById<ImageView>(R.id.item_sale_item_image)
        val name = holder.itemView.findViewById<TextView>(R.id.item_sale_product_title)
        val desc = holder.itemView.findViewById<TextView>(R.id.item_sale_product_desc)

        val pictureUid = entity!!.pictureUid
        var imagePath = ""
        if (pictureUid != 0L) {
//            imagePath = UmAppDatabase.Companion.getInstance(theContext).saleProductPictureDao
//                            .getAttachmentPath(entity);
        }

        if (imagePath != null && !imagePath.isEmpty())
            setPictureOnView(imagePath, imageView)
        else
            imageView.setImageResource(R.drawable.ic_card_giftcard_black_24dp)

        name.text = entity.name
        desc.text = entity.description

        holder.itemView.setOnClickListener { v -> mPresenter.handleClickProduct(entity.productUid, listCategory) }

    }

    inner class SelectSaleProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {

        private val IMAGE_WITH = 26

        private fun dpToPxImagePerson(): Int {
            return (IMAGE_WITH * Resources.getSystem().displayMetrics.density).toInt()
        }
    }


}
