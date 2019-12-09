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

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleDetailPresenter
import com.ustadmobile.core.db.dao.SaleProductPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.lib.db.entities.SaleItemListDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async

import java.io.File

class SaleItemRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<SaleItemListDetail>,
        internal var mPresenter: SaleDetailPresenter,
        internal var theActivity: Activity,
        internal var theContext: Context) : PagedListAdapter<SaleItemListDetail, SaleItemRecyclerAdapter.SaleDetailViewHolder>(diffCallback) {

    private var productPictureDao : SaleProductPictureDao? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleDetailViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sale_item, parent, false)
        return SaleDetailViewHolder(list)

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

    override fun onBindViewHolder(holder: SaleDetailViewHolder, position: Int) {

        val entity = getItem(position)

        val pictureUid = entity!!.saleItemPictureUid
        val imageView = holder.itemView.findViewById<ImageView>(R.id.item_sale_item_image)
        var imagePath = ""

        holder.imageLoadJob?.cancel()

        holder.imageLoadJob = GlobalScope.async(Dispatchers.Main) {

            productPictureDao  = UmAccountManager.getRepositoryForActiveAccount(theContext).saleProductPictureDao

            val saleProductPicture = productPictureDao!!.findBySaleProductUidAsync2(entity.saleItemProductUid)
            imagePath = productPictureDao!!.getAttachmentPath(saleProductPicture!!)!!;

            if (!imagePath.isEmpty())
                setPictureOnView(imagePath, imageView)
            else
                imageView.setImageResource(R.drawable.ic_card_giftcard_black_24dp)
        }

//        if (pictureUid != 0L) {
//            //TODO: Fix attachment stuff KMP
//            //            imagePath = UmAppDatabase.Companion.getInstance(theContext).getSaleProductPictureDao()
//            //                    .getAttachmentPath(pictureUid);
//        }
//
//        if (imagePath != null && !imagePath.isEmpty())
//            setPictureOnView(imagePath, imageView)
//        else
//            imageView.setImageResource(R.drawable.ic_card_giftcard_black_24dp)

        val itemName = holder.itemView.findViewById<TextView>(R.id.item_sale_item_name)
        val itemQuantity = holder.itemView.findViewById<TextView>(R.id.item_sale_item_quantity)
        val itemPrice = holder.itemView.findViewById<TextView>(R.id.item_sale_item_price)
        val itemTotal = holder.itemView.findViewById<TextView>(R.id.item_sale_item_total)
        val dueDate = holder.itemView.findViewById<TextView>(R.id.item_sale_item_due_date)
        val dueDateImage = holder.itemView.findViewById<ImageView>(R.id.item_sale_item_warning_image)

        val itemWhole = holder.itemView.findViewById<ConstraintLayout>(R.id.item_sale_item_cl)

        dueDate.visibility = if (entity.saleItemPreorder) View.VISIBLE else View.INVISIBLE
        dueDateImage.visibility = if (entity.saleItemPreorder) View.VISIBLE else View.INVISIBLE

        val quantity = entity.saleItemQuantity
        val price = entity.saleItemPricePerPiece

        val priceString = Math.round(price).toString() + " " + theActivity.getString(R.string.currency_afs)
        val priceTotalString = Math.round(quantity * price).toString() + " " + theActivity.getString(R.string.currency_afs)
        val dueString = theActivity.getString(R.string.due) + " " +
                UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                        entity.saleItemDueDate)


        val impl = UstadMobileSystemImpl.instance
        val currentLocale = impl.getLocale(theContext)
        var saleProductNameLocale: String?=null

        if(entity.saleItemSaleProduct != null) {
            if (currentLocale.equals("fa")) {
                saleProductNameLocale = entity.saleItemSaleProduct!!.saleProductNameDari
            } else if (currentLocale.equals("ps")) {
                saleProductNameLocale = entity.saleItemSaleProduct!!.saleProductNamePashto
            } else {
                saleProductNameLocale = entity.saleItemSaleProduct!!.saleProductName
            }
            if (saleProductNameLocale == null && entity.saleItemSaleProduct!!.saleProductName != null) {
                saleProductNameLocale = entity.saleItemSaleProduct!!.saleProductName
            }
            itemName.text = saleProductNameLocale
        }else{
            itemName.text = entity.saleItemProductName
        }


        itemQuantity.text = quantity.toString()
        itemPrice.text = priceString
        itemTotal.text = priceTotalString

        dueDate.text = dueString

        itemWhole.setOnClickListener { v: View -> mPresenter.handleClickSaleItemEdit(entity.saleItemUid) }


    }

    inner class SaleDetailViewHolder(itemView: View , var imageLoadJob: Job? = null) : RecyclerView.ViewHolder(itemView)

    companion object {
        val IMAGE_WITH = 52

        private fun dpToPxImagePerson(): Int {
            return (IMAGE_WITH * Resources.getSystem().displayMetrics.density).toInt()
        }
    }


}
