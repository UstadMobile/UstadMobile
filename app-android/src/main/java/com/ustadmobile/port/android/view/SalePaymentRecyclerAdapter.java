package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Activity;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleDetailPresenter;

import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.lib.db.entities.SalePayment;

public class SalePaymentRecyclerAdapter extends
        PagedListAdapter<SalePayment,
                SalePaymentRecyclerAdapter.SaleDetailViewHolder> {

    Context theContext;
    Activity theActivity;
    SaleDetailPresenter mPresenter;

    @NonNull
    @Override
    public SaleDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_key_value, parent, false);
        return new SaleDetailViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull SaleDetailViewHolder holder, int position) {

        SalePayment entity = getItem(position);

        String prettyDate = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                        entity.getSalePaymentPaidDate(), null);
        TextView keyTV = holder.itemView.findViewById(R.id.item_key_value_key);
        TextView valueTV = holder.itemView.findViewById(R.id.item_key_value_value);

        String amountText = entity.getSalePaymentPaidAmount() + " " +
                entity.getSalePaymentCurrency();
        keyTV.setText(prettyDate);
        valueTV.setText(amountText);

        AppCompatImageView dots = holder.itemView.findViewById(R.id.item_key_value_context_dots);

        //Options to Edit/Delete every schedule in the list
        dots.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);
            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {
                    mPresenter.handleEditPayment(entity.getSalePaymentUid());
                    return true;
                } else if (i == R.id.delete) {
                    mPresenter.handleDeletePayment(entity.getSalePaymentUid());
                    return true;
                } else {
                    return false;
                }
            });
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_edit_delete);

            popup.getMenu().findItem(R.id.edit).setVisible(true);

            //displaying the popup
            popup.show();
        });
    }

    protected class SaleDetailViewHolder extends RecyclerView.ViewHolder {
        protected SaleDetailViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected SalePaymentRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SalePayment> diffCallback,
            SaleDetailPresenter thePresenter,
            Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
        theActivity = activity;
    }


}
