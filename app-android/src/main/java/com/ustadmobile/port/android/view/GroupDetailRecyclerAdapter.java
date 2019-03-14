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
import com.ustadmobile.core.controller.GroupDetailPresenter;

import com.ustadmobile.lib.db.entities.PersonGroupMember;

public class GroupDetailRecyclerAdapter extends
        PagedListAdapter<PersonGroupMember,
                GroupDetailRecyclerAdapter.GroupDetailViewHolder> {

    Context theContext;
    Activity theActivity;
    GroupDetailPresenter mPresenter;

    @NonNull
    @Override
    public GroupDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false);
        return new GroupDetailViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull GroupDetailViewHolder holder, int position) {

        PersonGroupMember entity = getItem(position);


    }

    protected class GroupDetailViewHolder extends RecyclerView.ViewHolder {
        protected GroupDetailViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected GroupDetailRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<PersonGroupMember> diffCallback,
            GroupDetailPresenter thePresenter,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
    }


}
