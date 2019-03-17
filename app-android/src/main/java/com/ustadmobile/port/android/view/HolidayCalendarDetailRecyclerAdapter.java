package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.app.Activity;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.HolidayCalendarDetailPresenter;

import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.lib.db.entities.DateRange;

public class HolidayCalendarDetailRecyclerAdapter extends
        PagedListAdapter<DateRange,
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

        DateRange entity = getItem(position);

        TextView title = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_title);
        TextView desc = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_desc);
        AppCompatImageView menu =
                holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_dots);

        assert entity != null;
        String rangeString =
                UMCalendarUtil.getPrettySuperSimpleDateSimpleWithoutYearFromLong(entity.getDateRangeFromDate());

        if (entity.getDateRangeToDate() > 0) {
            rangeString = rangeString + " - " +
                    UMCalendarUtil.getPrettySuperSimpleDateSimpleWithoutYearFromLong(entity.getDateRangeToDate());
        }
        title.setText(rangeString);

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);

            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {
                    mPresenter.handleEditRange(entity.getDateRangeUid());
                    return true;
                } else if (i == R.id.delete) {
                    mPresenter.handleDeleteRange(entity.getDateRangeUid());
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

    protected class HolidayCalendarDetailViewHolder extends RecyclerView.ViewHolder {
        protected HolidayCalendarDetailViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected HolidayCalendarDetailRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<DateRange> diffCallback,
            HolidayCalendarDetailPresenter thePresenter,
            Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theActivity = activity;
        theContext = context;
    }


}
