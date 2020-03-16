package com.ustadmobile.staging.port.android.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzLogDetailPresenter
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson

import java.util.HashMap

import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ABSENT
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ATTENDED
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_PARTIAL

/**
 * The Log Detail (Attendance) Recycler Adapter
 */
class ClazzLogDetailRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<ClazzLogAttendanceRecordWithPerson>,
        internal var theContext: Context, private val theActivity: Activity,
        private val thePresenter: ClazzLogDetailPresenter)
    : PagedListAdapter<ClazzLogAttendanceRecordWithPerson, ClazzLogDetailRecyclerAdapter.ClazzLogDetailViewHolder>(diffCallback) {

    class ClazzLogDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzLogDetailViewHolder {

        val clazzLogDetailListItem = LayoutInflater.from(theContext).inflate(
                R.layout.item_clazzlog_detail_student, parent, false)
        return ClazzLogDetailViewHolder(clazzLogDetailListItem)
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     *
     * Every item in the recycler view will have set its colors if no attendance status is set.
     * every attendance button will have it-self mapped to tints on activation.
     *
     */
    override fun onBindViewHolder(holder: ClazzLogDetailViewHolder, position: Int) {
        val attendanceRecord = getItem(position)!!

        val studentName = attendanceRecord.person!!.firstNames + " " +
                attendanceRecord.person!!.lastName

        holder.itemView.tag = attendanceRecord.clazzLogAttendanceRecordUid

        (holder.itemView
                .findViewById<View>(R.id.item_clazzlog_detail_student_name) as TextView).text = studentName
        (holder.itemView
                .findViewById<View>(R.id.item_clazzlog_detail_student_present_icon) as ImageView)
                .setColorFilter(Color.BLACK)

        val clazzLogAttendanceRecordUid = attendanceRecord.clazzLogAttendanceRecordUid

        @SuppressLint("UseSparseArrays") val attendanceButtons = HashMap<Int, ImageView>()
        attendanceButtons[STATUS_ATTENDED] = holder.itemView.findViewById(
                R.id.item_clazzlog_detail_student_present_icon)
        attendanceButtons[STATUS_ABSENT] = holder.itemView.findViewById(
                R.id.item_clazzlog_detail_student_absent_icon)
        attendanceButtons[STATUS_PARTIAL] = holder.itemView.findViewById(
                R.id.item_clazzlog_detail_student_delay_icon)

        //Loop through every attendance button
        for ((key, value) in attendanceButtons) {
            val selectedOption = attendanceRecord.attendanceStatus == key

            if (thePresenter.isHasEditPermissions) {
                value.setOnClickListener { view ->
                    thePresenter.handleMarkStudent(
                            clazzLogAttendanceRecordUid, key)
                }
            }

            value.setColorFilter(
                    if (selectedOption)
                        ContextCompat.getColor(theContext,
                                STATUS_TO_COLOR_MAP[key]!!)
                    else
                        ContextCompat.getColor(
                                theContext, R.color.color_gray))

            val status_tag = theActivity.resources.getString(
                    STATUS_TO_STRING_ID_MAP[key]!!) + " " +
                    if (selectedOption)
                        SELECTED_STATUS_TO_STATUS_TAG[key]
                    else
                        UNSELECTED_STATUS_TO_STATUS_TAG[key]
            value.tag = status_tag
            //Set any content description here.
        }

    }

    companion object {

        //static map matching attendance status code value with color tint
        @SuppressLint("UseSparseArrays")
        private val STATUS_TO_COLOR_MAP = HashMap<Int, Int>()

        //static map matching attendance status code value with
        @SuppressLint("UseSparseArrays")
        private val STATUS_TO_STRING_ID_MAP = HashMap<Int, Int>()

        @SuppressLint("UseSparseArrays")
        private val SELECTED_STATUS_TO_STATUS_TAG = HashMap<Int, Int>()

        @SuppressLint("UseSparseArrays")
        private val UNSELECTED_STATUS_TO_STATUS_TAG = HashMap<Int, Int>()

        init {
            STATUS_TO_COLOR_MAP[STATUS_ATTENDED] = R.color.traffic_green
            STATUS_TO_COLOR_MAP[STATUS_ABSENT] = R.color.traffic_red
            STATUS_TO_COLOR_MAP[STATUS_PARTIAL] = R.color.traffic_orange

            STATUS_TO_STRING_ID_MAP[STATUS_ATTENDED] = R.string.attendance
            STATUS_TO_STRING_ID_MAP[STATUS_ABSENT] = R.string.attendance
            STATUS_TO_STRING_ID_MAP[STATUS_PARTIAL] = R.string.attendance

            SELECTED_STATUS_TO_STATUS_TAG[STATUS_ATTENDED] = R.string.present_selected
            SELECTED_STATUS_TO_STATUS_TAG[STATUS_ABSENT] = R.string.absent_selected
            SELECTED_STATUS_TO_STATUS_TAG[STATUS_PARTIAL] = R.string.partial_selected

            UNSELECTED_STATUS_TO_STATUS_TAG[STATUS_ATTENDED] = R.string.present_unselected
            UNSELECTED_STATUS_TO_STATUS_TAG[STATUS_ABSENT] = R.string.absent_unselected
            UNSELECTED_STATUS_TO_STATUS_TAG[STATUS_PARTIAL] = R.string.partial_unselected

        }
    }
}
