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
        TextView title = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_title);
        TextView desc = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_desc);
        AppCompatImageView menu =
                holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_dots);

        holder.itemView.setOnClickListener(v -> {
            assert entity != null;
            mPresenter.handleEditGroup(entity.getGroupUid());
        });

        assert entity != null;
        if(entity == null){
            return;
        }
        title.setText(entity.getGroupName());
        String membersString = theActivity.getText(R.string.members).toString();
        int count = entity.getMemberCount();
        if(count == 1){
            membersString=theActivity.getText(R.string.member).toString();

        }
        String descString = count + " " + membersString;
        desc.setText(descString);

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);

            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {
                    mPresenter.handleEditGroup(entity.getGroupUid());
                    return true;
                } else if (i == R.id.delete) {
                    mPresenter.handleDeleteGroup(entity.getGroupUid());
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

    protected class GroupListViewHolder extends RecyclerView.ViewHolder {
        protected GroupListViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected GroupListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<GroupWithMemberCount> diffCallback,
            GroupListPresenter thePresenter,
            Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
        theActivity = activity;
    }


}
