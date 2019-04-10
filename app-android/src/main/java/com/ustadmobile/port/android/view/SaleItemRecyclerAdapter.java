package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleDetailPresenter;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.lib.db.entities.SaleItemListDetail;

import java.io.File;

public class SaleItemRecyclerAdapter extends
        PagedListAdapter<SaleItemListDetail,
                SaleItemRecyclerAdapter.SaleDetailViewHolder> {

    Context theContext;
    Activity theActivity;
    SaleDetailPresenter mPresenter;
    public static final int IMAGE_WITH = 52;

    @NonNull
    @Override
    public SaleDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sale_item, parent, false);
        return new SaleDetailViewHolder(list);

    }

    private static int dpToPxImagePerson() {
        return (int) (IMAGE_WITH
                * Resources.getSystem().getDisplayMetrics().density);
    }

    private void setPictureOnView(String imagePath, ImageView theImage) {

        Uri imageUri = Uri.fromFile(new File(imagePath));

        Picasso
                .get()
                .load(imageUri)
                .resize(dpToPxImagePerson(), dpToPxImagePerson())
                .noFade()
                .into(theImage);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleDetailViewHolder holder, int position) {

        SaleItemListDetail entity = getItem(position);

        long pictureUid = entity.getSaleItemPictureUid();
        ImageView imageView = holder.itemView.findViewById(R.id.item_sale_item_image);
        String imagePath = "";
        if (pictureUid != 0) {
            imagePath = UmAppDatabase.getInstance(theContext).getSaleProductPictureDao()
                    .getAttachmentPath(pictureUid);
        }

        if(imagePath != null && !imagePath.isEmpty())
            setPictureOnView(imagePath, imageView);
        else
            imageView.setImageResource(R.drawable.ic_card_giftcard_black_24dp);

        TextView itemName = holder.itemView.findViewById(R.id.item_sale_item_name);
        TextView itemQuantity = holder.itemView.findViewById(R.id.item_sale_item_quantity);
        TextView itemPrice = holder.itemView.findViewById(R.id.item_sale_item_price);
        TextView itemTotal = holder.itemView.findViewById(R.id.item_sale_item_total);
        TextView dueDate = holder.itemView.findViewById(R.id.item_sale_item_due_date);
        ImageView dueDateImage = holder.itemView.findViewById(R.id.item_sale_item_warning_image);

        ConstraintLayout itemWhole = holder.itemView.findViewById(R.id.item_sale_item_cl);

        int quantity = entity.getSaleItemQuantity();
        float price = entity.getSaleItemPricePerPiece();

        String priceString = String.valueOf(price) + theActivity.getString(R.string.currency_afs);
        String priceTotalString = String.valueOf(Math.round(quantity*price)) + theActivity.getString(R.string.currency_afs);
        String dueString = theActivity.getString(R.string.due) + " " +
                UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                        entity.getSaleItemDueDate(), null);

        itemName.setText(entity.getSaleItemProductName());
        itemQuantity.setText(String.valueOf(quantity));
        itemPrice.setText(priceString);
        itemTotal.setText(priceTotalString);

        //Sprint 2:
        //dueDate.setVisibility(View.VISIBLE);
        //dueDateImage.setVisibility(View.VISIBLE);
        //dueDate.setText(dueString);

        itemWhole.setOnClickListener(v ->
                mPresenter.handleClickSaleItemEdit(entity.getSaleItemUid()));



    }

    protected class SaleDetailViewHolder extends RecyclerView.ViewHolder {
        protected SaleDetailViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected SaleItemRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SaleItemListDetail> diffCallback,
            SaleDetailPresenter thePresenter,
            Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theActivity = activity;
        theContext = context;
    }


}
