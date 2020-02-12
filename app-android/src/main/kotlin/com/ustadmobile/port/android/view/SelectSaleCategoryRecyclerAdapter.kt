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
import android.widget.TextView

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleProductCategoryListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SaleProductPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.lib.db.entities.SaleProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async

import java.io.File

class SelectSaleCategoryRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<SaleProduct>,
        internal var mPresenter: SaleProductCategoryListPresenter,
        private val theActivity: Activity?,
        private val showContextMenu: Boolean?,
        private val listCategory: Boolean?,
        private val theContext: Context)
    : PagedListAdapter<SaleProduct, SelectSaleCategoryRecyclerAdapter.SelectSaleProductViewHolder>(diffCallback) {

    private var productPictureDaoRepo : SaleProductPictureDao?= null
    private var productPictureDao : SaleProductPictureDao? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectSaleProductViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sale_product_blob, parent, false)
        return SelectSaleProductViewHolder(list)

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

    override fun onBindViewHolder(holder: SelectSaleProductViewHolder, position: Int) {

        val entity = getItem(position)
        val imageView = holder.itemView.findViewById<ImageView>(R.id.item_sale_product_blob_image)
        val name = holder.itemView.findViewById<TextView>(R.id.item_sale_product_blob_title)
        val dots = holder.itemView.findViewById<ImageView>(R.id.item_sale_product_blob_dots)

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
            val saleProductPictureLocal = productPictureDao!!.findBySaleProductUidAsync2(
                    entity!!.saleProductUid)
            imagePathLocal = productPictureDaoRepo!!.getAttachmentPath(saleProductPictureLocal!!)!!;

            imageView.setImageResource(R.drawable.ic_card_giftcard_black_24dp)
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

        name.text = entity!!.saleProductName

        //Options to Edit/Delete every schedule in the list
        if (showContextMenu!!) {
            dots.setOnClickListener { v: View ->
                if (theActivity != null) {
                    //creating a popup menu
                    val popup = PopupMenu(theActivity.applicationContext, v)
                    popup.setOnMenuItemClickListener { item ->
                        val i = item.itemId
                        if (i == R.id.edit) {
                            mPresenter.handleClickEditCategory(entity.saleProductUid)
                            true
                        } else if (i == R.id.delete) {
                            mPresenter.handleDeleteCategory(entity.saleProductUid)
                            true
                        } else {
                            false
                        }
                    }

                    //inflating menu from xml resource
                    popup.inflate(R.menu.menu_edit_delete)
                    popup.menu.findItem(R.id.edit).isVisible = true
                    //displaying the popup
                    popup.show()
                }

            }
        } else {
            dots.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { v -> mPresenter.handleClickProduct(entity.saleProductUid, listCategory!!) }

    }

    inner class SelectSaleProductViewHolder(itemView: View, var imageLoadJob: Job? = null) : RecyclerView.ViewHolder(itemView)

    companion object {

        private val IMAGE_WITH = 100

        private fun dpToPxImagePerson(): Int {
            return (IMAGE_WITH * Resources.getSystem().displayMetrics.density).toInt()
        }
    }

}
