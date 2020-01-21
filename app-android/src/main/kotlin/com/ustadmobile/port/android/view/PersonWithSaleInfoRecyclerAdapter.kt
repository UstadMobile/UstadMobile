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
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.PersonWithSaleInfoListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import java.io.File


class PersonWithSaleInfoRecyclerAdapter : PagedListAdapter<PersonWithSaleInfo,
        PersonWithSaleInfoRecyclerAdapter.PersonWithSaleInfoViewHolder>{

    internal var theContext: Context
    internal var theActivity: Activity? = null
    internal var theFragment: Fragment ?= null
    internal var mPresenter: PersonWithSaleInfoListPresenter
    internal var paymentsDueTab = false
    internal var preOrderTab = false
    private var personPictureDao: PersonPictureDao?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonWithSaleInfoViewHolder {

        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_personwithsaleinfo, parent, false)
        return PersonWithSaleInfoViewHolder(list)

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

    override fun onBindViewHolder(holder: PersonWithSaleInfoViewHolder, position: Int) {

        val entity = getItem(position)

        val personTitle = holder.itemView.findViewById<TextView>(R.id.item_person_name)
        val saleAmount = holder.itemView.findViewById<TextView>(R.id.item_person_sale_amount)
        val topProducts = holder.itemView.findViewById<TextView>(R.id.item_person_top_products)
        val personPicture = holder.itemView.findViewById<AppCompatImageView>(R.id.item_person_picture)

        val personName= entity!!.firstNames + " " + entity.lastName
        val totalSale = entity.totalSale
        var topP = entity.topProducts
        if(entity.topProducts == null){
            topP = "-"
        }
        personTitle.setText(personName)
        saleAmount.setText(totalSale.toString() + " " + theActivity!!.getText(R.string.currency_afs))
        val topProductsBit = UstadMobileSystemImpl.instance.getString(MessageID.top_products,
                theContext) + ": " + topP
        topProducts.setText(topProductsBit)


        holder.imageLoadJob?.cancel()

        holder.imageLoadJob = GlobalScope.async(Dispatchers.Main) {

            personPictureDao = UmAccountManager.getRepositoryForActiveAccount(theContext).personPictureDao

            val personPictureEntity = personPictureDao!!.findByPersonUidAsync(entity!!.personUid)

            val imgPath = personPictureDao!!.getAttachmentPath(personPictureEntity!!)

            if (!imgPath!!.isEmpty())
                setPictureOnView(imgPath, personPicture)
            else
                personPicture.setImageResource(R.drawable.ic_account_circle_white_36dp)
        }

        val cl = holder.itemView.findViewById<ConstraintLayout>(R.id.item_person_cl)
        cl.setOnClickListener { mPresenter.handleClickWE(entity.personUid) }
    }

    inner class PersonWithSaleInfoViewHolder(itemView: View, var imageLoadJob: Job? = null) : RecyclerView.ViewHolder(itemView)


    internal constructor(
            diffCallback: DiffUtil.ItemCallback<PersonWithSaleInfo>,
            thePresenter: PersonWithSaleInfoListPresenter,
            paymentsDue: Boolean,
            preOrder: Boolean,
            fragment: Fragment,
            context: Context) : super(diffCallback) {
        mPresenter = thePresenter
        theContext = context
        paymentsDueTab = paymentsDue
        this.theFragment = fragment
        preOrderTab = preOrder
        this.theFragment = fragment
    }

    internal constructor(
            diffCallback: DiffUtil.ItemCallback<PersonWithSaleInfo>,
            thePresenter: PersonWithSaleInfoListPresenter,
            paymentsDue: Boolean,
            preOrder: Boolean,
            activity: Activity,
            context: Context) : super(diffCallback) {
        mPresenter = thePresenter
        theContext = context
        paymentsDueTab = paymentsDue
        this.theActivity = activity
        preOrderTab = preOrder
    }

    companion object{
        private fun dpToPxImagePerson(): Int {
            return (SaleItemRecyclerAdapter.IMAGE_WITH * Resources.getSystem().displayMetrics.density).toInt()
        }
    }

}