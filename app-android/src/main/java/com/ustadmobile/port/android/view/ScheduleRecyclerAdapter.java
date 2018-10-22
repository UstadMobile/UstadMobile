package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CommonHandlerPresenter;
import com.ustadmobile.lib.db.entities.Schedule;

import static com.ustadmobile.lib.db.entities.Schedule.DAY_FRIDAY;
import static com.ustadmobile.lib.db.entities.Schedule.DAY_MONDAY;
import static com.ustadmobile.lib.db.entities.Schedule.DAY_SATURDAY;
import static com.ustadmobile.lib.db.entities.Schedule.DAY_SUNDAY;
import static com.ustadmobile.lib.db.entities.Schedule.DAY_THURSDAY;
import static com.ustadmobile.lib.db.entities.Schedule.DAY_TUESDAY;
import static com.ustadmobile.lib.db.entities.Schedule.DAY_WEDNESDAY;
import static com.ustadmobile.lib.db.entities.Schedule.MONTH_APRIL;
import static com.ustadmobile.lib.db.entities.Schedule.MONTH_AUGUST;
import static com.ustadmobile.lib.db.entities.Schedule.MONTH_DECEMBER;
import static com.ustadmobile.lib.db.entities.Schedule.MONTH_FEBUARY;
import static com.ustadmobile.lib.db.entities.Schedule.MONTH_JANUARY;
import static com.ustadmobile.lib.db.entities.Schedule.MONTH_JULY;
import static com.ustadmobile.lib.db.entities.Schedule.MONTH_JUNE;
import static com.ustadmobile.lib.db.entities.Schedule.MONTH_MARCH;
import static com.ustadmobile.lib.db.entities.Schedule.MONTH_MAY;
import static com.ustadmobile.lib.db.entities.Schedule.MONTH_NOVEMBER;
import static com.ustadmobile.lib.db.entities.Schedule.MONTH_OCTOBER;
import static com.ustadmobile.lib.db.entities.Schedule.MONTH_SEPTEMBER;
import static com.ustadmobile.lib.db.entities.Schedule.SCHEDULE_FREQUENCY_DAILY;
import static com.ustadmobile.lib.db.entities.Schedule.SCHEDULE_FREQUENCY_MONTHLY;
import static com.ustadmobile.lib.db.entities.Schedule.SCHEDULE_FREQUENCY_ONCE;
import static com.ustadmobile.lib.db.entities.Schedule.SCHEDULE_FREQUENCY_WEEKLY;
import static com.ustadmobile.lib.db.entities.Schedule.SCHEDULE_FREQUENCY_YEARLY;

public class ScheduleRecyclerAdapter extends
        PagedListAdapter<Schedule, ScheduleRecyclerAdapter.ScheduleViewHolder> {

    Context theContext;
    Fragment theFragment;
    Activity theActivity;
    CommonHandlerPresenter mPresenter;

    protected class ScheduleViewHolder extends RecyclerView.ViewHolder{
        protected ScheduleViewHolder(View itemView){super(itemView);}

    }

    protected ScheduleRecyclerAdapter(@NonNull DiffUtil.ItemCallback<Schedule> diffCallback,
                                      Context context, Activity activity,
                                      CommonHandlerPresenter presenter){
        super(diffCallback);
        theContext =context;
        theActivity = activity;
        mPresenter = presenter;
    }

    /**
     * This method inflates the card layout (to parent view given) and returns it.
     * @param parent View given.
     * @param viewType View Type not used here.
     * @return New ViewHolder for the ClazzStudent type
     */
    @NonNull
    @Override
    public ScheduleRecyclerAdapter.ScheduleViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View scheduleListItem =
                LayoutInflater.from(theContext).inflate(
                        R.layout.item_schedule, parent, false);
        return new ScheduleRecyclerAdapter.ScheduleViewHolder(scheduleListItem);
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     * @param holder    The holder
     * @param position  The position in the recycler view.
     */
    @Override
    public void onBindViewHolder(
            @NonNull ScheduleRecyclerAdapter.ScheduleViewHolder holder, int position) {

        Schedule thisSchedule = getItem(position);
        int scheduleFrequencyCode = thisSchedule.getScheduleFrequency();
        String scheduleFrequency;
        switch (scheduleFrequencyCode){
            case SCHEDULE_FREQUENCY_ONCE:
                scheduleFrequency = "Once";
                break;
            case SCHEDULE_FREQUENCY_WEEKLY:
                scheduleFrequency = "Weekly";
                break;
            case SCHEDULE_FREQUENCY_DAILY:
                scheduleFrequency = "Daily";
                break;
            case SCHEDULE_FREQUENCY_MONTHLY:
                scheduleFrequency = "Monthly";
                break;
            case SCHEDULE_FREQUENCY_YEARLY:
                scheduleFrequency = "Yearly";
                break;
            default:
                scheduleFrequency = "";
                break;

        }

        long startTimeLong = thisSchedule.getSceduleStartTime();
        long endTimeLong = thisSchedule.getScheduleEndTime();

        //TODO: this
        String startTime = "";
        String endTime = "";

        int scheduleDayCode = thisSchedule.getScheduleDay();
        int scheduleMonthCode = thisSchedule.getScheduleMonth();
        
        String scheduleDay;
        String scheduleMonth;
        
        switch(scheduleDayCode){
            case DAY_SUNDAY:
                scheduleDay = "Sunday";
                break;
            case DAY_MONDAY:
                scheduleDay = "Monday";
                break;
            case DAY_TUESDAY:
                scheduleDay = "Tuesday";
                break;
            case DAY_WEDNESDAY:
                scheduleDay = "Wednesday";
                break;
            case DAY_THURSDAY:
                scheduleDay = "Thursday";
                break;
            case DAY_FRIDAY:
                scheduleDay = "Friday";
                break;
            case DAY_SATURDAY:
                scheduleDay = "Saturday";
                break;
            default:
                scheduleDay = "";
                break;
        }
        
        switch (scheduleMonthCode){
            case MONTH_JANUARY :
                scheduleMonth = "January";
                break;
            case MONTH_FEBUARY :
                scheduleMonth = "Febuary";
                break;
            case MONTH_MARCH :
                scheduleMonth = "March";
                break;
            case MONTH_APRIL :
                scheduleMonth = "April";
                break;
            case MONTH_MAY :
                scheduleMonth = "May";
                break;
            case MONTH_JUNE :
                scheduleMonth = "June";
                break;
            case MONTH_JULY :
                scheduleMonth = "July";
                break;
            case MONTH_AUGUST :
                scheduleMonth = "August";
                break;
            case MONTH_SEPTEMBER :
                scheduleMonth = "September";
                break;
            case MONTH_OCTOBER :
                scheduleMonth = "October";
                break;
            case MONTH_NOVEMBER :
                scheduleMonth = "November";
                break;
            case MONTH_DECEMBER :
                scheduleMonth = "December";
                break;
            default:
                scheduleMonth = "";
                break;

        }

        TextView scheduleText = holder.itemView.findViewById(R.id.item_schedule_title);
        String scheduleEntryText = scheduleFrequency + " - " + scheduleDay + scheduleMonth + " " +
                theContext.getText(R.string.from) + " " + startTime + " " +
                theContext.getText(R.string.to) + " " + endTime;
        scheduleText.setText(scheduleEntryText);

        //TODO: Add Menu time and their handlers.

    }
}
