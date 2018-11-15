package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzListPresenter;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

/**
 * The ClazzList Recycler Adapter used here.
 */
public class ClazzListRecyclerAdapter extends
        PagedListAdapter<ClazzWithNumStudents, ClazzListRecyclerAdapter.ClazzViewHolder> {

    Context theContext;
    private Fragment theFragment;
    private ClazzListPresenter thePresenter;

    class ClazzViewHolder extends RecyclerView.ViewHolder {

        ClazzViewHolder(View itemView) {
            super(itemView);
        }
    }

    ClazzListRecyclerAdapter(@NonNull DiffUtil.ItemCallback<ClazzWithNumStudents>
                   diffCallback, Context context, Fragment fragment, ClazzListPresenter mPresenter) {
        super(diffCallback);
        theContext = context;
        theFragment = fragment;
        thePresenter = mPresenter;
    }

    @NonNull
    @Override
    public ClazzViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View clazzListItem =
                LayoutInflater.from(theContext).inflate(R.layout.item_clazzlist_clazz,
                        parent, false);
        return new ClazzViewHolder(clazzListItem);
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     *
     * @param holder The view holder
     * @param position The position of the item
     */
    @Override
    public void onBindViewHolder  (@NonNull ClazzViewHolder holder, int position) {
        ClazzWithNumStudents clazz = getItem(position);
        assert clazz != null;
        long attendancePercentage = (long) (clazz.getAttendanceAverage() * 100);
        String lastRecordedAttendance = "";
        ((TextView)holder.itemView.findViewById(R.id.item_clazzlist_clazz_title))
                .setText(clazz.getClazzName());
        String numStudentsText = clazz.getNumStudents() + " " + theFragment.getResources()
                .getText(R.string.students_literal).toString();
        String attendancePercentageText =
                attendancePercentage + "% " + theFragment.getText(R.string.attendance)
                        + " (" + theFragment.getText(R.string.last_recorded)
                        + " " + lastRecordedAttendance + ")";
        ((TextView)holder.itemView.findViewById(R.id.item_clazzlist_numstudents_text))
                .setText(numStudentsText);
        ((TextView)holder.itemView.findViewById(R.id.item_clazzlist_attendance_percentage))
                .setText(attendancePercentageText);
        holder.itemView.setOnClickListener((view) -> thePresenter.handleClickClazz(clazz));
        holder.itemView.findViewById(R.id.item_clazzlist_attendance_record_attendance_button)
                .setOnClickListener((view)-> thePresenter.handleClickClazzRecordAttendance(clazz));

        ImageView trafficLight =
                holder.itemView.findViewById(R.id.item_clazzlist_attendance_trafficlight);
        if(attendancePercentage > 75L){
            trafficLight.setColorFilter(ContextCompat.getColor(theContext, R.color.traffic_green));
        }else if(attendancePercentage > 50L){
            trafficLight.setColorFilter(ContextCompat.getColor(theContext, R.color.traffic_orange));
        }else{
            trafficLight.setColorFilter(ContextCompat.getColor(theContext, R.color.traffic_red));
        }
    }
}
