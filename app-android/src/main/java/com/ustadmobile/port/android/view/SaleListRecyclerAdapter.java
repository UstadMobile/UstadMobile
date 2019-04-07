package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.app.Activity;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleListPresenter;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.lib.db.entities.SaleListDetail;

public class SaleListRecyclerAdapter extends
        PagedListAdapter<SaleListDetail,
                SaleListRecyclerAdapter.SaleListViewHolder> {

    Context theContext;
    Activity theActivity;
    Fragment theFragment;
    SaleListPresenter mPresenter;

    @NonNull
    @Override
    public SaleListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sale, parent, false);
        return new SaleListViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull SaleListViewHolder holder, int position) {

        SaleListDetail entity = getItem(position);

        TextView saleTitle = holder.itemView.findViewById(R.id.item_sale_title);
        TextView saleAmount = holder.itemView.findViewById(R.id.item_sale_amount);
        TextView saleLocation = holder.itemView.findViewById(R.id.item_sale_location);
        TextView saleOrderDate = holder.itemView.findViewById(R.id.item_sale_order_date);
        TextView saleDueDate = holder.itemView.findViewById(R.id.item_sale_order_due_date);

        assert entity != null;
        saleTitle.setText(entity.getSaleTitle());
        String saleAmountWithCurrency = String.valueOf(Math.round(entity.getSaleAmount())) + " " +
                entity.getSaleCurrency();
        saleAmount.setText(saleAmountWithCurrency);
        saleLocation.setText(entity.getLocationName());

        String creationDatePretty = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(entity.getSaleCreationDate(), null);
        String dueDatePretty = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(entity.getSaleDueDate(), null);

        saleOrderDate.setText(String.valueOf(creationDatePretty));
        String dueString = theFragment.getText(R.string.due) + " " + String.valueOf(dueDatePretty);
        saleDueDate.setText(dueString);

        if(entity.getSaleDueDate() != 0 &&
                entity.getSaleDueDate() < System.currentTimeMillis()){
            saleDueDate.setVisibility(View.VISIBLE);
        }else{
            saleDueDate.setVisibility(View.GONE);
        }

        ConstraintLayout item = holder.itemView.findViewById(R.id.item_sale_cl);
        item.setOnClickListener(v -> mPresenter.handleClickSale(entity.getSaleUid()));



    }

    protected class SaleListViewHolder extends RecyclerView.ViewHolder {
        protected SaleListViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected SaleListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SaleListDetail> diffCallback,
            SaleListPresenter thePresenter,
            Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
        theActivity = activity;
    }

    protected SaleListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SaleListDetail> diffCallback,
            SaleListPresenter thePresenter,
            Fragment fragment,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
        theFragment = fragment;
    }


}
