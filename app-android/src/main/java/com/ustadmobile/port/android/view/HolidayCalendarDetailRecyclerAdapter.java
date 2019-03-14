package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.app.Activity;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.HolidayCalendarDetailPresenter;

import com.ustadmobile.lib.db.entities.UMCalendar;

public class HolidayCalendarDetailRecyclerAdapter extends
        PagedListAdapter<UMCalendar,
                HolidayCalendarDetailRecyclerAdapter.HolidayCalendarDetailViewHolder> {

    Context theContext;
    Activity theActivity;
    HolidayCalendarDetailPresenter mPresenter;

    @NonNull
    @Override
    public HolidayCalendarDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false);
        return new HolidayCalendarDetailViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull HolidayCalendarDetailViewHolder holder, int position) {

        UMCalendar entity = getItem(position);


    }

    protected class HolidayCalendarDetailViewHolder extends RecyclerView.ViewHolder {
        protected HolidayCalendarDetailViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected HolidayCalendarDetailRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<UMCalendar> diffCallback,
            HolidayCalendarDetailPresenter thePresenter,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
    }


}
