package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleProductDetailPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.SaleProductSelected

class SaleProductCategorySelectorRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<SaleProductSelected>,
        internal var mPresenter: SaleProductDetailPresenter,
        private val theContext: Context)
    : PagedListAdapter<SaleProductSelected, SaleProductCategorySelectorRecyclerAdapter.SaleProductDetailViewHolder>(diffCallback) {
    internal var theActivity: Activity? = null

    val impl = UstadMobileSystemImpl.instance

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleProductDetailViewHolder {

        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_category_selector, parent, false)
        return SaleProductDetailViewHolder(list)

    }

    override fun onBindViewHolder(holder: SaleProductDetailViewHolder, position: Int) {

        val saleProductCategory = getItem(position)
        val checkBox = holder.itemView.findViewById<CheckBox>(R.id.item_category_selector_checkbox)

        val currentLocale = impl.getLocale(theContext)
        var saleProductNameLocale: String?=null

        if(currentLocale.equals("fa")){
            saleProductNameLocale = saleProductCategory!!.saleProductNameDari
        }else if(currentLocale.equals("ps")){
            saleProductNameLocale = saleProductCategory!!.saleProductNamePashto
        }else{
            saleProductNameLocale = saleProductCategory!!.saleProductName
        }
        if(saleProductNameLocale == null && saleProductCategory!!.saleProductName != null) {
            saleProductNameLocale = saleProductCategory!!.saleProductName

        }

        checkBox.text = saleProductNameLocale
        checkBox.isChecked = saleProductCategory.isSelected

        checkBox.setOnCheckedChangeListener { buttonView, isChecked -> mPresenter.handleCheckboxChanged(isChecked, saleProductCategory.saleProductUid) }
    }

    inner class SaleProductDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
