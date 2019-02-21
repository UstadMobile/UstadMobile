package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CommonHandlerPresenter;
import com.ustadmobile.lib.db.entities.Schedule;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    private Activity theActivity;
    CommonHandlerPresenter mPresenter;

    class ScheduleViewHolder extends RecyclerView.ViewHolder{
        ScheduleViewHolder(View itemView){super(itemView);}

    }

    ScheduleRecyclerAdapter(@NonNull DiffUtil.ItemCallback<Schedule> diffCallback,
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

        long startTimeLong = thisSchedule.getSceduleStartTime();
        long endTimeLong = thisSchedule.getScheduleEndTime();
        int scheduleDayCode = thisSchedule.getScheduleDay();
        int scheduleMonthCode = thisSchedule.getScheduleMonth();
        int scheduleFrequencyCode = thisSchedule.getScheduleFrequency();

        //start time
        Date startTimeDate = new Date(startTimeLong);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        String startTime = formatter.format(startTimeDate);

        //end time
        Date endTimeDate = new Date(endTimeLong);
        String endTime = formatter.format(endTimeDate);


        String scheduleDay;
        String scheduleMonth;
        String scheduleFrequency;

        //Get the text corresponding the schedule codes:
        //Frequency
        switch (scheduleFrequencyCode){
            case SCHEDULE_FREQUENCY_ONCE:
                scheduleFrequency = theActivity.getText(R.string.once).toString();
                break;
            case SCHEDULE_FREQUENCY_WEEKLY:
                scheduleFrequency = theActivity.getText(R.string.weekly).toString();
                break;
            case SCHEDULE_FREQUENCY_DAILY:
                scheduleFrequency = theActivity.getText(R.string.daily).toString();
                break;
            case SCHEDULE_FREQUENCY_MONTHLY:
                scheduleFrequency = theActivity.getText(R.string.monthly).toString();
                break;
            case SCHEDULE_FREQUENCY_YEARLY:
                scheduleFrequency = theActivity.getText(R.string.yearly).toString();
                break;
            default:
                scheduleFrequency = "";
                break;

        }

        //Day
        switch(scheduleDayCode){
            case DAY_SUNDAY:
                scheduleDay = theActivity.getText(R.string.sunday).toString();
                break;
            case DAY_MONDAY:
                scheduleDay = theActivity.getText(R.string.monday).toString();
                break;
            case DAY_TUESDAY:
                scheduleDay = theActivity.getText(R.string.tuesday).toString();
                break;
            case DAY_WEDNESDAY:
                scheduleDay = theActivity.getText(R.string.wednesday).toString();
                break;
            case DAY_THURSDAY:
                scheduleDay = theActivity.getText(R.string.thursday).toString();
                break;
            case DAY_FRIDAY:
                scheduleDay = theActivity.getText(R.string.friday).toString();
                break;
            case DAY_SATURDAY:
                scheduleDay = theActivity.getText(R.string.saturday).toString();
                break;
            default:
                scheduleDay = "";
                break;
        }

        //Month
        switch (scheduleMonthCode){
            case MONTH_JANUARY :
                scheduleMonth = theActivity.getText(R.string.jan).toString();
                break;
            case MONTH_FEBUARY :
                scheduleMonth = theActivity.getText(R.string.feb).toString();
                break;
            case MONTH_MARCH :
                scheduleMonth = theActivity.getText(R.string.mar).toString();
                break;
            case MONTH_APRIL :
                scheduleMonth = theActivity.getText(R.string.apr).toString();
                break;
            case MONTH_MAY :
                scheduleMonth = theActivity.getText(R.string.may).toString();
                break;
            case MONTH_JUNE :
                scheduleMonth = theActivity.getText(R.string.jun).toString();
                break;
            case MONTH_JULY :
                scheduleMonth = theActivity.getText(R.string.jul).toString();
                break;
            case MONTH_AUGUST :
                scheduleMonth = theActivity.getText(R.string.aug).toString();
                break;
            case MONTH_SEPTEMBER :
                scheduleMonth = theActivity.getText(R.string.sep).toString();
                break;
            case MONTH_OCTOBER :
                scheduleMonth = theActivity.getText(R.string.oct).toString();
                break;
            case MONTH_NOVEMBER :
                scheduleMonth = theActivity.getText(R.string.nov).toString();
                break;
            case MONTH_DECEMBER :
                scheduleMonth = theActivity.getText(R.string.dec).toString();
                break;
            default:
                scheduleMonth = "";
                break;

        }

        //Add text to entry for this schedule
        TextView scheduleText = holder.itemView.findViewById(R.id.item_schedule_title);
        String scheduleEntryText = scheduleFrequency + " - " + scheduleDay + scheduleMonth + " " +
                theContext.getText(R.string.from) + " " + startTime + " " +
                theContext.getText(R.string.to) + " " + endTime;
        scheduleText.setText(scheduleEntryText);

        //Options to Edit/Delete every schedule in the list
        ImageView optionsImageView =
                holder.itemView.findViewById(R.id.item_schedule_secondary_menu_imageview);
        optionsImageView.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);

            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {
                    mPresenter.handleCommonPressed(thisSchedule.getScheduleUid());
                    return true;
                } else if (i == R.id.delete) {
                    mPresenter.handleSecondaryPressed(thisSchedule.getScheduleUid());
                    return true;
                } else {
                    return false;
                }
            });
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_item_schedule);

            //displaying the popup
            popup.show();
        });


    }
}
