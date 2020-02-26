package com.ustadmobile.port.android.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView

import androidx.fragment.app.Fragment
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectMultiplePeoplePresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.Person

import java.util.ArrayList

class PersonListReturnSelectedSimpleRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<Person>,
        internal var theContext: Context,
        private val theFragment: Fragment,
        private val thePresenter: SelectMultiplePeoplePresenter)
    : PagedListAdapter<Person, PersonListReturnSelectedSimpleRecyclerAdapter.PersonViewHolder>(diffCallback) {

    private var selectedPeople: List<Long>? = null

    inner class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    init {
        selectedPeople = thePresenter.selectedPeopleList
        if (selectedPeople == null) {
            selectedPeople = ArrayList()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val personListItem = LayoutInflater.from(theContext)
                .inflate(R.layout.item_person_checked, parent, false)
        return PersonViewHolder(personListItem)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = getItem(position)!!

        val title = holder.itemView.findViewById<TextView>(R.id.item_clazz_list_enroll_person_title)
        title.text = person.fullName(UstadMobileSystemImpl.instance.getLocale(holder.itemView.context))

        val checkBox = holder.itemView.findViewById<CheckBox>(R.id.item_clazz_list_enroll_person_checkbox)
        checkBox.text = ""

        //checkBox.setChecked(???);
        if (selectedPeople!!.contains(person.personUid)) {
            checkBox.isChecked = true
        } else {
            checkBox.isChecked = false
        }

        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                thePresenter.addToPeople(person)
            } else {
                thePresenter.removePeople(person)
            }
        }

    }

}
