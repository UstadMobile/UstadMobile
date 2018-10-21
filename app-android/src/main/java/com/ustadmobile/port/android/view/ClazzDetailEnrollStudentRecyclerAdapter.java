package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzDetailEnrollStudentPresenter;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.util.HashMap;

public class ClazzDetailEnrollStudentRecyclerAdapter
        extends PagedListAdapter<PersonWithEnrollment,
        ClazzDetailEnrollStudentRecyclerAdapter.ClazzLogDetailViewHolder> {

    Context theContext;
    Activity theActivity;
    private ClazzDetailEnrollStudentPresenter mPresenter;

    HashMap checkBoxHM = new HashMap();

    protected class ClazzLogDetailViewHolder extends RecyclerView.ViewHolder{
        protected ClazzLogDetailViewHolder(View itemView){
            super(itemView);
        }
    }

    protected ClazzDetailEnrollStudentRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<PersonWithEnrollment> diffCallback){
        super(diffCallback);
    }
    protected ClazzDetailEnrollStudentRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<PersonWithEnrollment> diffCallback, Context context,
            Activity activity, ClazzDetailEnrollStudentPresenter  presenter){
        super(diffCallback);
        theContext = context;
        theActivity = activity;
        mPresenter = presenter;
    }

    @NonNull
    @Override
    public ClazzDetailEnrollStudentRecyclerAdapter.ClazzLogDetailViewHolder
            onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View clazzLogDetailListItem =
                LayoutInflater.from(theContext).inflate(
                        R.layout.item_studentlistenroll_student, parent, false);
        return new ClazzDetailEnrollStudentRecyclerAdapter.ClazzLogDetailViewHolder(
                clazzLogDetailListItem);
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     *
     * Every item in the recycler view will have set its colors if no attendance status is set.
     * every attendance button will have it-self mapped to tints on activation.
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(
            @NonNull ClazzDetailEnrollStudentRecyclerAdapter.ClazzLogDetailViewHolder holder,
            int position){

        PersonWithEnrollment personWithEnrollment = getItem(position);

        String studentName = personWithEnrollment.getFirstNames() + " " +
                personWithEnrollment.getLastName();

        long attendancePercentage =
                (long) (personWithEnrollment.getAttendancePercentage() * 100);
        String studentAttendancePercentage = attendancePercentage +
                "% " + theActivity.getText(R.string.attendance);
        ImageView trafficLight = ((ImageView) holder.itemView
                .findViewById(R.id.item_studentlist_student_simple_attendance_trafficlight));
        if(attendancePercentage > 75L){
            trafficLight.setColorFilter(ContextCompat.getColor(theContext,
                    R.color.traffic_green));
        }else if(attendancePercentage > 50L){
            trafficLight.setColorFilter(ContextCompat.getColor(theContext,
                    R.color.traffic_orange));
        }else{
            trafficLight.setColorFilter(ContextCompat.getColor(theContext,
                    R.color.traffic_red));
        }

        ((TextView)holder.itemView
                .findViewById(R.id.item_studentlist_student_simple_student_title))
                .setText(studentName);
        ((TextView)holder.itemView
                .findViewById(R.id.item_studentlist_student_simple_attendance_percentage))
                .setText(studentAttendancePercentage);

        //Get checkbox and set it to visible.
        CheckBox checkBox =
                holder.itemView.findViewById(R.id.item_studentlist_student_simple_student_checkbox);
        checkBox.setVisibility(View.VISIBLE);

        //Get current person's enrollment w.r.t. this class. (Its either set or null (not enrolled)
        boolean personWithEnrollmentBoolean = false;
        if (personWithEnrollment.getEnrolled() != null){
            personWithEnrollmentBoolean = personWithEnrollment.getEnrolled();
        }

        //To preserve checkboxes, add this enrollment to the Map.
        checkBoxHM.put(personWithEnrollment.getPersonUid(), personWithEnrollmentBoolean);
        //set the value of the check according to the value..
        checkBox.setChecked((Boolean) checkBoxHM.get(personWithEnrollment.getPersonUid()));

        //Add a change listener to the checkbox
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                checkBox.setChecked(isChecked));

        checkBox.setOnClickListener(v -> {
            final boolean isChecked = checkBox.isChecked();
            mPresenter.handleEnrollChanged(personWithEnrollment, isChecked);
        });


    }
}