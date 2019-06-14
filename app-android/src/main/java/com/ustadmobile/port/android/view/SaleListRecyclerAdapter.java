package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CommonHandlerPresenter;
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
    CommonHandlerPresenter mPresenter;
    boolean paymentsDueTab = false;
    boolean preOrderTab = false;

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
        ImageView saleDueDateImage = holder.itemView.findViewById(R.id.item_sale_order_due_date_image);

        assert entity != null;
        saleTitle.setText(entity.getSaleTitle());
        String saleAmountWithCurrency = Math.round(entity.getSaleAmount()) + " " +
                entity.getSaleCurrency();
        saleLocation.setText(entity.getLocationName());

        String creationDatePretty = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(entity.getSaleCreationDate(), null);
        saleOrderDate.setText(creationDatePretty);

        saleAmount.setText(saleAmountWithCurrency);
        saleAmount.setTextColor(ContextCompat.getColor(theContext, R.color.text_primary));

        long earliestDueDate = entity.getEarliestDueDate();
        String dueDatePretty = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(earliestDueDate, null);
        String dueString;
        if(theActivity != null){
            dueString = theActivity.getText(R.string.due) + " " + dueDatePretty;
        }else{
            dueString = theFragment.getText(R.string.due) + " " + dueDatePretty;
        }

        if(preOrderTab){
            saleDueDate.setVisibility(View.VISIBLE);
            saleDueDateImage.setVisibility(View.VISIBLE);
            if(earliestDueDate != 0 &&
                    earliestDueDate <= System.currentTimeMillis()){
                saleDueDate.setText(dueString);

            }else if(!entity.isSaleItemPreOrder()){

                    if(theActivity != null){
                        dueString = theActivity.getText(R.string.not_delivered).toString();
                    }else{
                        dueString = theFragment.getText(R.string.not_delivered).toString();
                    }

                    saleDueDate.setText(dueString);
            }else if (earliestDueDate != 0 &&
                    earliestDueDate > System.currentTimeMillis()){
                saleDueDate.setText(dueString);
                saleDueDate.setTextColor(ContextCompat.getColor(theContext, R.color.text_primary));
                saleDueDateImage.setColorFilter(ContextCompat.getColor(theContext,
                        R.color.text_primary));
            }else{
                saleDueDate.setVisibility(View.GONE);
                saleDueDateImage.setVisibility(View.GONE);
            }
        }else{
            saleDueDate.setVisibility(View.GONE);
            saleDueDateImage.setVisibility(View.GONE);
        }


        if(paymentsDueTab){
            //Also change amount to remaining amount and change its color
            String saleAmountRemainingWithCurrency = Math.round(entity.getSaleAmountDue()) + " " +
                    entity.getSaleCurrency();
            saleAmount.setText(saleAmountRemainingWithCurrency);
            saleAmount.setTextColor(ContextCompat.getColor(theContext, R.color.primary_dark));
        }


        ConstraintLayout item = holder.itemView.findViewById(R.id.item_sale_cl);
        item.setOnClickListener(v -> {
            String genTitle = entity.getSaleTitleGen();
            String saleName;
            if(genTitle != null && !genTitle.isEmpty()){
                saleName = genTitle;
            }else{
                saleName = entity.getSaleTitle();
            }
            mPresenter.handleCommonPressed(entity.getSaleUid(), saleName);
        });



    }

    class SaleListViewHolder extends RecyclerView.ViewHolder {
        SaleListViewHolder(View itemView) {
            super(itemView);
        }
    }


    SaleListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SaleListDetail> diffCallback,
            CommonHandlerPresenter thePresenter,
            boolean paymentsDue,
            boolean preOrder,
            Fragment fragment,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
        paymentsDueTab = paymentsDue;
        theFragment = fragment;
        preOrderTab = preOrder;
    }

    SaleListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SaleListDetail> diffCallback,
            CommonHandlerPresenter thePresenter,
            boolean paymentsDue,
            boolean preOrder,
            Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
        paymentsDueTab = paymentsDue;
        theActivity = activity;
        preOrderTab = preOrder;
    }


}
