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
import com.ustadmobile.core.controller.GroupListPresenter;

import com.ustadmobile.lib.db.entities.GroupWithMemberCount;

public class GroupListRecyclerAdapter extends
        PagedListAdapter<GroupWithMemberCount,
                GroupListRecyclerAdapter.GroupListViewHolder> {

    Context theContext;
    Activity theActivity;
    GroupListPresenter mPresenter;

    @NonNull
    @Override
    public GroupListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false);
        return new GroupListViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull GroupListViewHolder holder, int position) {

        GroupWithMemberCount entity = getItem(position);


    }

    protected class GroupListViewHolder extends RecyclerView.ViewHolder {
        protected GroupListViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected GroupListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<GroupWithMemberCount> diffCallback,
            GroupListPresenter thePresenter,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
    }


}
