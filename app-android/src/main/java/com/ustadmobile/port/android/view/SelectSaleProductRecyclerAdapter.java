package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SelectSaleProductPresenter;

import com.ustadmobile.lib.db.entities.SaleNameWithImage;

import static com.ustadmobile.lib.db.entities.SaleProductGroup.PRODUCT_GROUP_TYPE_PRODUCT;

public class SelectSaleProductRecyclerAdapter extends
        PagedListAdapter<SaleNameWithImage,
                SelectSaleProductRecyclerAdapter.SelectSaleProductViewHolder> {

    Context theContext;
    Activity theActivity;
    SelectSaleProductPresenter mPresenter;

    @NonNull
    @Override
    public SelectSaleProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sale_product_blob, parent, false);
        return new SelectSaleProductViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull SelectSaleProductViewHolder holder, int position) {

        SaleNameWithImage entity = getItem(position);
        ImageView image = holder.itemView.findViewById(R.id.item_sale_product_blob_image);
        TextView name = holder.itemView.findViewById(R.id.item_sale_product_blob_title);

        //TODO: Get picture
        long pictureUid = entity.getPictureUid();

        name.setText(entity.getName());

        holder.itemView.setOnClickListener(v -> {
            if(entity.getType() == PRODUCT_GROUP_TYPE_PRODUCT) {
                mPresenter.handleClickProduct(entity.getProductUid());
            }
        });

    }

    protected class SelectSaleProductViewHolder extends RecyclerView.ViewHolder {
        protected SelectSaleProductViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected SelectSaleProductRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SaleNameWithImage> diffCallback,
            SelectSaleProductPresenter thePresenter,
            Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theActivity = activity;
        theContext = context;
    }


}
