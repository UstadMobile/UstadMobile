package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleItemDetailPresenter;
import com.ustadmobile.lib.db.entities.SaleItemReminder;

public class SaleItemReminderRecyclerAdapter extends
        PagedListAdapter<SaleItemReminder,
                SaleItemReminderRecyclerAdapter.ViewHolder> {

    Context theContext;
    Activity theActivity;
    SaleItemDetailPresenter mPresenter;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_reminder, parent, false);
        return new ViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        SaleItemReminder entity = getItem(position);


    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        protected ViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected SaleItemReminderRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SaleItemReminder> diffCallback,
            SaleItemDetailPresenter thePresenter,
            Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theActivity = activity;
        theContext = context;
    }


}
