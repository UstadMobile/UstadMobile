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
import com.ustadmobile.core.controller.AuditLogListPresenter;

import com.ustadmobile.lib.db.entities.AuditLog;

public class AuditLogListRecyclerAdapter extends
        PagedListAdapter<AuditLog,
                AuditLogListRecyclerAdapter.AuditLogListViewHolder> {

    Context theContext;
    Activity theActivity;
    AuditLogListPresenter mPresenter;

    @NonNull
    @Override
    public AuditLogListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false);
        return new AuditLogListViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull AuditLogListViewHolder holder, int position) {

        AuditLog entity = getItem(position);


    }

    protected class AuditLogListViewHolder extends RecyclerView.ViewHolder {
        protected AuditLogListViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected AuditLogListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<AuditLog> diffCallback,
            AuditLogListPresenter thePresenter,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
    }


}
