package com.ustadmobile.staging.port.android.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzLogListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.lib.db.entities.ClazzLogWithScheduleStartEndTimes
import com.ustadmobile.lib.db.entities.Schedule
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * The ClazzLogList's recycler adapter.
 */
class ClazzLogListRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<ClazzLogWithScheduleStartEndTimes>,
        internal var theContext: Context, private val theFragment: Fragment,
        private val thePresenter: ClazzLogListPresenter,
       private val showImage: Boolean?)
    : PagedListAdapter<ClazzLogWithScheduleStartEndTimes, ClazzLogListRecyclerAdapter.ClazzLogViewHolder>(diffCallback) {

    class ClazzLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzLogViewHolder {
        val clazzLogListItem = LayoutInflater.from(theContext).inflate(
                R.layout.item_clazzlog_log, parent, false)
        return ClazzLogViewHolder(clazzLogListItem)
    }

    /**
     *
     * Gets the appropriate string of schedule frequency and returns MessageID code for applicable
     * string.
     * @param frequency The Schedule freqency (from Schedule entity's scheduleFrequency field)
     * @return  MessageID code for applicable string.
     */
    private fun frequencyToMessageID(frequency: Int): Int {
        var frequencyId = 0
        when (frequency) {
            Schedule.SCHEDULE_FREQUENCY_DAILY -> frequencyId = MessageID.daily
            Schedule.SCHEDULE_FREQUENCY_WEEKLY -> frequencyId = MessageID.weekly
            Schedule.SCHEDULE_FREQUENCY_ONCE -> frequencyId = MessageID.once
            Schedule.SCHEDULE_FREQUENCY_MONTHLY -> frequencyId = MessageID.monthly
            Schedule.SCHEDULE_FREQUENCY_YEARLY -> frequencyId = MessageID.yearly
            else -> {
            }
        }
        return frequencyId
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     *
     * For every item part of the recycler adapter, this will be called and every item in it
     * will be set as per this function.
     *
     */
    override fun onBindViewHolder(holder: ClazzLogViewHolder, position: Int) {
        val clazzLog = getItem(position)!!

        val currentLocale = theFragment.resources.configuration.locale
        val impl = UstadMobileSystemImpl.instance

        var prettyDate = UMCalendarUtil.getPrettyDateFromLong(clazzLog.logDate, currentLocale)

        //Get frequency of schedule

        val frequencyName = impl.getString(frequencyToMessageID(clazzLog.scheduleFrequency),
                theContext)

        //Add time to ClazzLog's date
        val startTimeLong = clazzLog.sceduleStartTime
        val endTimeLong = clazzLog.scheduleEndTime
        val formatter = SimpleDateFormat.getTimeInstance(DateFormat.SHORT)

        //start time
        val startMins = startTimeLong / (1000 * 60)
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, (startMins / 60).toInt())
        cal.set(Calendar.MINUTE, (startMins % 60).toInt())
        val startTime = formatter.format(cal.time)

        //end time
        val endMins = endTimeLong / (1000 * 60)
        cal.set(Calendar.HOUR_OF_DAY, (endMins / 60).toInt())
        cal.set(Calendar.MINUTE, (endMins % 60).toInt())
        val endTime = formatter.format(cal.time)

        prettyDate = "$prettyDate ($frequencyName, $startTime - $endTime)"

        val prettyShortDay = UMCalendarUtil.getSimpleDayFromLongDate(clazzLog.logDate, currentLocale)

        val secondaryTextImageView = holder.itemView.findViewById<ImageView>(R.id.item_clazzlog_log_status_text_imageview)

        val presentCount = clazzLog.clazzLogNumPresent
        val absentCount = clazzLog.clazzLogNumAbsent
        val partialCount = clazzLog.clazzLogNumPartial
        val clazzLogAttendanceStatus: String
        if (partialCount > 0) {
            clazzLogAttendanceStatus = presentCount.toString() + " " +
                    theFragment.getText(R.string.present) + ", " + absentCount + " " +
                    theFragment.getText(R.string.absent) + ", " + partialCount + " " +
                    theFragment.getText(R.string.partial)
        } else {
            clazzLogAttendanceStatus = presentCount.toString() + " " +
                    theFragment.getText(R.string.present) + ", " + absentCount + " " +
                    theFragment.getText(R.string.absent)
        }

        val statusTextView = holder.itemView.findViewById<TextView>(R.id.item_clazzlog_log_status_text)

        val doneIV = holder.itemView.findViewById<AppCompatImageView>(R.id.item_clazzlog_log_done_image)

        if (clazzLog.clazzLogDone) {
            //Update doneIV to tick
            doneIV.setImageResource(R.drawable.ic_check_black_24dp)
        } else {
            //Update doneIV to pencil
            doneIV.setImageResource(R.drawable.ic_edit_gray)

        }
        (holder.itemView.findViewById<View>(R.id.item_clazzlog_log_date) as TextView).text = prettyDate
        (holder.itemView.findViewById<View>(R.id.item_clazzlog_log_day) as TextView).text = prettyShortDay
        statusTextView.text = clazzLogAttendanceStatus

        if ((!showImage!!)) {
            secondaryTextImageView.visibility = View.INVISIBLE

            //Change the constraint layout so that the hidden bits are not empty spaces.
            val cl = holder.itemView.findViewById<ConstraintLayout>(R.id.item_clazzlog_log_cl)
            val constraintSet = ConstraintSet()
            constraintSet.clone(cl)

            constraintSet.connect(R.id.item_clazzlog_log_status_text,
                    ConstraintSet.START, R.id.item_clazzlog_log_calendar_image,
                    ConstraintSet.END, 16)

            constraintSet.applyTo(cl)


        } else {
            secondaryTextImageView.visibility = View.VISIBLE
        }


        holder.itemView.setOnClickListener { v -> thePresenter.goToClazzLogDetailActivity(clazzLog) }
    }
}
