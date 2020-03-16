package com.ustadmobile.staging.port.android.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectPeopleDialogPresenter
import com.ustadmobile.lib.db.entities.PersonWithEnrollment
import java.util.*

class PersonListReturnSelectedRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<PersonWithEnrollment>, internal var theContext: Context, private val theFragment: Fragment,
        private val thePresenter: SelectPeopleDialogPresenter)
    : PagedListAdapter<PersonWithEnrollment, PersonListReturnSelectedRecyclerAdapter.PersonViewHolder>(diffCallback) {
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
                .inflate(R.layout.item_clazz_list_enroll_person, parent, false)
        return PersonViewHolder(personListItem)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = getItem(position)!!

        val title = holder.itemView.findViewById<TextView>(R.id.item_clazz_list_enroll_person_title)
        title.text = person.firstNames + " " + person.lastName

        val checkBox = holder.itemView.findViewById<CheckBox>(R.id.item_clazz_list_enroll_person_checkbox)
        checkBox.text = ""

        val descImage = holder.itemView.findViewById<ImageView>(R.id.item_clazz_list_enroll_person_icon)
        descImage.visibility = View.GONE

        checkBox.isChecked = person.enrolled!!

        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                thePresenter.addToPeople(person)
            } else {
                thePresenter.removePeople(person)
            }
        }

    }

}
