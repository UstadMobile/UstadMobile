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
import com.ustadmobile.core.controller.RoleAssignmentListPresenter;

import com.ustadmobile.lib.db.entities.EntityRoleWithGroupName;

public class RoleAssignmentListRecyclerAdapter extends
        PagedListAdapter<EntityRoleWithGroupName,
                RoleAssignmentListRecyclerAdapter.RoleAssignmentListViewHolder> {

    Context theContext;
    Activity theActivity;
    RoleAssignmentListPresenter mPresenter;

    @NonNull
    @Override
    public RoleAssignmentListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false);
        return new RoleAssignmentListViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull RoleAssignmentListViewHolder holder, int position) {

        EntityRoleWithGroupName entity = getItem(position);


    }

    protected class RoleAssignmentListViewHolder extends RecyclerView.ViewHolder {
        protected RoleAssignmentListViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected RoleAssignmentListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<EntityRoleWithGroupName> diffCallback,
            RoleAssignmentListPresenter thePresenter,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
    }


}
