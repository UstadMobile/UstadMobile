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
import com.ustadmobile.core.controller.RoleAssignmentListPresenter;

import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.EntityRoleWithGroupName;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;

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

        TextView title = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_title);
        TextView desc = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_desc);
        AppCompatImageView menu =
                holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_dots);

        holder.itemView.setOnClickListener(v -> mPresenter.handleEditRoleAssignment(entity.getErUid()));
        String titleText = entity.getGroupName() + " -> " + entity.getRoleName();
        String scopeName = null;
        String assigneeName = null;
        switch (entity.getErTableId()){
            case Clazz.TABLE_ID:
                scopeName = theActivity.getText(R.string.clazz).toString();
                assigneeName = entity.getClazzName();
                break;
            case Location.TABLE_ID:
                scopeName = theActivity.getText(R.string.location).toString();
                assigneeName = entity.getLocationName();
                break;
            case Person.TABLE_ID:
                scopeName = theActivity.getText(R.string.person).toString();
                assigneeName = entity.getPersonName();
                break;
            default:
                break;
        }

        String descText = "";
        if(scopeName != null && assigneeName != null){
            descText = theActivity.getText(R.string.in).toString()
                    + " " +scopeName + ": " + assigneeName;
        }

        title.setText(titleText);
        desc.setText(descText);

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);

            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {
                    mPresenter.handleEditRoleAssignment(entity.getErUid());
                    return true;
                } else if (i == R.id.delete) {
                    mPresenter.handleDeleteRoleAssignment(entity.getErUid());
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

    protected class RoleAssignmentListViewHolder extends RecyclerView.ViewHolder {
        protected RoleAssignmentListViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected RoleAssignmentListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<EntityRoleWithGroupName> diffCallback,
            RoleAssignmentListPresenter thePresenter,
            Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
        theActivity = activity;
    }


}
