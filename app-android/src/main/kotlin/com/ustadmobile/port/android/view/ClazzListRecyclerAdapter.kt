package com.ustadmobile.port.android.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzListPresenter
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents

/**
 * The ClazzList Recycler Adapter used here.
 */
class ClazzListRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<ClazzWithNumStudents>,
        internal var theContext: Context,
        private val theFragment: Fragment, private val thePresenter: ClazzListPresenter)
    : PagedListAdapter<ClazzWithNumStudents, ClazzListRecyclerAdapter.ClazzViewHolder>(diffCallback) {
    private var recordAttendanceVisible = false

    class ClazzViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    init {
        recordAttendanceVisible = thePresenter.recordAttendanceVisibility!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzViewHolder {
        val clazzListItem = LayoutInflater.from(theContext).inflate(R.layout.item_clazzlist_clazz,
                parent, false)
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
        val attendancePercentage = (clazz.attendanceAverage * 100).toLong()
        val lastRecordedAttendance = ""
        val clazzTitle: TextView = holder.itemView.findViewById(R.id.item_clazzlist_clazz_title)
        clazzTitle.text = clazz.clazzName


        val numStudentsText = clazz.numStudents.toString() + " " + theFragment.resources
                .getText(R.string.students_literal).toString()
        val subTitle: String
        if (clazz.numTeachers > 0) {
            subTitle = theFragment.resources.getText(R.string.taught_by).toString() + ": " +
                    clazz.teacherNames + " - " + numStudentsText
        } else {
            subTitle = numStudentsText
        }

        //Get lastRecordedAttendance

        var lastRecordedString = theFragment.getText(R.string.not_recorded).toString()
        if (clazz.lastRecorded > 0) {
            lastRecordedString = theFragment.getText(R.string.last_recorded).toString() + " " +
                    UMCalendarUtil.getPrettyDateSimpleFromLong(clazz.lastRecorded, null)
        }

        val attendancePercentageText = (attendancePercentage.toString() + "% " + theFragment.getText(R.string.attendance)
                + " (" + lastRecordedString + ")")
        (holder.itemView.findViewById<View>(R.id.item_clazzlist_numstudents_text) as TextView).text = subTitle
        (holder.itemView.findViewById<View>(R.id.item_clazzlist_attendance_percentage) as TextView).text = attendancePercentageText
        holder.itemView.setOnClickListener { view -> thePresenter.handleClickClazz(clazz) }

//        val recordAttendanceButton = holder.itemView.findViewById<Button>(R.id.item_clazzlist_attendance_record_attendance_button)
//        val recordAttendanceImageButton = holder.itemView.findViewById<ImageButton>(R.id.item_clazzlist_attendance_record_attendance_icon)
//        recordAttendanceButton.setOnClickListener { view -> thePresenter.handleClickClazzRecordAttendance(clazz) }

        //Disabling as per internal issue # 7
//        if (recordAttendanceVisible) {
//            recordAttendanceImageButton.visibility = View.VISIBLE
//            recordAttendanceButton.visibility = View.VISIBLE
//        } else {
//            recordAttendanceImageButton.visibility = View.GONE
//            recordAttendanceButton.visibility = View.GONE
//        }

        val trafficLight = holder.itemView.findViewById<ImageView>(R.id.item_clazzlist_attendance_trafficlight)
        if (attendancePercentage > 75L) {
            trafficLight.setColorFilter(ContextCompat.getColor(theContext, R.color.traffic_green))
        } else if (attendancePercentage > 50L) {
            trafficLight.setColorFilter(ContextCompat.getColor(theContext, R.color.traffic_orange))
        } else {
            trafficLight.setColorFilter(ContextCompat.getColor(theContext, R.color.traffic_red))
        }
    }
}
