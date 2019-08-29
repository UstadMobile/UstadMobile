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
import com.ustadmobile.core.controller.AuditLogListPresenter;
import com.ustadmobile.lib.db.entities.AuditLogWithNames;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.Person;

public class AuditLogListRecyclerAdapter extends
        PagedListAdapter<AuditLogWithNames,
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

        AuditLogWithNames entity = getItem(position);

        TextView title = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_title);
        TextView desc = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_desc);
        AppCompatImageView menu =
                holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_dots);

        //"Actor changed Entity Type Entity Name at Time"
        String entityType = "";
        String entityName = "";
        switch(entity.getAuditLogTableUid()){
            case Clazz.TABLE_ID:
                entityType = theActivity.getText(R.string.clazz).toString();
                entityName = entity.getClazzName();
                break;
            case Person.TABLE_ID:
                entityType = theActivity.getText(R.string.person).toString();
                entityName = entity.getPersonName();
                break;
            default:
                break;
        }
        String logString = entity.getActorName() + " " + theActivity.getText(R.string.changed) + " " +
                entityType + " " + entityName;
        title.setText(logString);

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);

            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {

                    return true;
                } else if (i == R.id.delete) {

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

        menu.setVisibility(View.GONE);
        desc.setVisibility(View.GONE);
    }

    protected class AuditLogListViewHolder extends RecyclerView.ViewHolder {
        protected AuditLogListViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected AuditLogListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<AuditLogWithNames> diffCallback,
            AuditLogListPresenter thePresenter,
            Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theActivity = activity;
        theContext = context;
    }


}
