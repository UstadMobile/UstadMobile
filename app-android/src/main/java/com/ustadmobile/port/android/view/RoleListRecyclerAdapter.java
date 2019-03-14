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
import com.ustadmobile.core.controller.RoleListPresenter;

import com.ustadmobile.lib.db.entities.Role;

public class RoleListRecyclerAdapter extends
        PagedListAdapter<Role,
                RoleListRecyclerAdapter.RoleListViewHolder> {

    Context theContext;
    Activity theActivity;
    RoleListPresenter mPresenter;

    @NonNull
    @Override
    public RoleListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false);
        return new RoleListViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull RoleListViewHolder holder, int position) {

        Role entity = getItem(position);


    }

    protected class RoleListViewHolder extends RecyclerView.ViewHolder {
        protected RoleListViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected RoleListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<Role> diffCallback,
            RoleListPresenter thePresenter,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
    }


}
