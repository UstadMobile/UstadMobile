package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
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
import com.ustadmobile.core.controller.CustomFieldListPresenter;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.lib.db.entities.CustomField;

public class CustomFieldListRecyclerAdapter extends
        PagedListAdapter<CustomField,
                CustomFieldListRecyclerAdapter.CustomFieldListViewHolder> {

    Context theContext;
    Activity theActivity;
    CustomFieldListPresenter mPresenter;

    @NonNull
    @Override
    public CustomFieldListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false);
        return new CustomFieldListViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull CustomFieldListViewHolder holder, int position) {

        CustomField entity = getItem(position);

        TextView title = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_title);
        TextView desc = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_desc);
        AppCompatImageView menu =
                holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_dots);

        assert entity != null;
        title.setText(entity.getCustomFieldName());
        switch(entity.getCustomFieldType()){
            case CustomField.FIELD_TYPE_TEXT:
                desc.setText(theActivity.getString(MessageID.text));
                break;
            case CustomField.FIELD_TYPE_DROPDOWN:
                desc.setText(theActivity.getString(MessageID.dropdown));
                break;
            default:break;
        }

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);

            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {
                    mPresenter.handleClickEditCustomField(entity.getCustomFieldUid());
                    return true;
                } else if (i == R.id.delete) {
                    mPresenter.handleClickDeleteCustomField(entity.getCustomFieldUid());
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

    protected class CustomFieldListViewHolder extends RecyclerView.ViewHolder {
        protected CustomFieldListViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected CustomFieldListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<CustomField> diffCallback,
            CustomFieldListPresenter thePresenter, Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theActivity = activity;
        theContext = context;
    }


}
