package com.ustadmobile.staging.port.android.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectClazzesDialogPresenter
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import java.util.*

class ClazzListReturnSelectedRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<ClazzWithNumStudents>,
        internal var theContext: Context, private val theFragment: Fragment,
        private val thePresenter: SelectClazzesDialogPresenter)
    : PagedListAdapter<ClazzWithNumStudents, ClazzListReturnSelectedRecyclerAdapter.ClazzViewHolder>(diffCallback) {

    private var selectedClazzes: List<Long>? = null

    inner class ClazzViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    init {
        selectedClazzes = thePresenter.selectedClazzesList
        if (selectedClazzes == null) {
            selectedClazzes = ArrayList()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzViewHolder {
        val clazzListItem = LayoutInflater.from(theContext)
                .inflate(R.layout.item_clazz_list_enroll_person, parent, false)
        return ClazzViewHolder(clazzListItem)
    }

    override fun onBindViewHolder(holder: ClazzViewHolder, position: Int) {
        val clazz = getItem(position)!!
        val cl = holder.itemView.findViewById<ConstraintLayout>(R.id
                .item_clazz_list_enroll_person_cl)
        val theCheckbox = holder.itemView.findViewById<CheckBox>(R.id
                .item_clazz_list_enroll_person_checkbox)

        val numStudentsText = clazz!!.numStudents.toString() + " " + theFragment.getResources()
                .getText(R.string.students_literal).toString()
        (holder.itemView.findViewById(R.id.item_clazz_list_enroll_person_title) as TextView)
                .setText(clazz!!.clazzName)
        (holder.itemView.findViewById(R.id.item_clazz_list_enroll_person_numstudents_text) as TextView)
                .setText(numStudentsText)

        theCheckbox.setText("")

        cl.setOnClickListener({ view ->
            theCheckbox.setChecked(!theCheckbox.isChecked())
            theCheckbox.callOnClick()
        })


        theCheckbox.setOnCheckedChangeListener({ buttonView, isChecked ->
            if (isChecked) {
                thePresenter.addToClazzes(clazz!!)
            } else {
                thePresenter.removeFromClazzes(clazz!!)
            }
        })

        if (selectedClazzes!!.contains(clazz!!.clazzUid)) {
            theCheckbox.setChecked(true)
        } else {
            theCheckbox.setChecked(false)
        }

    }

}
