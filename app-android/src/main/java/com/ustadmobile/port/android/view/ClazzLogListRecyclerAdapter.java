package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzLogListPresenter;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.lib.db.entities.ClazzLog;

/**
 * The ClazzLogList's recycler adapter.
 */
public class ClazzLogListRecyclerAdapter extends
        PagedListAdapter<ClazzLog, ClazzLogListRecyclerAdapter.ClazzLogViewHolder> {

    Context theContext;
    private Fragment theFragment;
    private ClazzLogListPresenter thePresenter;
    private Boolean showImage;

    class ClazzLogViewHolder extends RecyclerView.ViewHolder{
        ClazzLogViewHolder(View itemView){
            super(itemView);
        }
    }

    ClazzLogListRecyclerAdapter(@NonNull DiffUtil.ItemCallback<ClazzLog>
              diffCallback, Context context, Fragment fragment, ClazzLogListPresenter mPresenter,
                                          boolean imageShow){
        super(diffCallback);
        theContext = context;
        theFragment = fragment;
        thePresenter = mPresenter;
        showImage = imageShow;
    }

    @NonNull
    @Override
    public ClazzLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View clazzLogListItem =
                LayoutInflater.from(theContext).inflate(
                        R.layout.item_clazzlog_log, parent, false);
        return new ClazzLogViewHolder(clazzLogListItem);

    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     *
     * For every item part of the recycler adapter, this will be called and every item in it
     * will be set as per this function.
     *
     */
    @Override
    public void onBindViewHolder(@NonNull ClazzLogViewHolder holder, int position){
        ClazzLog clazzLog = getItem(position);

        assert clazzLog != null;
        String prettyDate = UMCalendarUtil.getPrettyDateFromLong(clazzLog.getLogDate());
        String prettyShortDay = UMCalendarUtil.getSimpleDayFromLongDate(clazzLog.getLogDate());

        ImageView secondaryTextImageView =
                holder.itemView.findViewById(R.id.item_clazzlog_log_status_text_imageview);

        int presentCount = clazzLog.getNumPresent();
        int absentCount = clazzLog.getNumAbsent();
        int partialCount = clazzLog.getNumPartial();
        String clazzLogAttendanceStatus ;
        if(partialCount > 0){
            clazzLogAttendanceStatus = presentCount + " " +
                    theFragment.getText(R.string.present) + ", " + absentCount + " " +
                    theFragment.getText(R.string.absent)  + ", " + partialCount + " " +
                    theFragment.getText(R.string.partial);
        }else{
            clazzLogAttendanceStatus = presentCount + " " +
                    theFragment.getText(R.string.present) + ", " + absentCount + " " +
                    theFragment.getText(R.string.absent);
        }

        TextView statusTextView = holder.itemView
                .findViewById(R.id.item_clazzlog_log_status_text);

        ((TextView)holder.itemView
                .findViewById(R.id.item_clazzlog_log_date))
                .setText(prettyDate);
        ((TextView)holder.itemView
                .findViewById(R.id.item_clazzlog_log_day))
                .setText(prettyShortDay);
        statusTextView.setText(clazzLogAttendanceStatus);

        if(!showImage){
            secondaryTextImageView.setVisibility(View.INVISIBLE);

            //Change the constraint layout so that the hidden bits are not empty spaces.
            ConstraintLayout cl = holder.itemView.findViewById(R.id.item_clazzlog_log_cl);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(cl);

            constraintSet.connect(R.id.item_clazzlog_log_status_text,
                    ConstraintSet.START, R.id.item_clazzlog_log_calendar_image,
                    ConstraintSet.END, 16);

            constraintSet.applyTo(cl);


        }else{
            secondaryTextImageView.setVisibility(View.VISIBLE);
        }


        holder.itemView.setOnClickListener(v -> thePresenter.goToClazzLogDetailActivity(clazzLog));
    }
}
