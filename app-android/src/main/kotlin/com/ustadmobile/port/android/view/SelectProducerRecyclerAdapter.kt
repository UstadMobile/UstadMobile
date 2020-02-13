package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectProducerPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.Person

class SelectProducerRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<Person>,
        internal var mPresenter: SelectProducerPresenter,
        internal var theActivity: Activity,
        internal var theContext: Context) : PagedListAdapter<Person, SelectProducerRecyclerAdapter.SelectProducerViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectProducerViewHolder {


        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_simple, parent, false)
        return SelectProducerViewHolder(list)

    }

    override fun onBindViewHolder(holder: SelectProducerViewHolder, position: Int) {

        val entity = getItem(position)
        if (entity != null) {
            val title = holder.itemView.findViewById<TextView>(R.id.item_title_simple_title)
            title.text = entity.fullName(UstadMobileSystemImpl.instance.getLocale(theContext))

            title.setOnClickListener { v -> mPresenter.handleClickProducer(entity.personUid) }
        }
    }

    inner class SelectProducerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
