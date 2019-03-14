package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.app.Activity;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.LocationListPresenter;

import com.ustadmobile.lib.db.entities.LocationWithSubLocationCount;

public class LocationListRecyclerAdapter extends
        PagedListAdapter<LocationWithSubLocationCount,
                LocationListRecyclerAdapter.LocationListViewHolder> {

    Context theContext;
    Activity theActivity;
    LocationListPresenter mPresenter;

    @NonNull
    @Override
    public LocationListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false);
        return new LocationListViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull LocationListViewHolder holder, int position) {

        LocationWithSubLocationCount entity = getItem(position);

        TextView title = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_title);
        TextView desc = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_desc);
        AppCompatImageView menu =
                holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_dots);
        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);

            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {

                    return true;
                } else if (i == R.id.delete) {

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

    protected class LocationListViewHolder extends RecyclerView.ViewHolder {
        protected LocationListViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected LocationListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<LocationWithSubLocationCount> diffCallback,
            LocationListPresenter thePresenter,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
    }


}
