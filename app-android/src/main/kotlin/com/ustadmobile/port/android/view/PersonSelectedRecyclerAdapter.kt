package com.ustadmobile.port.android.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectCustomerPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.Person

class PersonSelectedRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<Person>, internal var theContext: Context,
        private val theFragment: Fragment,
        private val thePresenter: SelectCustomerPresenter)
    : PagedListAdapter<Person, PersonSelectedRecyclerAdapter.PersonViewHolder>(diffCallback) {
    private var selectedPeople: List<Long>? = null

    inner class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    init {

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val personListItem = LayoutInflater.from(theContext)
                .inflate(R.layout.item_peoplelist, parent, false)
        return PersonViewHolder(personListItem)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = getItem(position) ?: return

        val name = holder.itemView.findViewById<TextView>(R.id.item_peoplelist_name)
        name.text = person.fullName(UstadMobileSystemImpl.instance.getLocale(holder.itemView.context))

        val cl = holder.itemView.findViewById<ConstraintLayout>(R.id.item_peoplelist_cl)
        cl.setOnClickListener{
            thePresenter.handleClickCustomer(person.personUid)
        }

    }

}
