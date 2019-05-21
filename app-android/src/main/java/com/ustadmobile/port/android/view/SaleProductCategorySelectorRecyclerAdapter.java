package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleProductDetailPresenter;
import com.ustadmobile.core.db.dao.SaleProductDao;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.lib.db.entities.SaleProductSelected;

public class SaleProductCategorySelectorRecyclerAdapter extends
        PagedListAdapter<SaleProductSelected,
                SaleProductCategorySelectorRecyclerAdapter.SaleProductDetailViewHolder> {

    Context theContext;
    Activity theActivity;
    SaleProductDetailPresenter mPresenter;

    @NonNull
    @Override
    public SaleProductDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_category_selector, parent, false);
        return new SaleProductDetailViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull SaleProductDetailViewHolder holder, int position) {

        SaleProductSelected saleProductCategory = getItem(position);
        CheckBox checkBox = holder.itemView.findViewById(R.id.item_category_selector_checkbox);
        checkBox.setChecked(saleProductCategory.isSelected());




    }

    protected class SaleProductDetailViewHolder extends RecyclerView.ViewHolder {
        protected SaleProductDetailViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected SaleProductCategorySelectorRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SaleProductSelected> diffCallback,
            SaleProductDetailPresenter thePresenter,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
    }


}
