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
import com.ustadmobile.core.controller.HolidayCalendarListPresenter;

import com.ustadmobile.lib.db.entities.Holiday;

public class HolidayCalendarListRecyclerAdapter extends
        PagedListAdapter<Holiday,
                HolidayCalendarListRecyclerAdapter.HolidayCalendarListViewHolder> {

    Context theContext;
    Activity theActivity;
    HolidayCalendarListPresenter mPresenter;

    @NonNull
    @Override
    public HolidayCalendarListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false);
        return new HolidayCalendarListViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull HolidayCalendarListViewHolder holder, int position) {

        Holiday entity = getItem(position);


    }

    protected class HolidayCalendarListViewHolder extends RecyclerView.ViewHolder {
        protected HolidayCalendarListViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected HolidayCalendarListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<Holiday> diffCallback,
            HolidayCalendarListPresenter thePresenter,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
    }


}
