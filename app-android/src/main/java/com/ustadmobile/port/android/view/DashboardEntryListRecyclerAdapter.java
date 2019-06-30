package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.DashboardEntryListPresenter;
import com.ustadmobile.lib.db.entities.DashboardEntry;

public class DashboardEntryListRecyclerAdapter extends
        PagedListAdapter<DashboardEntry,
                DashboardEntryListRecyclerAdapter.DashboardEntryListViewHolder> {

    Context theContext;
    Activity theActivity;
    DashboardEntryListPresenter mPresenter;

    @NonNull
    @Override
    public DashboardEntryListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_dashboard_entry, parent, false);
        return new DashboardEntryListViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull DashboardEntryListViewHolder holder, int position) {

        DashboardEntry entity = getItem(position);
        //TODO

        AppCompatImageView dots = holder.itemView.findViewById(R.id.item_dashboard_entry_dots);
        AppCompatImageView pin = holder.itemView.findViewById(R.id.item_dashboard_entry_flag);

        if(entity.getDashboardEntryIndex() < 0 ){
            pin.setColorFilter(ContextCompat.getColor(theContext, R.color.primary_dark));
        }else{
            pin.setColorFilter(ContextCompat.getColor(theContext, R.color.text_primary));
        }

        long entryUid = entity.getDashboardEntryUid();
        String existingTitle = entity.getDashboardEntryTitle();

        //Options to Edit/Delete every schedule in the list
        dots.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);
            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {
                    mPresenter.handleEditEntry(entryUid);
                    return true;
                } else if (i == R.id.delete) {
                    mPresenter.handleDeleteEntry(entryUid);
                    return true;
                } else if (i == R.id.set_title) {
                    mPresenter.handleChangeTitle(entryUid, existingTitle);
                    return true;
                } else {
                    return false;
                }
            });
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_edit_delete_set_title);

            popup.getMenu().findItem(R.id.edit).setVisible(true);

            //displaying the popup
            popup.show();
        });

        pin.setOnClickListener(v -> mPresenter.handlePinEntry(entryUid));

        TextView title = holder.itemView.findViewById(R.id.item_dashboard_entry_title);
        title.setText(entity.getDashboardEntryTitle());




    }

    protected class DashboardEntryListViewHolder extends RecyclerView.ViewHolder {
        protected DashboardEntryListViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected DashboardEntryListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<DashboardEntry> diffCallback,
            DashboardEntryListPresenter thePresenter,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
    }


}
