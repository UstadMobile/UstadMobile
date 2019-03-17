package com.ustadmobile.port.android.view;

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

        TextView title = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_title);
        TextView desc = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_desc);
        AppCompatImageView menu =
                holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_dots);

        holder.itemView.setOnClickListener(v -> {
            assert entity != null;
            mPresenter.handleEditRole(entity.getRoleUid());
        });

        assert entity != null;
        title.setText(entity.getRoleName());
        long rolePermissions = entity.getRolePermissions();
        int count = Long.bitCount(rolePermissions);
        String permissionString = theActivity.getText(R.string.permissions).toString();
        if(count == 1){
            permissionString = theActivity.getText(R.string.permission).toString();
        }
        String roleDesc = count + " " + permissionString;
        desc.setText(roleDesc);

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);

            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {
                    mPresenter.handleEditRole(entity.getRoleUid());
                    return true;
                } else if (i == R.id.delete) {
                    mPresenter.handleRoleDelete(entity.getRoleUid());
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

    protected class RoleListViewHolder extends RecyclerView.ViewHolder {
        protected RoleListViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected RoleListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<Role> diffCallback,
            RoleListPresenter thePresenter,
            Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
        theActivity = activity;
    }


}
