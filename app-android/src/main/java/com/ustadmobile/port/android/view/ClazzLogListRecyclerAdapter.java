package com.ustadmobile.port.android.view;

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
import com.ustadmobile.core.controller.ClazzLogListPresenter;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.lib.db.entities.ClazzLog;

/**
 * The ClazzLogList's recycler adapter.
 */
public class ClazzLogListRecyclerAdapter extends
        PagedListAdapter<ClazzLog, ClazzLogListRecyclerAdapter.ClazzLogViewHolder> {

    Context theContext;
    Fragment theFragment;
    ClazzLogListPresenter thePresenter;

    protected class ClazzLogViewHolder extends RecyclerView.ViewHolder{
        protected ClazzLogViewHolder(View itemView){
            super(itemView);
        }
    }

    protected ClazzLogListRecyclerAdapter(@NonNull DiffUtil.ItemCallback<ClazzLog>
              diffCallback, Context context, Fragment fragment, ClazzLogListPresenter mPresenter){
        super(diffCallback);
        theContext = context;
        theFragment = fragment;
        thePresenter = mPresenter;
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
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull ClazzLogViewHolder holder, int position){
        ClazzLog clazzLog = getItem(position);

        String prettyDate = UMCalendarUtil.getPrettyDateFromLong(clazzLog.getLogDate());
        String prettyShortDay = UMCalendarUtil.getSimpleDayFromLongDate(clazzLog.getLogDate());

        int presentCount = clazzLog.getNumPresent();
        int absentCount = clazzLog.getNumAbsent();
        String clazzLogAttendanceStatus = presentCount + " " +
                theFragment.getText(R.string.present) + ", " + absentCount + " " +
                theFragment.getText(R.string.absent);

        ((TextView)holder.itemView
                .findViewById(R.id.item_clazzlog_log_date))
                .setText(prettyDate);
        ((TextView)holder.itemView
                .findViewById(R.id.item_clazzlog_log_day))
                .setText(prettyShortDay);
        ((TextView)holder.itemView
                .findViewById(R.id.item_clazzlog_log_status_text))
                .setText(clazzLogAttendanceStatus);

        holder.itemView.setOnClickListener(v -> thePresenter.goToClazzLogDetailActivity(clazzLog));
    }
}
