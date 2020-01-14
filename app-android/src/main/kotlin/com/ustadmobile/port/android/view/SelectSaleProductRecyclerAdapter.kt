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

import androidx.fragment.app.Fragment
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectSaleProductPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SaleProductPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.SaleProduct
import kotlinx.coroutines.*

import java.io.File

class SelectSaleProductRecyclerAdapter
    : PagedListAdapter<SaleProduct, SelectSaleProductRecyclerAdapter.SelectSaleProductViewHolder> {
    private var theContext: Context
    private var theActivity: Activity? = null
    private var theFragment: Fragment? = null
    internal var mPresenter: SelectSaleProductPresenter

    private var listCategory: Boolean = false
    private var isCatalog: Boolean = false

    private var impl = UstadMobileSystemImpl.instance

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

        var imagePathLocal = ""
        var imagePathServer = ""

        productPictureDaoRepo  =
                UmAccountManager.getRepositoryForActiveAccount(theContext).saleProductPictureDao
        productPictureDao = UmAppDatabase.getInstance(theContext).saleProductPictureDao

        holder.imageLoadJob?.cancel()

        imageView.setImageResource(R.drawable.ic_card_giftcard_black_24dp)
        holder.imageLoadJob = GlobalScope.async(Dispatchers.Main) {

            //Load the local image first
            val saleProductPictureLocal = productPictureDao!!.findBySaleProductUidAsync2(
                    entity!!.saleProductUid)
            imagePathLocal = productPictureDaoRepo!!.getAttachmentPath(saleProductPictureLocal!!)!!;

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
        var saleProductNameLocale: String?=null

        if(currentLocale.equals("fa")){
            saleProductNameLocale = entity!!.saleProductNameDari
        }else if(currentLocale.equals("ps")){
            saleProductNameLocale = entity!!.saleProductNamePashto
        }else{
            saleProductNameLocale = entity!!.saleProductName
        }
        if(saleProductNameLocale == null && entity!!.saleProductName != null) {
            saleProductNameLocale = entity!!.saleProductName

        }
        name.text = saleProductNameLocale


        if (isCatalog) {

            if(entity.saleProductName.equals("Collections")){
                dots.visibility = View.GONE
            }else {
                dots.visibility = View.VISIBLE
            }
            //Options to Edit/Delete every schedule in the list
            dots.setOnClickListener { v: View ->
                if (theActivity != null) {
                    //creating a popup menu
                    val popup = PopupMenu(theActivity!!.applicationContext, v)
                    popup.setOnMenuItemClickListener { item ->
                        val i = item.itemId
                        if (i == R.id.edit) {
                            mPresenter.handleClickProductMulti(entity.saleProductUid, listCategory,
                                    true)
                            true
                        } else if (i == R.id.delete) {
                            mPresenter.handleDelteSaleProduct(entity.saleProductUid, listCategory)
                            true
                        } else {
                            false
                        }
                    }

                    //inflating menu from xml resource
                    popup.inflate(R.menu.menu_edit_delete)
                    popup.menu.findItem(R.id.edit).isVisible = true
                    //If the category is called Collections then hide the delete button
                    if(entity.saleProductName.equals("Collections")){
                        dots.visibility = View.GONE
                    }else {
                        //displaying the popup
                        popup.show()
                    }
                } else if (theFragment != null) {
                    //creating a popup menu
                    val popup = PopupMenu(theFragment!!.context, v)
                    popup.setOnMenuItemClickListener { item ->
                        val i = item.itemId
                        if (i == R.id.edit) {
                            mPresenter.handleClickProductMulti(entity.saleProductUid, listCategory,
                                    true)
                            true
                        } else if (i == R.id.delete) {
                            mPresenter.handleDelteSaleProduct(entity.saleProductUid, listCategory)
                            true
                        } else {
                            false
                        }
                    }

                    //inflating menu from xml resource
                    popup.inflate(R.menu.menu_edit_delete)
                    popup.menu.findItem(R.id.edit).isVisible = true
                    //If the category is called Collections then hide the delete button
                    if(entity.saleProductName.equals("Collections")){
                        dots.visibility = View.GONE
                    }else {
                        //displaying the popup
                        popup.show()
                    }
                }

            }
        } else {
            dots.visibility = View.GONE
        }

        if(mPresenter.isLoggedInPersonAdmin()){
            dots.visibility = View.VISIBLE
        }else{
            dots.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { mPresenter.handleClickProduct(entity.saleProductUid,
                listCategory) }
    }

    inner class SelectSaleProductViewHolder(itemView: View, var imageLoadJob: Job? = null)
        : RecyclerView.ViewHolder(itemView)

    internal constructor(
            diffCallback: DiffUtil.ItemCallback<SaleProduct>,
            thePresenter: SelectSaleProductPresenter,
            activity: Activity,
            isCategory: Boolean?,
            catalog: Boolean?,
            context: Context) : super(diffCallback) {
        mPresenter = thePresenter
        theActivity = activity
        listCategory = isCategory!!
        isCatalog = catalog!!
        theContext = context
    }

    internal constructor(
            diffCallback: DiffUtil.ItemCallback<SaleProduct>,
            thePresenter: SelectSaleProductPresenter,
            fragment: Fragment,
            isCategory: Boolean?,
            catalog: Boolean?,
            context: Context) : super(diffCallback) {
        mPresenter = thePresenter
        theFragment = fragment
        listCategory = isCategory!!
        isCatalog = catalog!!
        theContext = context
    }

    companion object {

        private val IMAGE_WIDTH = 100

        private fun dpToPxImagePerson(): Int {
            return (IMAGE_WIDTH * Resources.getSystem().displayMetrics.density).toInt()
        }
    }


}
