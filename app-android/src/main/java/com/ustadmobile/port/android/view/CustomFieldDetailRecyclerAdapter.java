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
import com.ustadmobile.core.controller.CustomFieldDetailPresenter;
import com.ustadmobile.lib.db.entities.CustomFieldValueOption;

public class CustomFieldDetailRecyclerAdapter extends
        PagedListAdapter<CustomFieldValueOption,
                CustomFieldDetailRecyclerAdapter.CustomFieldDetailViewHolder> {

    Context theContext;
    Activity theActivity;
    CustomFieldDetailPresenter mPresenter;

    @NonNull
    @Override
    public CustomFieldDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_title_with_desc_and_dots, parent, false);
        return new CustomFieldDetailViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull CustomFieldDetailViewHolder holder, int position) {

        CustomFieldValueOption entity = getItem(position);

        TextView title = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_title);
        TextView desc = holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_desc);
        AppCompatImageView menu =
                holder.itemView.findViewById(R.id.item_title_with_desc_and_dots_dots);

        assert entity != null;
        title.setText(entity.getCustomFieldValueOptionName());
        desc.setText("");

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);

            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {
                    mPresenter.handleClickOptionEdit(entity.getCustomFieldValueOptionUid());
                    return true;
                } else if (i == R.id.delete) {
                    mPresenter.handleClickOptionDelete(entity.getCustomFieldValueOptionUid());
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

    protected class CustomFieldDetailViewHolder extends RecyclerView.ViewHolder {
        protected CustomFieldDetailViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected CustomFieldDetailRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<CustomFieldValueOption> diffCallback,
            CustomFieldDetailPresenter thePresenter, Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theActivity = activity;
        theContext = context;
    }


}
