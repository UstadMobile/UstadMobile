package com.ustadmobile.staging.port.android.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CommonHandlerPresenter
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_FRIDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_MONDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_SATURDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_SUNDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_THURSDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_TUESDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_WEDNESDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.MONTH_APRIL
import com.ustadmobile.lib.db.entities.Schedule.Companion.MONTH_AUGUST
import com.ustadmobile.lib.db.entities.Schedule.Companion.MONTH_DECEMBER
import com.ustadmobile.lib.db.entities.Schedule.Companion.MONTH_FEBUARY
import com.ustadmobile.lib.db.entities.Schedule.Companion.MONTH_JANUARY
import com.ustadmobile.lib.db.entities.Schedule.Companion.MONTH_JULY
import com.ustadmobile.lib.db.entities.Schedule.Companion.MONTH_JUNE
import com.ustadmobile.lib.db.entities.Schedule.Companion.MONTH_MARCH
import com.ustadmobile.lib.db.entities.Schedule.Companion.MONTH_MAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.MONTH_NOVEMBER
import com.ustadmobile.lib.db.entities.Schedule.Companion.MONTH_OCTOBER
import com.ustadmobile.lib.db.entities.Schedule.Companion.MONTH_SEPTEMBER
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_DAILY
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_MONTHLY
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_ONCE
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_WEEKLY
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_YEARLY
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ScheduleRecyclerAdapter internal constructor(diffCallback: DiffUtil.ItemCallback<Schedule>,
                                   internal var theContext: Context, private val theActivity: Activity,
                                   internal var mPresenter: CommonHandlerPresenter<*>)
    : PagedListAdapter<Schedule, ScheduleRecyclerAdapter.ScheduleViewHolder>(diffCallback) {

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * This method inflates the card layout (to parent view given) and returns it.
     * @param parent View given.
     * @param viewType View Type not used here.
     * @return New ViewHolder for the ClazzStudent type
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {

        val scheduleListItem = LayoutInflater.from(theContext).inflate(
                R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(scheduleListItem)
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     * @param holder    The holder
     * @param position  The position in the recycler view.
     */
    override fun onBindViewHolder(
            holder: ScheduleViewHolder, position: Int) {

        val thisSchedule = getItem(position)

        val startTimeLong = thisSchedule!!.sceduleStartTime
        val endTimeLong = thisSchedule.scheduleEndTime
        val scheduleDayCode = thisSchedule.scheduleDay
        val scheduleMonthCode = thisSchedule.scheduleMonth
        val scheduleFrequencyCode = thisSchedule.scheduleFrequency

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

        val scheduleDay: String
        val scheduleMonth: String
        val scheduleFrequency: String

        //Get the text corresponding the schedule codes:
        //Frequency
        when (scheduleFrequencyCode) {
            SCHEDULE_FREQUENCY_ONCE -> scheduleFrequency = theActivity.getText(R.string.once).toString()
            SCHEDULE_FREQUENCY_WEEKLY -> scheduleFrequency = theActivity.getText(R.string.weekly).toString()
            SCHEDULE_FREQUENCY_DAILY -> scheduleFrequency = theActivity.getText(R.string.daily).toString()
            SCHEDULE_FREQUENCY_MONTHLY -> scheduleFrequency = theActivity.getText(R.string.monthly).toString()
            SCHEDULE_FREQUENCY_YEARLY -> scheduleFrequency = theActivity.getText(R.string.yearly).toString()
            else -> scheduleFrequency = ""
        }

        //Day
        when (scheduleDayCode) {
            DAY_SUNDAY -> scheduleDay = theActivity.getText(R.string.sunday).toString()
            DAY_MONDAY -> scheduleDay = theActivity.getText(R.string.monday).toString()
            DAY_TUESDAY -> scheduleDay = theActivity.getText(R.string.tuesday).toString()
            DAY_WEDNESDAY -> scheduleDay = theActivity.getText(R.string.wednesday).toString()
            DAY_THURSDAY -> scheduleDay = theActivity.getText(R.string.thursday).toString()
            DAY_FRIDAY -> scheduleDay = theActivity.getText(R.string.friday).toString()
            DAY_SATURDAY -> scheduleDay = theActivity.getText(R.string.saturday).toString()
            else -> scheduleDay = ""
        }

        //Month
        when (scheduleMonthCode) {
            MONTH_JANUARY -> scheduleMonth = theActivity.getText(R.string.jan).toString()
            MONTH_FEBUARY -> scheduleMonth = theActivity.getText(R.string.feb).toString()
            MONTH_MARCH -> scheduleMonth = theActivity.getText(R.string.mar).toString()
            MONTH_APRIL -> scheduleMonth = theActivity.getText(R.string.apr).toString()
            MONTH_MAY -> scheduleMonth = theActivity.getText(R.string.may).toString()
            MONTH_JUNE -> scheduleMonth = theActivity.getText(R.string.jun).toString()
            MONTH_JULY -> scheduleMonth = theActivity.getText(R.string.jul).toString()
            MONTH_AUGUST -> scheduleMonth = theActivity.getText(R.string.aug).toString()
            MONTH_SEPTEMBER -> scheduleMonth = theActivity.getText(R.string.sep).toString()
            MONTH_OCTOBER -> scheduleMonth = theActivity.getText(R.string.oct).toString()
            MONTH_NOVEMBER -> scheduleMonth = theActivity.getText(R.string.nov).toString()
            MONTH_DECEMBER -> scheduleMonth = theActivity.getText(R.string.dec).toString()
            else -> scheduleMonth = ""
        }

        //Add text to entry for this schedule
        val scheduleText = holder.itemView.findViewById<TextView>(R.id.item_schedule_title)
        val scheduleEntryText = scheduleFrequency + " - " + scheduleDay + scheduleMonth + " " +
                theContext.getText(R.string.from) + " " + startTime + " " +
                theContext.getText(R.string.to) + " " + endTime
        scheduleText.text = scheduleEntryText

        //Options to Edit/Delete every schedule in the list
        val optionsImageView = holder.itemView.findViewById<ImageView>(R.id.item_schedule_secondary_menu_imageview)
        optionsImageView.setOnClickListener { v: View ->
            //creating a popup menu
            val popup = PopupMenu(theActivity.applicationContext, v)

            popup.setOnMenuItemClickListener { item ->
                val i = item.itemId
                if (i == R.id.edit) {
                    mPresenter.handleCommonPressed(thisSchedule.scheduleUid)
                    true
                } else if (i == R.id.delete) {
                    mPresenter.handleSecondaryPressed(thisSchedule.scheduleUid)
                    true
                } else {
                    false
                }
            }
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_item_schedule)

            //displaying the popup
            popup.show()
        }


    }
}
