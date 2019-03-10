package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzActivityListPresenter;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.lib.db.entities.ClazzActivity;
import com.ustadmobile.lib.db.entities.ClazzActivityWithChangeTitle;

import java.util.Locale;

/**
 * The ClazzActivityList's recycler adapter.
 */
public class ClazzActivityListRecyclerAdapter extends
        PagedListAdapter<ClazzActivityWithChangeTitle, ClazzActivityListRecyclerAdapter.ClazzActivityViewHolder> {

    Context theContext;
    private Fragment theFragment;
    private ClazzActivityListPresenter thePresenter;
    private Boolean showImage;


    class ClazzActivityViewHolder extends RecyclerView.ViewHolder{
        ClazzActivityViewHolder(View itemView){
            super(itemView);
        }
    }


    ClazzActivityListRecyclerAdapter(@NonNull DiffUtil.ItemCallback<ClazzActivityWithChangeTitle>
                                             diffCallback, Context context, Fragment fragment,
                                     ClazzActivityListPresenter mPresenter,
                                     boolean imageShow){
        super(diffCallback);
        theContext = context;
        theFragment = fragment;
        thePresenter = mPresenter;
        showImage = imageShow;
    }

    @NonNull
    @Override
    public ClazzActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View clazzLogListItem =
                LayoutInflater.from(theContext).inflate(
                        R.layout.item_clazzlog_log, parent, false);
        return new ClazzActivityViewHolder(clazzLogListItem);

    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     *
     * For every item part of the recycler adapter, this will be called and every item in it
     * will be set as per this function.
     *
     * @param holder            The holder
     * @param position          The position
     */
    @Override
    public void onBindViewHolder(@NonNull ClazzActivityViewHolder holder, int position){
        ClazzActivityWithChangeTitle clazzActivity = getItem(position);
        assert clazzActivity != null;
        boolean wasItGood = clazzActivity.isClazzActivityGoodFeedback();


        Locale currentLocale = theFragment.getResources().getConfiguration().locale;

        String prettyDate =
                UMCalendarUtil.getPrettyDateFromLong(
                        clazzActivity.getClazzActivityLogDate(), currentLocale);
        String prettyShortDay =
                UMCalendarUtil.getSimpleDayFromLongDate(
                        clazzActivity.getClazzActivityLogDate(), currentLocale);

        TextView statusTextView = holder.itemView
                .findViewById(R.id.item_clazzlog_log_status_text);

        AppCompatImageView secondaryTextImageView =
                holder.itemView.findViewById(R.id.item_clazzlog_log_status_text_imageview);

        String verb = clazzActivity.getChangeTitle();
        if(verb == null || verb.isEmpty()){
            verb =  "Increased group work by";
        }

        if(!wasItGood){
            secondaryTextImageView.setBackground(
                    AppCompatResources.getDrawable(theContext,R.drawable.ic_thumb_down_black_24dp));
        }else{
            secondaryTextImageView.setBackground(
                    AppCompatResources.getDrawable(theContext,R.drawable.ic_thumb_up_black_24dp));
        }

        String desc = verb + " " + clazzActivity.getClazzActivityQuantity()
                + " times";
        statusTextView.setText(desc);

        ((TextView)holder.itemView
                .findViewById(R.id.item_clazzlog_log_date))
                .setText(prettyDate);
        ((TextView)holder.itemView
                .findViewById(R.id.item_clazzlog_log_day))
                .setText(prettyShortDay);

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


        holder.itemView.setOnClickListener(
                v -> thePresenter.goToNewClazzActivityEditActivity(clazzActivity.getClazzActivityUid()));
    }
}