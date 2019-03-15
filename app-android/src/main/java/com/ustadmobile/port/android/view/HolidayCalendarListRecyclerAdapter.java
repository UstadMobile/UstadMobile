package com.ustadmobile.port.android.view;

import android.app.Activity;
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

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.HolidayCalendarListPresenter;
import com.ustadmobile.lib.db.entities.UMCalendar;
import com.ustadmobile.lib.db.entities.UMCalendarWithNumEntries;

public class HolidayCalendarListRecyclerAdapter extends
        PagedListAdapter<UMCalendarWithNumEntries,
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

        UMCalendarWithNumEntries entity = getItem(position);

        TextView title = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_title);
        TextView desc = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_desc);
        AppCompatImageView menu =
                holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_dots);

        assert entity != null;
        String entityTitle = entity.getUmCalendarName();
        title.setText(entityTitle);
        int numEntries = entity.getNumEntries();
        String entriesString = theActivity.getText(R.string.entries).toString();
        if(numEntries == 1){
            entriesString = theActivity.getText(R.string.entry).toString();
        }
        String numEntitiesString = numEntries + " " + entriesString;
        desc.setText(numEntitiesString);

        holder.itemView.setOnClickListener(v -> mPresenter.handleEditCalendar(entity.getUmCalendarUid()));

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);

            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {
                    mPresenter.handleEditCalendar(entity.getUmCalendarUid());
                    return true;
                } else if (i == R.id.delete) {
                    mPresenter.handleDeleteCalendar(entity.getUmCalendarUid());
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

    protected class HolidayCalendarListViewHolder extends RecyclerView.ViewHolder {
        protected HolidayCalendarListViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected HolidayCalendarListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<UMCalendarWithNumEntries> diffCallback,
            HolidayCalendarListPresenter thePresenter, Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theActivity = activity;
        theContext = context;
    }


}
