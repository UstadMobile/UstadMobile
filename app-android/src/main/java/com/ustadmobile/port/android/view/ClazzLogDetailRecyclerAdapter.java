package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzLogDetailPresenter;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson;

import java.util.HashMap;
import java.util.Map;

import static com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.STATUS_ABSENT;
import static com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.STATUS_ATTENDED;
import static com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.STATUS_PARTIAL;

/**
 * The Log Detail (Attendance) Recycler Adapter
 */
public class ClazzLogDetailRecyclerAdapter
        extends PagedListAdapter<ClazzLogAttendanceRecordWithPerson,
        ClazzLogDetailRecyclerAdapter.ClazzLogDetailViewHolder> {

    Context theContext;
    Activity theActivity;
    ClazzLogDetailPresenter thePresenter;
    //static map matching attendance status code value with color tint
    private static Map<Integer, Integer> STATUS_TO_COLOR_MAP = new HashMap<>();

    //static map matching attendance status code value with
    private static Map<Integer, Integer> STATUS_TO_STRING_ID_MAP = new HashMap<>();

    private static Map<Integer, Integer> SELECTED_STATUS_TO_STATUS_TAG = new HashMap<>();

    private static Map<Integer, Integer> UNSELECTED_STATUS_TO_STATUS_TAG = new HashMap<>();

    static {
        STATUS_TO_COLOR_MAP.put(STATUS_ATTENDED, R.color.traffic_green);
        STATUS_TO_COLOR_MAP.put(STATUS_ABSENT, R.color.traffic_red);
        STATUS_TO_COLOR_MAP.put(STATUS_PARTIAL, R.color.traffic_orange);

        STATUS_TO_STRING_ID_MAP.put(STATUS_ATTENDED, R.string.attendance);
        STATUS_TO_STRING_ID_MAP.put(STATUS_ABSENT, R.string.attendance);
        STATUS_TO_STRING_ID_MAP.put(STATUS_PARTIAL, R.string.attendance);

        SELECTED_STATUS_TO_STATUS_TAG.put(STATUS_ATTENDED, R.string.present_selected);
        SELECTED_STATUS_TO_STATUS_TAG.put(STATUS_ABSENT, R.string.absent_selected);
        SELECTED_STATUS_TO_STATUS_TAG.put(STATUS_PARTIAL, R.string.partial_selected);

        UNSELECTED_STATUS_TO_STATUS_TAG.put(STATUS_ATTENDED, R.string.present_unselected);
        UNSELECTED_STATUS_TO_STATUS_TAG.put(STATUS_ABSENT, R.string.absent_unselected);
        UNSELECTED_STATUS_TO_STATUS_TAG.put(STATUS_PARTIAL, R.string.partial_unselected);

    }

    protected class ClazzLogDetailViewHolder extends RecyclerView.ViewHolder{
        protected ClazzLogDetailViewHolder(View itemView){
            super(itemView);
        }
    }

    protected ClazzLogDetailRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<ClazzLogAttendanceRecordWithPerson> diffCallback,
            Context context, Activity activity, ClazzLogDetailPresenter mPresenter){
        super(diffCallback);
        theContext = context;
        theActivity = activity;
        thePresenter = mPresenter;
    }


    @NonNull
    @Override
    public ClazzLogDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View clazzLogDetailListItem =
                LayoutInflater.from(theContext).inflate(
                        R.layout.item_clazzlog_detail_student, parent, false);
        return new ClazzLogDetailViewHolder(clazzLogDetailListItem);
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
    public void onBindViewHolder(@NonNull ClazzLogDetailViewHolder holder, int position){
        ClazzLogAttendanceRecordWithPerson attendanceRecord = getItem(position);

        String studentName = attendanceRecord.getPerson().getFirstNames() + " " +
                attendanceRecord.getPerson().getLastName();

        holder.itemView.setTag(attendanceRecord.getClazzLogAttendanceRecordUid());

        ((TextView)holder.itemView
                .findViewById(R.id.item_clazzlog_detail_student_name)).setText(studentName);
        ((ImageView)holder.itemView
                .findViewById(R.id.item_clazzlog_detail_student_present_icon))
                .setColorFilter(Color.BLACK);

        final long clazzLogAttendanceRecordUid =
                attendanceRecord.getClazzLogAttendanceRecordUid();

        Map<Integer, ImageView> attendanceButtons = new HashMap<>();
        attendanceButtons.put(STATUS_ATTENDED, holder.itemView.findViewById(
                R.id.item_clazzlog_detail_student_present_icon));
        attendanceButtons.put(STATUS_ABSENT, holder.itemView.findViewById(
                R.id.item_clazzlog_detail_student_absent_icon));
        attendanceButtons.put(STATUS_PARTIAL, holder.itemView.findViewById(
                R.id.item_clazzlog_detail_student_delay_icon));

        for(Map.Entry<Integer, ImageView> entry : attendanceButtons.entrySet()) {
            boolean selectedOption = attendanceRecord.getAttendanceStatus() == entry.getKey();
            entry.getValue().setOnClickListener((view) -> thePresenter.handleMarkStudent(
                    clazzLogAttendanceRecordUid, entry.getKey()));
            entry.getValue().setColorFilter(
                    selectedOption ?
                            ContextCompat.getColor(theContext,
                                    STATUS_TO_COLOR_MAP.get(entry.getKey())) :
                            ContextCompat.getColor(
                                    theContext, R.color.color_gray));

            String status_tag = theActivity.getResources().getString(
                    STATUS_TO_STRING_ID_MAP.get(entry.getKey())) + " " +
                    (selectedOption ? SELECTED_STATUS_TO_STATUS_TAG.get(entry.getKey()) :
                            UNSELECTED_STATUS_TO_STATUS_TAG.get(entry.getKey()));
            entry.getValue().setTag(status_tag);
            //Set any content description here.
        }

    }
}
