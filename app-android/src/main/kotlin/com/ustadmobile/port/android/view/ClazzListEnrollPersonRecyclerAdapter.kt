package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.PersonDetailEnrollClazzPresenter
import com.ustadmobile.lib.db.entities.ClazzWithEnrollment


/**
 * The ClazzList Recycler Adapter used here.
 */
class ClazzListEnrollPersonRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<ClazzWithEnrollment>,
        internal var theContext: Context, private val theActivity: Activity,
        private val thePresenter: PersonDetailEnrollClazzPresenter)
    : PagedListAdapter<ClazzWithEnrollment,
        ClazzListEnrollPersonRecyclerAdapter.ClazzViewHolder>(diffCallback) {

    @SuppressLint("UseSparseArrays")
    private val checkBoxHM = HashMap<Long, Boolean?>()

    class ClazzViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzViewHolder {
        val clazzListItem = LayoutInflater.from(theContext)
                .inflate(R.layout.item_clazz_list_enroll_person, parent, false)
        return ClazzViewHolder(clazzListItem)
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     *
     * @param holder The view holder
     * @param position The position of the item
     */
    override fun onBindViewHolder(holder: ClazzViewHolder, position: Int) {
        val clazz = getItem(position)!!
        val numStudentsText = clazz!!.numStudents.toString() + " " + theActivity.resources
                .getText(R.string.students_literal).toString()
        (holder.itemView.findViewById(R.id.item_clazz_list_enroll_person_title) as TextView)
                .setText(clazz!!.clazzName)
        (holder.itemView.findViewById(R.id.item_clazz_list_enroll_person_numstudents_text)
                as TextView).setText(numStudentsText)

        val checkBox =
                holder.itemView.findViewById<CheckBox>(R.id.item_clazz_list_enroll_person_checkbox)

        //To preserve checkboxes, add this enrollment to the Map.
        checkBoxHM[clazz!!.clazzUid] = clazz!!.enrolled

        checkBox.setChecked(checkBoxHM[clazz!!.clazzUid]!!)

        //Add a change listener to the checkbox
        checkBox.setOnCheckedChangeListener({ buttonView, isChecked -> checkBox.setChecked(isChecked) })

        checkBox.setOnClickListener({ v ->
            val isChecked = checkBox.isChecked()
            thePresenter.handleToggleClazzChecked(clazz!!.clazzUid, clazz!!.personUid,
                    isChecked)
        })


        holder.itemView.setOnClickListener({ view -> thePresenter.handleClickClazz() })

    }
}